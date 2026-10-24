package com.dresso.app

import android.content.Context
import com.dresso.app.core.threeD.AvatarRenderer
import com.dresso.app.core.threeD.BodyShapeAdapter
import com.dresso.app.core.threeD.CalibrationPendingBodyShapeAdapter
import com.dresso.app.core.threeD.PendingAvatarRenderer
import com.dresso.app.data.repository.BodyProfileRepository
import com.dresso.app.data.repository.BrandRepository
import com.dresso.app.data.repository.DataStoreBodyProfileRepository
import com.dresso.app.data.repository.DataStoreUserPreferencesRepository
import com.dresso.app.data.repository.MockBrandRepository
import com.dresso.app.data.repository.UserPreferencesRepository
import com.dresso.app.domain.FitRecommendationEngine
import com.dresso.app.domain.UnavailableFitRecommendationEngine

interface AppContainer {
    val brandRepository: BrandRepository
    val bodyProfileRepository: BodyProfileRepository
    val userPreferencesRepository: UserPreferencesRepository
    val fitRecommendationEngine: FitRecommendationEngine
    val bodyShapeAdapter: BodyShapeAdapter
    val avatarRenderer: AvatarRenderer
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext

    override val brandRepository: BrandRepository = MockBrandRepository()
    override val bodyProfileRepository: BodyProfileRepository =
        DataStoreBodyProfileRepository(appContext)
    override val userPreferencesRepository: UserPreferencesRepository =
        DataStoreUserPreferencesRepository(appContext)
    override val fitRecommendationEngine: FitRecommendationEngine =
        UnavailableFitRecommendationEngine()
    override val bodyShapeAdapter: BodyShapeAdapter =
        CalibrationPendingBodyShapeAdapter()
    override val avatarRenderer: AvatarRenderer = PendingAvatarRenderer()
}
