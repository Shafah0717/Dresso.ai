package com.dresso.app.core.threeD

import com.dresso.app.data.model.BodyMeasurements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyMorphMappingTest {
    @Test
    fun blenderCalibrationsAndRangesAreIndependent() {
        assertEquals(165.9f, BodyMorphControl.HEIGHT.cmAtWeight(0f), 0.001f)
        assertEquals(49.7f, BodyMorphControl.SHOULDER.cmAtWeight(0f), 0.001f)
        assertEquals(107.9f, BodyMorphControl.CHEST.cmAtWeight(0f), 0.001f)
        assertEquals(74.6f, BodyMorphControl.BELLY.cmAtWeight(0f), 0.001f)
        assertEquals(67.6f, BodyMorphControl.WAIST.cmAtWeight(0f), 0.001f)
        assertEquals(93.8f, BodyMorphControl.HIPS.cmAtWeight(0f), 0.001f)
        assertEquals(5.5f, BodyMorphControl.BELLY_PROJECTION.cmAtWeight(1f), 0.001f)

        val body = BodyMeasurements(heightCm = 170, bellyCm = 80, bellyProjectionCm = 2.2f)
        assertTrue(BodyMorphControl.HEIGHT.weightFor(body) > 0f)
        assertTrue(BodyMorphControl.BELLY.weightFor(body) > 0f)
        assertEquals(0.4f, BodyMorphControl.BELLY_PROJECTION.weightFor(body), 0.0001f)
        assertEquals(0f, BodyMorphControl.CHEST.weightFor(body), 0f)
        assertEquals(0f, BodyMorphControl.SHOULDER.weightFor(body), 0f)
        assertEquals(
            BodyMorphControl.BELLY.weightFor(body),
            BodyMorphControl.BELLY.weightFor(body.copy(heightCm = 160)),
            0f,
        )
    }

    @Test
    fun invalidAndExtremeValuesCannotEscapeTheArtistRecommendedRange() {
        assertEquals(BodyMorphControl.MAX_WEIGHT, BodyMorphControl.CHEST.weightFor(
            BodyMeasurements(chestCm = 300)), 0f)
        assertEquals(BodyMorphControl.MIN_WEIGHT, BodyMorphControl.BELLY.weightFor(
            BodyMeasurements(bellyCm = 1)), 0f)
        assertEquals(0f, BodyMorphControl.BELLY_PROJECTION.weightFor(
            BodyMeasurements(bellyProjectionCm = Float.NaN)), 0f)
        assertEquals(0f, BodyMorphControl.BELLY_PROJECTION.weightFor(
            BodyMeasurements(bellyProjectionCm = Float.POSITIVE_INFINITY)), 0f)
        assertEquals(0f, BodyMorphControl.HEIGHT.weightFor(null), 0f)
        assertEquals(0f, BodyMorphControl.HEIGHT.weightFor(BodyMeasurements(heightCm = 0)), 0f)
    }
}
