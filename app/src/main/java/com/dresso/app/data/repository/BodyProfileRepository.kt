package com.dresso.app.data.repository

import com.dresso.app.data.model.BodyMeasurements
import kotlinx.coroutines.flow.Flow

/**
 * Device-local body profile. Implementations must not transmit measurements.
 */
interface BodyProfileRepository {
    val measurements: Flow<BodyMeasurements?>

    suspend fun save(measurements: BodyMeasurements)

    suspend fun clear()
}
