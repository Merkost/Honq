package com.merkost.honq.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ThemePreferences {
    val isDarkTheme: StateFlow<Boolean>
    fun setDarkTheme(isDark: Boolean)
}

class InMemoryThemePreferences : ThemePreferences {
    private val _isDarkTheme = MutableStateFlow(true)
    override val isDarkTheme: StateFlow<Boolean> = _isDarkTheme
    override fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
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
