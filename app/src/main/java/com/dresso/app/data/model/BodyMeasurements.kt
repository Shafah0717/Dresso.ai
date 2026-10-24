package com.dresso.app.data.model

/**
 * Stored in centimetres, independent of display units and of any 3D implementation.
 * Every field is optional: browsing never requires a completed profile.
 */
data class BodyMeasurements(
    val heightCm: Int? = null,
    val chestCm: Int? = null,
    val bellyCm: Int? = null,
    val waistCm: Int? = null,
    val hipsCm: Int? = null,
    val shoulderCm: Int? = null,
    val inseamCm: Int? = null,
    val updatedAtEpochMillis: Long? = null,
    /** Signed deviation of the abdomen toward/away from the front, in centimetres. */
    val bellyProjectionCm: Float? = null,
) {
    val values: List<Int?>
        get() = listOf(heightCm, shoulderCm, chestCm, waistCm, bellyCm, hipsCm, inseamCm)

    val completedFields: Int
        get() = values.count { it != null }

    val hasAny: Boolean
        get() = completedFields > 0 || bellyProjectionCm != null

    val isComplete: Boolean
        get() = completedFields == MEASUREMENT_FIELD_COUNT

    fun valueFor(field: MeasurementField): Int? = when (field) {
        MeasurementField.HEIGHT -> heightCm
        MeasurementField.CHEST -> chestCm
        MeasurementField.BELLY -> bellyCm
        MeasurementField.WAIST -> waistCm
        MeasurementField.HIPS -> hipsCm
        MeasurementField.SHOULDER -> shoulderCm
        MeasurementField.INSEAM -> inseamCm
    }

    fun withValue(field: MeasurementField, valueCm: Int?): BodyMeasurements = when (field) {
        MeasurementField.HEIGHT -> copy(heightCm = valueCm)
        MeasurementField.CHEST -> copy(chestCm = valueCm)
        MeasurementField.BELLY -> copy(bellyCm = valueCm)
        MeasurementField.WAIST -> copy(waistCm = valueCm)
        MeasurementField.HIPS -> copy(hipsCm = valueCm)
        MeasurementField.SHOULDER -> copy(shoulderCm = valueCm)
        MeasurementField.INSEAM -> copy(inseamCm = valueCm)
    }

    companion object {
        const val MEASUREMENT_FIELD_COUNT = 7
        val Empty = BodyMeasurements()
    }
}

enum class MeasurementField(
    val label: String,
    val minCm: Int,
    val maxCm: Int,
) {
    HEIGHT("Height", 120, 230),
    SHOULDER("Shoulder width", 30, 80),
    CHEST("Chest", 60, 180),
    WAIST("Waist", 45, 180),
    BELLY("Belly circumference", 45, 180),
    HIPS("Hips", 60, 190),
    INSEAM("Inseam", 45, 120),
}
