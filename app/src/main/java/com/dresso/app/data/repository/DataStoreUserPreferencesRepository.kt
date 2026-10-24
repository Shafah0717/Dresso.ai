package com.dresso.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dresso.app.data.model.UnitSystem
import com.dresso.app.data.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dressoPreferencesDataStore by preferencesDataStore(name = "dresso_preferences")

class DataStoreUserPreferencesRepository(
    private val context: Context,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> =
        context.dressoPreferencesDataStore.data
            .catch { error ->
                if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
            }
            .map { stored ->
                val unitSystem = stored[Keys.unitSystem]
                    ?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() }
                    ?: UnitSystem.METRIC
                UserPreferences(unitSystem = unitSystem)
            }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dressoPreferencesDataStore.edit { it[Keys.unitSystem] = unitSystem.name }
    }

    private object Keys {
        val unitSystem = stringPreferencesKey("unit_system")
    }
}
