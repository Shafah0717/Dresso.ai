package com.dresso.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dresso.app.data.model.BodyMeasurements
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.bodyProfileDataStore by preferencesDataStore(name = "dresso_body_profile")

/**
 * Local-only profile storage. There is deliberately no network dependency here.
 */
class DataStoreBodyProfileRepository(
    private val context: Context,
) : BodyProfileRepository {

    override val measurements: Flow<BodyMeasurements?> =
        context.bodyProfileDataStore.data
            .catch { error ->
                if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
            }
            .map { preferences ->
                val hasAnyValue = Keys.all.any { preferences.contains(it) } ||
                    preferences.contains(Keys.bellyProjection)
                if (!hasAnyValue) {
                    null
                } else {
                    BodyMeasurements(
                        heightCm = preferences[Keys.height],
                        chestCm = preferences[Keys.chest],
                        bellyCm = preferences[Keys.belly],
                        waistCm = preferences[Keys.waist],
                        hipsCm = preferences[Keys.hips],
                        shoulderCm = preferences[Keys.shoulder],
                        inseamCm = preferences[Keys.inseam],
                        updatedAtEpochMillis = preferences[Keys.updatedAt],
                        bellyProjectionCm = preferences[Keys.bellyProjection],
                    )
                }
            }

    override suspend fun save(measurements: BodyMeasurements) {
        context.bodyProfileDataStore.edit { preferences ->
            preferences.writeNullable(Keys.height, measurements.heightCm)
            preferences.writeNullable(Keys.chest, measurements.chestCm)
            preferences.writeNullable(Keys.belly, measurements.bellyCm)
            preferences.writeNullable(Keys.waist, measurements.waistCm)
            preferences.writeNullable(Keys.hips, measurements.hipsCm)
            preferences.writeNullable(Keys.shoulder, measurements.shoulderCm)
            preferences.writeNullable(Keys.inseam, measurements.inseamCm)
            if (measurements.bellyProjectionCm == null) {
                preferences.remove(Keys.bellyProjection)
            } else {
                preferences[Keys.bellyProjection] = measurements.bellyProjectionCm
            }
            preferences[Keys.updatedAt] = System.currentTimeMillis()
        }
    }

    override suspend fun clear() {
        context.bodyProfileDataStore.edit { it.clear() }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.writeNullable(
        key: androidx.datastore.preferences.core.Preferences.Key<Int>,
        value: Int?,
    ) {
        if (value == null) remove(key) else this[key] = value
    }

    private object Keys {
        val height = intPreferencesKey("height_cm")
        val chest = intPreferencesKey("chest_cm")
        val belly = intPreferencesKey("belly_cm")
        val waist = intPreferencesKey("waist_cm")
        val hips = intPreferencesKey("hips_cm")
        val shoulder = intPreferencesKey("shoulder_cm")
        val inseam = intPreferencesKey("inseam_cm")
        val bellyProjection = floatPreferencesKey("belly_projection_cm")
        val updatedAt = longPreferencesKey("updated_at")

        val all = listOf(height, chest, belly, waist, hips, shoulder, inseam)
    }
}
