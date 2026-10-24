package com.dresso.app.data.repository

import com.dresso.app.data.model.UnitSystem
import com.dresso.app.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setUnitSystem(unitSystem: UnitSystem)
}
