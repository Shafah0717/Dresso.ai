package com.dresso.app.core.threeD

import com.dresso.app.data.model.BodyMeasurements

/**
 * Seam for future fitted-garment rendering. The human avatar preview is rendered by
 * SceneView in AvatarViewport; body measurements preview morphs, but garments are not fitted.
 */
data class AvatarRenderRequest(
    val garmentId: String?,
    val garmentAssetPath: String? = null,
    val colorId: String?,
    val sizeId: String?,
    val measurements: BodyMeasurements?,
)

interface AvatarRenderer {
    val isAvailable: Boolean
}

class PendingAvatarRenderer : AvatarRenderer {
    override val isAvailable: Boolean = false
}
