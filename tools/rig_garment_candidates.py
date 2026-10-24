"""Build *experimental* 53-joint garment candidates from the two source GLBs.

Run with Blender 5.2, never on the source export in place:
    blender -b --python tools/rig_garment_candidates.py -- 01 /path/to/output
    blender -b --python tools/rig_garment_candidates.py -- 02 /path/to/output

This transfers nearest-surface bone weights and body morph displacements after landmark
registration. It is a starting point for hand-painting and sculpting, NOT a validated fit:
the shirt stretches on Arms_Raise and the outfit exposes the avatar at its open surfaces.
The app deliberately continues to use the original GLBs until candidates pass inspection.
"""

import bpy
import math
import sys
from collections import defaultdict
from pathlib import Path
from mathutils import Matrix, Vector
from mathutils.bvhtree import BVHTree


args = sys.argv[sys.argv.index('--') + 1:]
if len(args) != 2 or args[0] not in {'01', '02'}:
    raise SystemExit('Usage: blender -b --python tools/rig_garment_candidates.py -- 01|02 OUTPUT_DIR')
kind, output_dir = args
root = Path(__file__).resolve().parent.parent
avatar_path = root / 'app/src/main/assets/models/Human_TryOn_Rigged.glb'
source_path = root / f'app/src/main/assets/garments/nike_garment_{kind}.glb'
output_dir = Path(output_dir).resolve()
output_dir.mkdir(parents=True, exist_ok=True)
name = f'nike_garment_{kind}_rigged_candidate'
controls = ('Shoulder_Width', 'Chest_Circumference', 'Belly_Circumference',
            'Waist_Circumference', 'Hip_Circumference', 'Height', 'Belly_Projection')


def bounds(objects):
    points = [obj.matrix_world @ Vector(corner)
              for obj in objects for corner in obj.bound_box]
    return (Vector((min(p[i] for p in points) for i in range(3))),
            Vector((max(p[i] for p in points) for i in range(3))))


def barycentric(point, a, b, c):
    u, v, w = b-a, c-a, point-a
    uu, uv, vv, wu, wv = u.dot(u), u.dot(v), v.dot(v), w.dot(u), w.dot(v)
    det = uu*vv - uv*uv
    if abs(det) < 1e-14:
        return (1/3, 1/3, 1/3)
    beta = (vv*wu - uv*wv)/det
    gamma = (uu*wv - uv*wu)/det
    return (1-beta-gamma, beta, gamma)


bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)
bpy.ops.import_scene.gltf(filepath=str(avatar_path))
avatar = next(obj for obj in bpy.context.scene.objects if obj.type == 'MESH')
rig = avatar.parent
assert rig and rig.type == 'ARMATURE' and len(rig.data.bones) == 53
rig.data.pose_position = 'REST'
existing = set(bpy.context.scene.objects)
bpy.ops.import_scene.gltf(filepath=str(source_path))
imported = set(bpy.context.scene.objects) - existing
garments = [obj for obj in imported if obj.type == 'MESH']
assert len(garments) == (13 if kind == '01' else 1)
source_min, source_max = bounds(garments)
source_size = source_max - source_min
avatar_min, avatar_max = bounds([avatar])
avatar_center = (avatar_min + avatar_max) / 2
avatar_size = avatar_max - avatar_min
neck = avatar_min.z + 1.45

if kind == '01':
    # Shirt source front is +X. Collar-to-hem=.70 m; sleeve span follows the A-pose.
    scale = Matrix.Diagonal(((avatar_size.y + 0.02)/source_size.x,
                             avatar_size.x/source_size.y,
                             0.70/source_size.z, 1.0))
    rotation = Matrix.Rotation(-math.pi/2, 4, 'Z')
    desired_center = Vector((avatar_center.x, avatar_center.y - 0.06,
                             (neck + avatar_min.z + 0.75)/2))
    registration = Matrix.Translation(
        desired_center - rotation @ scale @ ((source_min + source_max)/2)
    ) @ rotation @ scale
else:
    # Outfit source has neither head nor rig: register sole-to-collar to sole-to-neck.
    vertical_scale = (neck - avatar_min.z)/source_size.z
    scale = Matrix.Diagonal((vertical_scale,
                             (avatar_size.y + 0.02)/source_size.y,
                             vertical_scale, 1.0))
    desired_center = Vector((avatar_center.x, avatar_center.y - 0.06,
                             (neck + avatar_min.z)/2))
    registration = Matrix.Translation(
        desired_center - scale @ ((source_min + source_max)/2)
    ) @ scale

for obj in [obj for obj in imported if obj.parent not in imported]:
    obj.matrix_world = registration @ obj.matrix_world
bpy.context.view_layer.update()
for garment in garments:
    garment.data.transform(garment.matrix_world.copy())
    garment.parent = None
    garment.matrix_world = Matrix.Identity(4)
bpy.context.view_layer.update()

