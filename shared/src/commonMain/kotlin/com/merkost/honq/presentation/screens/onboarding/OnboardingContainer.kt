package com.merkost.honq.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.Result
import com.merkost.honq.core.util.getOrNull
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.data.repository.DataSyncManager
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.usecase.GetLicenseTypesUseCase
import com.merkost.honq.domain.usecase.GetStatesUseCase
import com.merkost.honq.domain.usecase.SetSelectedQuestionSetUseCase
import com.merkost.honq.domain.usecase.GetQuestionSetsByStateUseCase
import kotlinx.coroutines.CoroutineScope
import org.kimplify.cedar.logging.Cedar
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
    private val dataSyncManager: DataSyncManager,
    private val repository: QuestionRepository,
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
                OnboardingIntent.Retry -> {
                    Cedar.tag("Onboarding").i("Retry triggered by user")
                    loadData()
                }
            }
        }
    }

    private suspend fun runSync(): Result<Unit> {
        val remoteVersion = dataSyncManager.fetchRemoteVersion().getOrDefault(0)
        val syncResult = repository.fullSync(null)
        if (syncResult is Result.Success) {
            dataSyncManager.markSyncCompleted(remoteVersion)
        }
        return syncResult
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.loadData() {
        Cedar.tag("Onboarding").d("loadData: starting...")
        updateState { copy(isLoading = true, error = null) }

        if (dataSyncManager.needsInitialSync()) {
            Cedar.tag("Onboarding").d("loadData: first launch, running full sync")
            val syncResult = runSync()
            if (syncResult is Result.Error) {
                Cedar.tag("Onboarding").e("loadData: initial sync failed: ${syncResult.exception.message}", syncResult.exception)
                updateState {
                    copy(
                        isLoading = false,
                        error = "Couldn't load content. Check your connection and try again."
                    )
                }
                return
            }
        }

        var states = getStates().getOrNull().orEmpty()

        if (states.isEmpty()) {
            Cedar.tag("Onboarding").w("loadData: states empty after read, re-running sync")
            val retryResult = runSync()
            if (retryResult is Result.Error) {
                Cedar.tag("Onboarding").e("loadData: retry sync failed: ${retryResult.exception.message}", retryResult.exception)
                updateState {
                    copy(
                        isLoading = false,
                        error = "Couldn't load content. Check your connection and try again."
                    )
                }
                return
            }
            states = getStates().getOrNull().orEmpty()
        }

        Cedar.tag("Onboarding").d("loadData: loaded ${states.size} states")
        updateState { copy(states = states) }

        getLicenseTypes()
            .onSuccess { types ->
                val activeTypes = types.filter { it.isActive }.sortedBy { it.displayOrder }
                Cedar.tag("Onboarding").d("loadData: loaded ${activeTypes.size} active license types")
                updateState { copy(licenseTypes = activeTypes, isLoading = false) }
            }
            .onError { e ->
                Cedar.tag("Onboarding").e("loadData: failed to load license types: ${e.message}", e)
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load license types") }
            }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.completeOnboarding() {
        withState {
            val stateId = selectedStateId
            val typeId = selectedLicenseTypeId
            if (stateId == null || typeId == null) {
                Cedar.tag("Onboarding").w("completeOnboarding: missing selection (state=$stateId, type=$typeId)")
                return@withState
            }

            onboardingPreferences.setSelectedStateId(stateId)
            onboardingPreferences.setSelectedLicenseTypeId(typeId)

            analytics.track(
                AnalyticsEvent.OnboardingCompleted(
                    stateId = stateId,
                    licenseTypeId = typeId
                )
            )

            Cedar.tag("Onboarding").d("completeOnboarding: state=$stateId, type=$typeId")
            getQuestionSetsByState(stateId)
                .onSuccess { questionSets ->
                    val matchingSet = questionSets.firstOrNull {
                        it.isActive && it.licenseTypeId == typeId
                    } ?: questionSets.firstOrNull { it.isActive }

                    Cedar.tag("Onboarding").d("completeOnboarding: selected questionSet=${matchingSet?.id}")
                    matchingSet?.let { setSelectedQuestionSet(it.id) }

                    onboardingPreferences.setOnboardingCompleted(true)
                    action(OnboardingAction.NavigateToHome)
                }
                .onError { e ->
                    Cedar.tag("Onboarding").e("completeOnboarding: failed to load question sets, proceeding anyway: ${e.message}", e)
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
