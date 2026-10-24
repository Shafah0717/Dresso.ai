package com.dresso.app.core.threeD

import com.dresso.app.data.model.BodyMeasurements

/**
 * Opaque morph values, deliberately not derived from raw measurements by the MVP.
 * A future calibrated avatar rig can map these names to real blendshapes.
 */
data class AvatarMorphParameters(
    val values: Map<String, Float> = emptyMap(),
)

sealed interface BodyShapeAdaptation {
    data class Ready(val parameters: AvatarMorphParameters) : BodyShapeAdaptation

    data object NoMeasurements : BodyShapeAdaptation

    /** Morph preview is not a verified mapping for garment fit or physical body dimensions. */
    data object CalibrationRequired : BodyShapeAdaptation
}

interface BodyShapeAdapter {
    suspend fun adapt(measurements: BodyMeasurements?): BodyShapeAdaptation
}

/**
 * Future garment-fit adapter. The visual-only morph preview uses the Blender calibration
 * in BodyMorphControl; that approximation must not be mistaken for verified garment fit.
 */
class CalibrationPendingBodyShapeAdapter : BodyShapeAdapter {
    override suspend fun adapt(measurements: BodyMeasurements?): BodyShapeAdaptation =
        if (measurements == null) {
            BodyShapeAdaptation.NoMeasurements
        } else {
            BodyShapeAdaptation.CalibrationRequired
        }
}
