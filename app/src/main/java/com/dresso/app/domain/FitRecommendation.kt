package com.dresso.app.domain

import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.Garment

/**
 * A chart only becomes usable once it has been verified for a specific garment.
 * The MVP has no verified charts, so no fit claims are made.
 */
data class VerifiedSizeChart(
    val garmentId: String,
    val source: String,
    val verifiedAtEpochMillis: Long,
    val sizeGuides: Map<String, SizeGuide>,
)

data class SizeGuide(
    val sizeId: String,
    val ranges: Map<MeasurementKey, ClosedFloatingPointRange<Float>>,
)

enum class MeasurementKey {
    CHEST,
    WAIST,
    HIPS,
    SHOULDER,
    INSEAM,
}

sealed interface FitRecommendation {
    /** Only possible when a verified chart exists. Not produced by the MVP engine. */
    data class Available(
        val sizeId: String,
        val summary: String,
    ) : FitRecommendation

    data object MeasurementsRequired : FitRecommendation

    data class Unavailable(val reason: String) : FitRecommendation
}

interface FitRecommendationEngine {
    suspend fun evaluate(
        measurements: BodyMeasurements?,
        garment: Garment,
        colorId: String?,
        sizeId: String?,
    ): FitRecommendation
}

/**
 * Honest default: no verified garment charts exist in this demo, therefore no
 * recommendation is produced. Replace with a calibrated implementation later.
 */
class UnavailableFitRecommendationEngine : FitRecommendationEngine {
    override suspend fun evaluate(
        measurements: BodyMeasurements?,
        garment: Garment,
        colorId: String?,
        sizeId: String?,
    ): FitRecommendation = if (measurements == null) {
        FitRecommendation.MeasurementsRequired
    } else {
        FitRecommendation.Unavailable(
            reason = "No verified size chart is available for this piece yet.",
        )
    }
}
