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

enum class FontScale(val scale: Float, val label: String) {
    SMALL(0.85f, "Small"),
    DEFAULT(1.0f, "Default"),
    LARGE(1.15f, "Large"),
    EXTRA_LARGE(1.3f, "Extra Large");

    companion object {
        fun fromString(value: String?): FontScale = entries.find { it.name == value } ?: DEFAULT
    }
}

interface ThemePreferences {
    val themeMode: StateFlow<ThemeMode>
    val fontScale: StateFlow<FontScale>
    fun setThemeMode(mode: ThemeMode)
    fun setFontScale(scale: FontScale)
}

class InMemoryThemePreferences : ThemePreferences {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode: StateFlow<ThemeMode> = _themeMode
    private val _fontScale = MutableStateFlow(FontScale.DEFAULT)
    override val fontScale: StateFlow<FontScale> = _fontScale
    override fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
    override fun setFontScale(scale: FontScale) {
        _fontScale.value = scale
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
