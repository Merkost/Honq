package com.merkost.honq.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
