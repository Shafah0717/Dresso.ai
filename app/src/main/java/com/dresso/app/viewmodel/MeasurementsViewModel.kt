package com.dresso.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dresso.app.AppContainer
import com.dresso.app.data.model.BodyMeasurements
import com.dresso.app.data.model.MeasurementField
import com.dresso.app.data.model.UnitSystem
import com.dresso.app.data.repository.BodyProfileRepository
import com.dresso.app.data.repository.UserPreferencesRepository
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementsUiState(
    val fields: Map<MeasurementField, String> = MeasurementField.entries.associateWith { "" },
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val fieldErrors: Map<MeasurementField, String> = emptyMap(),
    val existingMeasurements: BodyMeasurements? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val generalMessage: String? = null,
    val showClearConfirmation: Boolean = false,
) {
    val completedFields: Int
        get() = fields.values.count { it.isNotBlank() }
}

class MeasurementsViewModel(
    private val bodyProfileRepository: BodyProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeasurementsUiState())
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val preferences = userPreferencesRepository.preferences.first()
            val saved = bodyProfileRepository.measurements.first()
            _uiState.value = MeasurementsUiState(
                fields = saved.toDisplayFields(preferences.unitSystem),
                unitSystem = preferences.unitSystem,
                existingMeasurements = saved,
                isLoading = false,
            )
        }
    }

    fun onFieldChange(field: MeasurementField, rawValue: String) {
        val value = rawValue.filter(Char::isDigit).take(3)
        _uiState.update { state ->
            state.copy(
                fields = state.fields + (field to value),
                fieldErrors = state.fieldErrors - field,
                generalMessage = null,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val errors = mutableMapOf<MeasurementField, String>()
        var measurements = BodyMeasurements(
            bellyProjectionCm = state.existingMeasurements?.bellyProjectionCm,
        )

        MeasurementField.entries.forEach { field ->
            val text = state.fields[field].orEmpty().trim()
            if (text.isEmpty()) return@forEach
            val displayValue = text.toIntOrNull()
            if (displayValue == null) {
                errors[field] = "Enter a number."
                return@forEach
            }
            val minDisplay = state.unitSystem.displayMin(field)
            val maxDisplay = state.unitSystem.displayMax(field)
            if (displayValue !in minDisplay..maxDisplay) {
                errors[field] = "Use $minDisplay–$maxDisplay ${state.unitSystem.suffix}."
                return@forEach
            }
            val valueCm = state.unitSystem.toCentimetres(displayValue)
            if (valueCm !in field.minCm..field.maxCm) {
                errors[field] = "Use $minDisplay–$maxDisplay ${state.unitSystem.suffix}."
                return@forEach
            }
            measurements = measurements.withValue(field, valueCm)
        }

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    fieldErrors = errors,
                    generalMessage = "Check the highlighted measurements.",
                )
            }
            return
        }

        if (!measurements.hasAny) {
            _uiState.update {
                it.copy(generalMessage = "Enter at least one measurement, or clear your profile.")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, generalMessage = null) }
        viewModelScope.launch {
            val saved = measurements.copy(updatedAtEpochMillis = System.currentTimeMillis())
            runCatching { bodyProfileRepository.save(saved) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedSuccessfully = true,
                            existingMeasurements = saved,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            generalMessage = "Could not save to this device. Please try again.",
                        )
                    }
                }
        }
    }

    fun onSavedHandled() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }

    fun requestClear() {
        _uiState.update { it.copy(showClearConfirmation = true) }
    }

    fun dismissClear() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    fun clearAll() {
        viewModelScope.launch {
            bodyProfileRepository.clear()
            _uiState.update {
                MeasurementsUiState(
                    fields = MeasurementField.entries.associateWith { "" },
                    unitSystem = it.unitSystem,
                    existingMeasurements = null,
                    isLoading = false,
                    generalMessage = "Measurements cleared from this device.",
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeasurementsViewModel(
                    bodyProfileRepository = container.bodyProfileRepository,
                    userPreferencesRepository = container.userPreferencesRepository,
                )
            }
        }
    }
}

private fun BodyMeasurements?.toDisplayFields(unitSystem: UnitSystem): Map<MeasurementField, String> =
    MeasurementField.entries.associateWith { field ->
        val value = this?.valueFor(field) ?: return@associateWith ""
        unitSystem.fromCentimetres(value).toString()
    }

private fun UnitSystem.fromCentimetres(valueCm: Int): Int =
    when (this) {
        UnitSystem.METRIC -> valueCm
        UnitSystem.IMPERIAL -> (valueCm / 2.54f).roundToInt()
    }

private fun UnitSystem.toCentimetres(value: Int): Int =
    when (this) {
        UnitSystem.METRIC -> value
        UnitSystem.IMPERIAL -> (value * 2.54f).roundToInt()
    }

private fun UnitSystem.displayMin(field: MeasurementField): Int =
    when (this) {
        UnitSystem.METRIC -> field.minCm
        UnitSystem.IMPERIAL -> ceil(field.minCm / 2.54f).toInt()
    }

private fun UnitSystem.displayMax(field: MeasurementField): Int =
    when (this) {
        UnitSystem.METRIC -> field.maxCm
        UnitSystem.IMPERIAL -> floor(field.maxCm / 2.54f).toInt()
    }
