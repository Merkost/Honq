package com.merkost.honq.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.domain.usecase.GetLicenseTypesUseCase
import com.merkost.honq.domain.usecase.GetStatesUseCase
import com.merkost.honq.domain.usecase.SetSelectedQuestionSetUseCase
import com.merkost.honq.domain.usecase.GetQuestionSetsByStateUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class OnboardingContainer(
    private val getStates: GetStatesUseCase,
    private val getLicenseTypes: GetLicenseTypesUseCase,
    private val getQuestionSetsByState: GetQuestionSetsByStateUseCase,
    private val setSelectedQuestionSet: SetSelectedQuestionSetUseCase,
    private val onboardingPreferences: OnboardingPreferences,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<OnboardingState, OnboardingIntent, OnboardingAction>, ViewModel() {

    override val store = store(OnboardingState(), scope) {
        init {
            analytics.track(AnalyticsEvent.OnboardingStarted)
            loadData()
        }

        reduce { intent ->
            when (intent) {
                OnboardingIntent.GetStarted -> {
                    updateState { copy(currentStep = OnboardingStep.StateSelection) }
                }
                is OnboardingIntent.SelectState -> {
                    updateState { copy(selectedStateId = intent.stateId) }
                }
                OnboardingIntent.ConfirmStateSelection -> {
                    updateState { copy(currentStep = OnboardingStep.LicenseTypeSelection) }
                }
                is OnboardingIntent.SelectLicenseType -> {
                    updateState { copy(selectedLicenseTypeId = intent.typeId) }
                }
                OnboardingIntent.CompleteOnboarding -> {
                    completeOnboarding()
                }
                OnboardingIntent.GoBack -> {
                    goBack()
                }
            }
        }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.loadData() {
        updateState { copy(isLoading = true, error = null) }

        getStates()
            .onSuccess { states ->
                updateState { copy(states = states) }
            }
            .onError { e ->
                updateState { copy(error = e.message ?: "Failed to load states") }
            }

        getLicenseTypes()
            .onSuccess { types ->
                val activeTypes = types.filter { it.isActive }.sortedBy { it.displayOrder }
                updateState { copy(licenseTypes = activeTypes, isLoading = false) }
            }
            .onError { e ->
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load license types") }
            }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.completeOnboarding() {
        withState {
            val stateId = selectedStateId ?: return@withState
            val typeId = selectedLicenseTypeId ?: return@withState

            onboardingPreferences.setSelectedStateId(stateId)
            onboardingPreferences.setSelectedLicenseTypeId(typeId)

            analytics.track(
                AnalyticsEvent.OnboardingCompleted(
                    stateId = stateId,
                    licenseTypeId = typeId
                )
            )

            getQuestionSetsByState(stateId)
                .onSuccess { questionSets ->
                    val matchingSet = questionSets.firstOrNull {
                        it.isActive && it.licenseTypeId == typeId
                    } ?: questionSets.firstOrNull { it.isActive }

                    matchingSet?.let { setSelectedQuestionSet(it.id) }

                    onboardingPreferences.setOnboardingCompleted(true)
                    action(OnboardingAction.NavigateToHome)
                }
                .onError {
                    onboardingPreferences.setOnboardingCompleted(true)
                    action(OnboardingAction.NavigateToHome)
                }
        }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.goBack() {
        withState {
            val previousStep = when (currentStep) {
                OnboardingStep.Welcome -> OnboardingStep.Welcome
                OnboardingStep.StateSelection -> OnboardingStep.Welcome
                OnboardingStep.LicenseTypeSelection -> OnboardingStep.StateSelection
            }
            updateState { copy(currentStep = previousStep) }
        }
    }
}
