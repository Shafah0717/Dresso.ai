package com.dresso.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dresso.app.AppContainer
import com.dresso.app.core.threeD.BodyMorphControl
import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.Brand
import com.dresso.app.data.model.Garment
import com.dresso.app.data.model.GarmentColor
import com.dresso.app.data.model.GarmentSize
import com.dresso.app.data.model.UnitSystem
import com.dresso.app.data.model.UserPreferences
import com.dresso.app.data.repository.BodyProfileRepository
import com.dresso.app.data.repository.BrandRepository
import com.dresso.app.data.repository.UserPreferencesRepository
import com.dresso.app.domain.FitRecommendation
import com.dresso.app.domain.FitRecommendationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface BodyProfileState {
    data object Loading : BodyProfileState
    data class Ready(val measurements: BodyMeasurements?) : BodyProfileState
}

data class BrandPageState(
    val brandId: String? = null,
    val brand: Brand? = null,
    val garments: List<Garment> = emptyList(),
    val isLoading: Boolean = true,
)

data class GarmentPageState(
    val productId: String? = null,
    val garment: Garment? = null,
    val brand: Brand? = null,
    val isLoading: Boolean = true,
)

data class SelectedLook(
    val garment: Garment,
    val brand: Brand?,
    val color: GarmentColor?,
    val size: GarmentSize?,
)

/**
 * Activity-scoped application state. Selections live here so product, try-on and
 * back navigation all share the same source of truth without serialising objects.
 */
class DressoViewModel(
    private val brandRepository: BrandRepository,
    private val bodyProfileRepository: BodyProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fitRecommendationEngine: FitRecommendationEngine,
) : ViewModel() {

    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()

    private val _brandPage = MutableStateFlow(BrandPageState())
    val brandPage: StateFlow<BrandPageState> = _brandPage.asStateFlow()

    private val _garmentPage = MutableStateFlow(GarmentPageState())
    val garmentPage: StateFlow<GarmentPageState> = _garmentPage.asStateFlow()

    private val _selectedLook = MutableStateFlow<SelectedLook?>(null)
    val selectedLook: StateFlow<SelectedLook?> = _selectedLook.asStateFlow()

    val bodyProfile: StateFlow<BodyProfileState> =
        bodyProfileRepository.measurements
            .map<BodyMeasurements?, BodyProfileState> { BodyProfileState.Ready(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BodyProfileState.Loading,
            )

    private val _avatarDraft = MutableStateFlow<BodyMeasurements?>(null)
    private val avatarSaveMutex = Mutex()

    /** A local live preview while dragging; DataStore remains the persisted source of truth. */
    val avatarMeasurements: StateFlow<BodyMeasurements?> =
        combine(bodyProfile, _avatarDraft) { profile, draft ->
            draft ?: (profile as? BodyProfileState.Ready)?.measurements
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val preferences: StateFlow<UserPreferences> =
        userPreferencesRepository.preferences
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UserPreferences(),
            )

    val fitRecommendation: StateFlow<FitRecommendation?> =
        combine(_selectedLook, bodyProfile) { look, profile ->
            look to ((profile as? BodyProfileState.Ready)?.measurements)
        }
            .map { (look, measurements) ->
                if (look == null) {
                    null
                } else {
                    fitRecommendationEngine.evaluate(
                        measurements = measurements,
                        garment = look.garment,
                        colorId = look.color?.id,
                        sizeId = look.size?.id,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    init {
        viewModelScope.launch {
            _brands.value = brandRepository.getBrands()
        }
    }

    fun loadBrand(brandId: String) {
        if (_brandPage.value.brand?.id == brandId && !_brandPage.value.isLoading) return
        _brandPage.value = BrandPageState(brandId = brandId, isLoading = true)
        viewModelScope.launch {
            val brand = brandRepository.getBrand(brandId)
            val garments = if (brand == null) emptyList() else brandRepository.getGarments(brandId)
            if (_brandPage.value.brandId == brandId) {
                _brandPage.value = BrandPageState(
                    brandId = brandId,
                    brand = brand,
                    garments = garments,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Opening the same product again preserves the chosen colour and size.
     * Opening a different product starts with its first colour and no size.
     */
    fun openGarment(garmentId: String) {
        val existingLook = _selectedLook.value
        if (existingLook?.garment?.id == garmentId) {
            _garmentPage.value = GarmentPageState(
                productId = garmentId,
                garment = existingLook.garment,
                brand = existingLook.brand,
                isLoading = false,
            )
            return
        }

        _garmentPage.value = GarmentPageState(productId = garmentId, isLoading = true)
        viewModelScope.launch {
            val garment = brandRepository.getGarment(garmentId) ?: return@launch
            if (_garmentPage.value.productId != garmentId) return@launch
            val brand = brandRepository.getBrand(garment.brandId)
            _garmentPage.value = GarmentPageState(
                productId = garmentId,
                garment = garment,
                brand = brand,
                isLoading = false,
            )
            _selectedLook.value = SelectedLook(
                garment = garment,
                brand = brand,
                color = garment.colors.firstOrNull(),
                size = null,
            )
        }
    }

    fun selectColor(colorId: String) {
        _selectedLook.update { look ->
            if (look == null) return@update look
            val color = look.garment.colors.firstOrNull { it.id == colorId } ?: return@update look
            look.copy(color = color)
        }
    }

    fun selectSize(sizeId: String) {
        _selectedLook.update { look ->
            if (look == null) return@update look
            val size = look.garment.sizes.firstOrNull { it.id == sizeId } ?: return@update look
            look.copy(size = size)
        }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            userPreferencesRepository.setUnitSystem(unitSystem)
        }
    }

    fun updateAvatarControl(control: BodyMorphControl, centimetres: Float) {
        if (bodyProfile.value !is BodyProfileState.Ready || !centimetres.isFinite()) return
        val current = _avatarDraft.value
            ?: (bodyProfile.value as BodyProfileState.Ready).measurements
            ?: BodyMeasurements()
        _avatarDraft.value = control.withValue(
            current,
            centimetres.coerceIn(control.minCm, control.maxCm),
        )
    }

    fun saveAvatarMeasurements() {
        viewModelScope.launch {
            avatarSaveMutex.withLock {
                val draft = _avatarDraft.value ?: return@withLock
                runCatching {
                    bodyProfileRepository.save(
                        draft.copy(updatedAtEpochMillis = System.currentTimeMillis()),
                    )
                }.onFailure { error -> Log.e("DressoAvatar", "Could not save body preview", error) }
            }
        }
    }

    /** Let the measurement editor's saved values replace the fitting-room preview. */
    fun clearAvatarDraft() {
        _avatarDraft.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DressoViewModel(
                    brandRepository = container.brandRepository,
                    bodyProfileRepository = container.bodyProfileRepository,
                    userPreferencesRepository = container.userPreferencesRepository,
                    fitRecommendationEngine = container.fitRecommendationEngine,
                )
            }
        }
    }
}
