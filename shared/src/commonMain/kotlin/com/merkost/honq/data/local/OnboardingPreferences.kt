package com.merkost.honq.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromString(value: String?): ThemeMode = when (value) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> SYSTEM
        }
    }

    fun toStorageString(): String = when (this) {
        SYSTEM -> "system"
        LIGHT -> "light"
        DARK -> "dark"
    }
}

interface ThemePreferences {
    val themeMode: StateFlow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
}

class InMemoryThemePreferences : ThemePreferences {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode: StateFlow<ThemeMode> = _themeMode
    override fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}

interface OnboardingPreferences {
    val isOnboardingCompleted: StateFlow<Boolean?>
    fun setOnboardingCompleted(completed: Boolean)
    fun getSelectedStateId(): String?
    fun setSelectedStateId(stateId: String)
    fun getSelectedLicenseTypeId(): String?
    fun setSelectedLicenseTypeId(typeId: String)
}

class InMemoryOnboardingPreferences : OnboardingPreferences {
    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    override val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted

    private var selectedStateId: String? = null
    private var selectedLicenseTypeId: String? = null

    override fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }

    override fun getSelectedStateId(): String? = selectedStateId

    override fun setSelectedStateId(stateId: String) {
        selectedStateId = stateId
    }

    override fun getSelectedLicenseTypeId(): String? = selectedLicenseTypeId

    override fun setSelectedLicenseTypeId(typeId: String) {
        selectedLicenseTypeId = typeId
    }
}
