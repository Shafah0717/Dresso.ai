package com.dresso.app.core.threeD

import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import kotlin.math.cos
import kotlin.math.sin

internal enum class GarmentVerticalSpan {
    /** Sole to collar (headless garment height on the try-on avatar). */
    FULL,
    /** Waist/chest band only; used for shirts. */
    TORSO,
}

internal enum class GarmentVerticalAnchor {
    /** Match mesh min Y to the body's grounded min Y after centering. */
    FEET,
    /** Match bounding-box centers (legacy shirt registration). */
    CENTER,
    /** Place a torso garment around the chest instead of the avatar's scene origin. */
    CHEST,
}

internal data class GarmentAlignProfile(
    val yawDegrees: Float,
    val envelopeMargin: Float = 1.00f,
    val verticalSpan: GarmentVerticalSpan = GarmentVerticalSpan.FULL,
    val verticalAnchor: GarmentVerticalAnchor = GarmentVerticalAnchor.FEET,
    /** When false, depth does not inflate uniform scale (headless outfits). */
    val useDepthForScale: Boolean = true,
    val verticalOffset: Float = 0f,
)

internal data class GarmentAlignment(
    val position: Position,
    val rotation: Rotation,
    val scale: Scale,
)

internal fun garmentAlignProfile(path: String): GarmentAlignProfile? = when (path) {
    // Collared shirt front faces +X; outfit faces +Z.
    "garments/nike_garment_01.glb" -> GarmentAlignProfile(
        yawDegrees = -90f,
        envelopeMargin = 1.04f,
        verticalSpan = GarmentVerticalSpan.TORSO,
        verticalAnchor = GarmentVerticalAnchor.CENTER,
    )
    "garments/nike_garment_02.glb" -> GarmentAlignProfile(
        yawDegrees = 0f,
        // Calibrated to the avatar's full-body bounds.
        envelopeMargin = 0.98f,
        verticalSpan = GarmentVerticalSpan.FULL,
        verticalAnchor = GarmentVerticalAnchor.FEET,
        useDepthForScale = false,
        verticalOffset = -0.06f,
    )
    // Zara's export is already skinned to the same human rig and is authored in avatar space.
    // Keep it at the avatar origin; the shared animation is applied in AvatarViewport.
    "garments/zara_garmet.glb" -> GarmentAlignProfile(
        yawDegrees = 0f,
        // The Zara export is a shirt (mesh bounds are collar-to-hem), not a full outfit.
        // Register it against the avatar's torso so it cannot stretch down to the ankles.
        envelopeMargin = 0.92f,
        verticalSpan = GarmentVerticalSpan.TORSO,
        verticalAnchor = GarmentVerticalAnchor.CHEST,
        useDepthForScale = false,
    )
    "garments/zara_grament2.glb" -> GarmentAlignProfile(
        yawDegrees = 0f,
        envelopeMargin = 1.0f,
        verticalSpan = GarmentVerticalSpan.TORSO,
        verticalAnchor = GarmentVerticalAnchor.CENTER,
        useDepthForScale = false,
    )
    else -> null
}

/** Avatar mesh bounds after the same root translation applied in [AvatarViewport]. */
internal fun bodyBoundsInScene(body: NeutralBounds): NeutralBounds {
    val cx = body.centerX
    val cy = body.centerY
    val cz = body.centerZ
    return NeutralBounds(
        body.minX - cx, body.minY - cy, body.minZ - cz,
        body.maxX - cx, body.maxY - cy, body.maxZ - cz,
    )
}

/**
 * Center the garment on the avatar's scene origin and scale so neutral bounds envelope the body.
 * Unskinned meshes still do not follow morphs or animation; this only replaces hand-tuned offsets.
 */
internal fun alignGarmentToAvatar(
    body: NeutralBounds,
    garment: NeutralBounds,
    profile: GarmentAlignProfile,
): GarmentAlignment {
    val sceneBody = bodyBoundsInScene(body)
    val targetHeight = when (profile.verticalSpan) {
        GarmentVerticalSpan.FULL -> (sceneBody.height - headAboveCollar(body)).coerceAtLeast(MIN_EXTENT)
        GarmentVerticalSpan.TORSO -> sceneBody.height * TORSO_HEIGHT_FRACTION
    }
    val scaleX = sceneBody.width / garment.width.coerceAtLeast(MIN_EXTENT)
    val scaleY = targetHeight / garment.height.coerceAtLeast(MIN_EXTENT)
    val scaleZ = sceneBody.depth / garment.depth.coerceAtLeast(MIN_EXTENT)
    val uniformScale = when {
        profile.useDepthForScale -> maxOf(scaleX, scaleY, scaleZ) * profile.envelopeMargin
        // Headless outfits (sole → collar): depth/width must not stretch past the neck opening.
        profile.verticalSpan == GarmentVerticalSpan.FULL ->
            scaleY * profile.envelopeMargin
        else -> maxOf(scaleX, scaleY) * profile.envelopeMargin
    }

    val yawRad = Math.toRadians(profile.yawDegrees.toDouble()).toFloat()
    val cosYaw = cos(yawRad)
    val sinYaw = sin(yawRad)
    val gcX = garment.centerX
    val gcY = garment.centerY
    val gcZ = garment.centerZ
    val rotatedCenterX = uniformScale * (gcX * cosYaw - gcZ * sinYaw)
    val rotatedCenterZ = uniformScale * (gcX * sinYaw + gcZ * cosYaw)
    val positionY = when (profile.verticalAnchor) {
        GarmentVerticalAnchor.FEET -> sceneBody.minY - uniformScale * garment.minY
        GarmentVerticalAnchor.CENTER -> -uniformScale * gcY
        GarmentVerticalAnchor.CHEST ->
            (sceneBody.minY + sceneBody.height * 0.25f) - uniformScale * gcY
    } + profile.verticalOffset

    return GarmentAlignment(
        position = Position(x = -rotatedCenterX, y = positionY, z = -rotatedCenterZ),
        rotation = Rotation(y = profile.yawDegrees),
        scale = Scale(x = uniformScale, y = uniformScale, z = uniformScale),
    )
}

private const val MIN_EXTENT = 1e-5f
private const val TORSO_HEIGHT_FRACTION = 0.52f
/** Crown-to-collar on the try-on avatar neutral mesh (~1.45 m collar vs ~1.67 m crown). */
private const val HEAD_ABOVE_COLLAR = 0.22f

private fun headAboveCollar(body: NeutralBounds): Float =
    HEAD_ABOVE_COLLAR.coerceIn(MIN_EXTENT, body.height - MIN_EXTENT)
