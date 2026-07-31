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
import kotlin.time.TimeSource
import org.kimplify.cedar.logging.Cedar
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private const val SYNC_ERROR_MESSAGE = "Couldn't load content. Check your connection and try again."

private val OnboardingStep.analyticsName: String
    get() = when (this) {
        OnboardingStep.Welcome -> "welcome"
        OnboardingStep.StateSelection -> "state_selection"
        OnboardingStep.LicenseTypeSelection -> "license_selection"
    }

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
            analytics.track(AnalyticsEvent.OnboardingStepViewed(OnboardingStep.Welcome.analyticsName))
            loadData()
        }

        reduce { intent ->
            when (intent) {
                OnboardingIntent.GetStarted -> {
                    updateState { copy(currentStep = OnboardingStep.StateSelection) }
                    withState {
                        when {
                            error != null && !isLoading -> loadData()
                            !isLoading -> analytics.track(
                                AnalyticsEvent.OnboardingStepViewed(OnboardingStep.StateSelection.analyticsName)
                            )
                        }
                    }
                }
                is OnboardingIntent.SelectState -> {
                    updateState { copy(selectedStateId = intent.stateId) }
                }
                OnboardingIntent.ConfirmStateSelection -> {
                    analytics.track(
                        AnalyticsEvent.OnboardingStepViewed(OnboardingStep.LicenseTypeSelection.analyticsName)
                    )
                    updateState { copy(currentStep = OnboardingStep.LicenseTypeSelection) }
                }
                is OnboardingIntent.SelectLicenseType -> {
                    updateState { copy(selectedLicenseTypeId = intent.typeId) }
                }
                OnboardingIntent.CompleteOnboarding -> {
                    completeOnboarding()
                }
                OnboardingIntent.RetryCompletion -> {
                    completeOnboarding()
                }
                OnboardingIntent.GoBack -> {
                    goBack()
                }
                OnboardingIntent.Retry -> {
                    Cedar.tag("Onboarding").i("Retry triggered by user")
                    analytics.track(AnalyticsEvent.OnboardingRetryClicked)
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
        var networkSyncMs = 0L
        var didNetworkSync = false

        suspend fun timedSync(): Result<Unit> {
            didNetworkSync = true
            val start = TimeSource.Monotonic.markNow()
            val result = runSync()
            networkSyncMs += start.elapsedNow().inWholeMilliseconds
            return result
        }

        suspend fun failWith(reason: String?) {
            analytics.track(AnalyticsEvent.OnboardingSyncFailed(reason, networkSyncMs))
            updateState { copy(isLoading = false, error = SYNC_ERROR_MESSAGE) }
        }

        if (dataSyncManager.needsInitialSync()) {
            Cedar.tag("Onboarding").d("loadData: first launch, running full sync")
            val syncResult = timedSync()
            if (syncResult is Result.Error) {
                Cedar.tag("Onboarding").e("loadData: initial sync failed: ${syncResult.exception.message}", syncResult.exception)
                failWith(syncResult.exception.message)
                return
            }
        }

        var states = getStates().getOrNull().orEmpty()

        if (states.isEmpty()) {
            Cedar.tag("Onboarding").w("loadData: states empty after read, re-running sync")
            val retryResult = timedSync()
            if (retryResult is Result.Error) {
                Cedar.tag("Onboarding").e("loadData: retry sync failed: ${retryResult.exception.message}", retryResult.exception)
                failWith(retryResult.exception.message)
                return
            }
            states = getStates().getOrNull().orEmpty()
            if (states.isEmpty()) {
                Cedar.tag("Onboarding").e("loadData: states still empty after successful sync")
                failWith("states empty after sync")
                return
            }
        }

        Cedar.tag("Onboarding").d("loadData: loaded ${states.size} states")
        updateState { copy(states = states) }

        getLicenseTypes()
            .onSuccess { types ->
                val activeTypes = types.filter { it.isActive }.sortedBy { it.displayOrder }
                Cedar.tag("Onboarding").d("loadData: loaded ${activeTypes.size} active license types")
                if (didNetworkSync) {
                    analytics.track(AnalyticsEvent.OnboardingSyncCompleted(networkSyncMs))
                }
                updateState { copy(licenseTypes = activeTypes, isLoading = false) }
                withState {
                    if (currentStep != OnboardingStep.Welcome) {
                        analytics.track(AnalyticsEvent.OnboardingStepViewed(currentStep.analyticsName))
                    }
                }
            }
            .onError { e ->
                Cedar.tag("Onboarding").e("loadData: failed to load license types: ${e.message}", e)
                failWith(e.message)
            }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.completeOnboarding() {
        var selectedStateId: String? = null
        var selectedLicenseTypeId: String? = null
        withState {
            selectedStateId = this.selectedStateId
            selectedLicenseTypeId = this.selectedLicenseTypeId
        }
        if (selectedStateId == null || selectedLicenseTypeId == null) {
            Cedar.tag("Onboarding").w(
                "completeOnboarding: missing selection (state=$selectedStateId, type=$selectedLicenseTypeId)"
            )
            return
        }
        val stateId = checkNotNull(selectedStateId)
        val licenseTypeId = checkNotNull(selectedLicenseTypeId)
        updateState { copy(isCompleting = true, completionError = null) }

        onboardingPreferences.setSelectedStateId(stateId)
        onboardingPreferences.setSelectedLicenseTypeId(licenseTypeId)

        analytics.track(
            AnalyticsEvent.OnboardingCompleted(
                stateId = stateId,
                licenseTypeId = licenseTypeId
            )
        )

        Cedar.tag("Onboarding").d("completeOnboarding: state=$stateId, type=$licenseTypeId")
        getQuestionSetsByState(stateId)
            .onSuccess { questionSets ->
                val matchingSet = questionSets.firstOrNull {
                    it.isActive && it.licenseTypeId == licenseTypeId
                } ?: questionSets.firstOrNull { it.isActive }

                Cedar.tag("Onboarding").d("completeOnboarding: selected questionSet=${matchingSet?.id}")
                matchingSet?.let { setSelectedQuestionSet(it.id) }

                onboardingPreferences.setOnboardingCompleted(true)
                action(OnboardingAction.NavigateToHome)
            }
            .onError { e ->
                Cedar.tag("Onboarding").e("completeOnboarding: failed to load question sets: ${e.message}", e)
                updateState { copy(isCompleting = false, completionError = SYNC_ERROR_MESSAGE) }
            }
    }

    private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.goBack() {
        withState {
            val previousStep = when (currentStep) {
                OnboardingStep.Welcome -> OnboardingStep.Welcome
                OnboardingStep.StateSelection -> OnboardingStep.Welcome
                OnboardingStep.LicenseTypeSelection -> OnboardingStep.StateSelection
            }
            if (previousStep != currentStep) {
                analytics.track(AnalyticsEvent.OnboardingStepViewed(previousStep.analyticsName))
            }
            updateState { copy(currentStep = previousStep) }
        }
    }
}