depsgraph = bpy.context.evaluated_depsgraph_get()
evaluated = avatar.evaluated_get(depsgraph)
body = evaluated.to_mesh()
body_points = [avatar.matrix_world @ v.co for v in body.vertices]
faces = [tuple(face.vertices) for face in body.polygons]
bvh = BVHTree.FromObject(avatar, depsgraph)
bone_names = {bone.name for bone in rig.data.bones}
groups = {group.index: group.name for group in avatar.vertex_groups}
shape_keys = avatar.data.shape_keys.key_blocks
deltas = {
    key: [avatar.matrix_world.to_3x3() @
          (shape_keys[key].data[i].co - shape_keys['Basis'].data[i].co)
          for i in range(len(avatar.data.vertices))]
    for key in controls
}

corrections = []
for garment in garments:
    target_groups = {
        group.name: garment.vertex_groups.new(name=group.name)
        for group in avatar.vertex_groups if group.name in bone_names
    }
    correspondence = []
    for vertex in garment.data.vertices:
        point = vertex.co.copy()
        nearest, normal, face_index, distance = bvh.find_nearest(point)
        signed_distance = (point-nearest).dot(normal)
        correction = max(0.0, 0.018-signed_distance)
        if correction:
            vertex.co += normal * min(correction, 0.13)
        corrections.append(correction)
        nearest, normal, face_index, distance = bvh.find_nearest(vertex.co)
        triangle = faces[face_index]
        bary = barycentric(nearest, *[body_points[i] for i in triangle])
        correspondence.append((triangle, bary, distance))
        weights = defaultdict(float)
        for index, fraction in zip(triangle, bary):
            for source_group in avatar.data.vertices[index].groups:
                bone = groups[source_group.group]
                if bone in target_groups:
                    weights[bone] += max(0.0, fraction) * source_group.weight
        strongest = sorted(weights.items(), key=lambda item: item[1], reverse=True)[:4]
        total = sum(weight for _, weight in strongest)
        if total <= 0:
            raise ValueError(f'No bone correspondence for {garment.name} vertex {vertex.index}')
        for bone, weight in strongest:
            target_groups[bone].add([vertex.index], weight/total, 'REPLACE')

    garment.shape_key_add(name='Basis', from_mix=False)
    for key in controls:
        shape = garment.shape_key_add(name=key, from_mix=False)
        for vertex, (triangle, bary, distance) in zip(garment.data.vertices, correspondence):
            displacement = sum((deltas[key][i] * fraction for i, fraction in zip(triangle, bary)),
                               Vector((0, 0, 0)))
            influence = max(0.0, min(1.0, (0.35-distance)/0.30))
            shape.data[vertex.index].co = vertex.co + displacement*influence
        shape.slider_min, shape.slider_max, shape.value = -1.0, 1.0, 0.0
    garment.parent = rig
    garment.modifiers.new('Avatar skin', 'ARMATURE').object = rig

evaluated.to_mesh_clear()
rig.data.pose_position = 'POSE'
for key in controls:
    shape_keys[key].value = 0.0
rig.animation_data.action = bpy.data.actions['Idle']
rig.animation_data.action_slot = rig.animation_data.action.slots[0]
bpy.context.scene.frame_set(0)
for action in bpy.data.actions:
    action.use_fake_user = True

# Do not export the avatar's original mesh as part of a garment candidate.
bpy.ops.object.select_all(action='DESELECT')
rig.select_set(True)
for garment in garments:
    garment.select_set(True)
bpy.context.view_layer.objects.active = rig
out_glb = output_dir / f'{name}.glb'
format_items = [item.identifier for item in bpy.ops.export_scene.gltf.get_rna_type()
                .properties['export_format'].enum_items]
if 'GLB' not in format_items:
    raise RuntimeError(f'Binary glTF export is unavailable: {format_items}')
animation_items = [item.identifier for item in bpy.ops.export_scene.gltf.get_rna_type()
                   .properties['export_animation_mode'].enum_items]
if 'ACTIONS' not in animation_items:
    raise RuntimeError(f'Cannot export all four actions: {animation_items}')
bpy.ops.export_scene.gltf(filepath=str(out_glb), export_format='GLB',
                          use_selection=True, export_animations=True,
                          export_animation_mode='ACTIONS', export_morph=True,
                          export_skins=True, export_texcoords=True,
                          export_normals=True)
out_blend = output_dir / f'{name}.blend'
bpy.ops.wm.save_as_mainfile(filepath=str(out_blend))
corrections.sort()
report = (
    f'{name}\n'
    f'Source: {source_path}\n'
    f'Output: {out_glb}\n'
    f'Geometry: {len(garments)} mesh(es), {sum(len(o.data.vertices) for o in garments)} vertices\n'
    f'Bone weights: interpolated from the avatar surface, strongest four per vertex\n'
    f'Morphs: {", ".join(controls)}\n'
    f'Vertices needing over 5 cm of push-out: {sum(v>0.05 for v in corrections)} '
    f'of {len(corrections)}\n'
    'STATUS: WORKING CANDIDATE ONLY. Visual fit fails; do not use as a wearable app asset.\n'
)
(output_dir / f'{name}.txt').write_text(report)
print(report, flush=True)
