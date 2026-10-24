# Preparing the two demo garment sources for a wearable export

The assets under `app/src/main/assets/garments/` are **source samples, not wearable meshes**.
The app fixes the avatar at the viewer centre and registers each source garment over its
neutral body landmarks as a clearly labelled **unrigged alignment preview**. This is not
a fitted outfit; arms, back and changing body proportions can intersect. Keep the originals and their embedded
materials/textures intact; export a new `*_fitted.glb` only after the following checks pass.

## Source inspection (2026-10-24)

- `nike_garment_01.glb` (`free_shirt.glb`): a collared, long-sleeve shirt with 13 mesh
  components, 13 materials, two embedded PNGs, UVs and normals. Its glTF scene spans
  approximately **5.34 × 7.87 × 6.18 m** (X × Y × Z); the front faces **+X** instead of the
  avatar's **+Z**. One sleeve is bent forward. There is no skin, animation or morph target.
  Placing its collar at the avatar's neck (about 1.45 m above the feet), its hem at about
  0.75 m, and its cuffs near the wrists still leaves the torso and sleeves behind the body:
  **22.9%** of its torso vertices are more than 3 cm inside the avatar in that inspection pose.
- `nike_garment_02.glb` (`shirt_with_pants_3d_model_free.glb`): one 21,063-vertex mesh
  representing a jacket, trousers and shoes, with one embedded 4096² JPEG, UVs and normals.
  It spans **0.671 × 1.0 × 0.237 m** (X × Y × Z), from shoe sole to headless collar. It has no
  skin, animation or morph target. Its triangle soup has **18,721 boundary edges and 26
  non-manifold edges**. Aligning its sole to Y=0 and collar to the avatar's neck still puts
  **17.4%** of the torso vertices and **5.9%** of leg vertices more than 3 cm inside the body.
  A trial nearest-surface skin/shape transfer produced visible holes at the chest, shins and
  feet in the neutral and animated poses. It is **not** a fitted derivative to ship.

## Repeatable Blender workflow

1. Duplicate each source into a separate working `.blend`. Import the existing
   `Human_TryOn_Rigged.glb` as the reference, set the avatar to its rest A-pose and keep its
   original 53-joint skin and 17 morphs. Use metres (glTF Y-up); do not edit the app's avatar
   GLB. Record the world positions of collar, shoulders, chest, waist, hips, elbows, wrists,
   knees, ankles and soles on both meshes.
2. For **01**, first orient +X forward to +Z and manually re-shape the 13 connected design
   components against the avatar's collar, arm and torso landmarks. Straighten/re-pose the
   forward-bent sleeve before weighting. Preserve placket/cuffs, UVs and both PNGs. A global
   scale or translation is only coordinate registration, **not** a fitting solution.
3. For **02**, separate jacket, trousers and shoes by connected surface and material/UV
   islands; repair the non-manifold and open surfaces where practical. Align collar-to-sole
   length to the avatar's neck-to-sole distance, then fit jacket chest/shoulders, trouser
   waist/hips/knees and shoe openings independently. Preserve the JPEG and UVs.
4. Bind each component to the **same avatar bind pose**, not merely bones with matching names.
   Compare all 53 joint names, parent hierarchy and inverse bind matrices; use at most four
   normalized influences per vertex. Hand-paint shoulders, sleeve cuffs, crotch, knees and
   ankles. Animate with `Idle`, `Arms_Raise`, `Turn_Torso` and `Walk_Test` at the same clock as
   the avatar. Reject a derivative that exposes body through the cloth or floats away.
5. Create garment-specific morphs for the supported controls in `BodyMorphMapping.kt`:
   `Shoulder_Width`, `Chest_Circumference`, `Waist_Circumference`, `Belly_Circumference`,
   `Hip_Circumference`, `Height` and `Belly_Projection` **only where the garment covers that
   body region**. Adjust sleeve/leg length and hem/cuff clearance independently; never just
   scale the whole garment when a body measurement changes.
6. Validate neutral, small and large body settings and all four animations from front, side
   and rear. Check that seams do not tear, open surfaces do not reveal the body unexpectedly,
   no part sinks into the avatar, and shoes remain aligned with animated feet. Then export
   a **new** GLB with preserved textures, skin and relevant morph targets; run the glTF
   validator and test it on Android. Only after that replace the catalogue's source asset
   path with the verified fitted derivative and enable synchronized garment animation/morphs.

## Reproducible experimental rig candidates

Run the script below to generate editable `.blend` files and **candidate-only** GLBs without
changing the source assets or the APK:

```sh
blender -b --python tools/rig_garment_candidates.py -- 01 /path/to/garment_candidates
blender -b --python tools/rig_garment_candidates.py -- 02 /path/to/garment_candidates
```

Both candidate GLBs have a 53-joint skin with the same joint names and inverse bind matrices
(maximum observed element difference 0.0000035), all four animation clips, the seven named
body morph targets, and their source texture assets. The script transfers the closest four
avatar bone weights per garment vertex and interpolates body morph displacements. **This is a
starting rig, not a fitted garment.** In the inspected candidates, the shirt stretches into
large folds when arms rise; the outfit still shows body through sleeve, hip and lower-leg
openings. The originals remain selected in Dresso until those gaps have been hand-corrected
and the neutral, raised-arms, torso-turn, walking, small and large cases pass visually.

The Dresso body preview is calibrated from mesh-silhouette estimates, not tape measurements
or garment size charts. A fitted derivative still requires visual and physical fit validation.
