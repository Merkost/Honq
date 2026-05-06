package com.merkost.honq.presentation.screens.onboarding

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.State
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val states: List<State> = emptyList(),
    val selectedStateId: String? = null,
    val licenseTypes: List<LicenseType> = emptyList(),
    val selectedLicenseTypeId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState {
    val selectedState: State?
        get() = states.firstOrNull { it.id == selectedStateId }

    val selectedLicenseType: LicenseType?
        get() = licenseTypes.firstOrNull { it.id == selectedLicenseTypeId }

    val canProceedFromStateSelection: Boolean
        get() = selectedStateId != null

    val canProceedFromLicenseTypeSelection: Boolean
        get() = selectedLicenseTypeId != null
}

enum class OnboardingStep {
    Welcome,
    StateSelection,
    LicenseTypeSelection
}

sealed interface OnboardingIntent : MVIIntent {
    data object GetStarted : OnboardingIntent
    data class SelectState(val stateId: String) : OnboardingIntent
    data object ConfirmStateSelection : OnboardingIntent
    data class SelectLicenseType(val typeId: String) : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
    data object GoBack : OnboardingIntent
    data object Retry : OnboardingIntent
}

sealed interface OnboardingAction : MVIAction {
    data object NavigateToHome : OnboardingAction
}
