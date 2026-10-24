package com.dresso.app.core.threeD

import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.MeasurementField
import kotlin.math.roundToInt

/**
 * Calibrated against the *silhouette estimates* in the Blender project's TryOn_Config.json.
 * These neutral centimetres are model references, not a person's tape measurements or fit claims.
 * A combined +0.6 preset compressed 32 armpit edges in Arms_Raise. Limit the interactive
 * controls to [-0.5, +0.5], and height to [-0.5, +0.3]; these bounds kept all evaluated edges
 * above half their neutral length across the tested animations. Blender's keys allow [-1, +1].
 */
enum class BodyMorphControl(
    val morphName: String,
    val neutralCm: Float,
    private val gain: Float,
    private val relativeGain: Boolean,
    val field: MeasurementField?,
    val maxWeight: Float = MAX_WEIGHT,
) {
    HEIGHT("Height", 165.9f, 0.08f, true, MeasurementField.HEIGHT, maxWeight = 0.3f),
    SHOULDER("Shoulder_Width", 49.7f, 10f, false, MeasurementField.SHOULDER),
    CHEST("Chest_Circumference", 107.9f, 0.16f, true, MeasurementField.CHEST),
    WAIST("Waist_Circumference", 67.6f, 0.16f, true, MeasurementField.WAIST),
    BELLY("Belly_Circumference", 74.6f, 0.16f, true, MeasurementField.BELLY),
    HIPS("Hip_Circumference", 93.8f, 0.16f, true, MeasurementField.HIPS),
    BELLY_PROJECTION("Belly_Projection", 0f, 5.5f, false, null),
    ;

    val minCm: Float get() = cmAtWeight(MIN_WEIGHT)
    val maxCm: Float get() = cmAtWeight(maxWeight)

    fun cmAtWeight(weight: Float): Float = if (relativeGain) {
        neutralCm * (1f + gain * weight)
    } else {
        neutralCm + gain * weight
    }

    fun valueCm(measurements: BodyMeasurements?): Float? = when (this) {
        BELLY_PROJECTION -> measurements?.bellyProjectionCm
        else -> field?.let { measurements?.valueFor(it)?.toFloat() }
    }

    fun weightFor(measurements: BodyMeasurements?): Float {
        val centimetres = valueCm(measurements)?.takeIf { it.isFinite() } ?: return 0f
        if (this != BELLY_PROJECTION && centimetres <= 0f) return 0f
        val weight = if (relativeGain) {
            (centimetres / neutralCm - 1f) / gain
        } else {
            (centimetres - neutralCm) / gain
        }
        return weight.coerceIn(MIN_WEIGHT, maxWeight)
    }

    fun withValue(measurements: BodyMeasurements, centimetres: Float): BodyMeasurements =
        if (this == BELLY_PROJECTION) {
            measurements.copy(bellyProjectionCm = (centimetres * 10f).roundToInt() / 10f)
        } else {
            measurements.withValue(requireNotNull(field), centimetres.roundToInt())
        }

    companion object {
        const val MIN_WEIGHT = -0.5f
        const val MAX_WEIGHT = 0.5f
    }
}
