package com.dresso.app.data.model

enum class UnitSystem(
    val label: String,
    val suffix: String,
) {
    METRIC("Metric", "cm"),
    IMPERIAL("Imperial", "in"),
}

data class UserPreferences(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
)
