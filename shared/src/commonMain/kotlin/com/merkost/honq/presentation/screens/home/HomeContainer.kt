package com.merkost.honq.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.usecase.GetLicenseTypesUseCase
import com.merkost.honq.domain.usecase.GetQuestionSetsByStateUseCase
import com.merkost.honq.domain.usecase.GetStateResourcesUseCase
import com.merkost.honq.domain.usecase.GetStatesUseCase
import com.merkost.honq.domain.usecase.GetUserProgressUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionsUseCase
import com.merkost.honq.domain.usecase.SetSelectedQuestionSetUseCase
import com.merkost.honq.domain.usecase.SyncQuestionsUseCase
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.whileSubscribed

class HomeContainer(
    private val getUserProgress: GetUserProgressUseCase,
    private val getStates: GetStatesUseCase,
    private val getLicenseTypes: GetLicenseTypesUseCase,
    private val getQuestionSetsByState: GetQuestionSetsByStateUseCase,
    private val getStateResources: GetStateResourcesUseCase,
    private val setSelectedQuestionSet: SetSelectedQuestionSetUseCase,
    private val syncQuestions: SyncQuestionsUseCase,
    private val observeFavoriteQuestions: ObserveFavoriteQuestionsUseCase,
    private val onboardingPreferences: OnboardingPreferences,
    private val analytics: Analytics
) : Container<HomeState, HomeIntent, HomeAction>, ViewModel() {

    override val store = store(HomeState(), viewModelScope) {
        init {
            loadInitialData()
        }

        whileSubscribed {
            getUserProgress().collect { progress ->
                updateState { copy(progress = progress) }
            }
        }

        whileSubscribed {
            observeFavoriteQuestions().collect { favorites ->
                updateState { copy(favoriteQuestions = favorites) }
            }
        }

        reduce { intent ->
            when (intent) {
                HomeIntent.StartPractice -> action(HomeAction.NavigateToPractice)
                HomeIntent.StartMockTest -> action(HomeAction.NavigateToMockTest)
                is HomeIntent.SelectState -> selectState(intent.stateId)
                is HomeIntent.SelectLicenseType -> selectLicenseType(intent.typeId)
                is HomeIntent.OpenExternalLink -> trackExternalLink(intent.linkType, intent.url)
                HomeIntent.Retry -> loadInitialData()
            }
        }
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.loadInitialData() {
        updateState { copy(isInitialLoading = true, initialLoadError = null) }

        var loadedStates: List<State> = emptyList()
        var loadedTypes: List<LicenseType> = emptyList()
        var hasError = false

        getStates()
            .onSuccess { loadedStates = it }
            .onError { e ->
                hasError = true
                updateState { copy(isInitialLoading = false, initialLoadError = e.message ?: "Failed to load states") }
            }

        if (hasError) return

        getLicenseTypes()
            .onSuccess { loadedTypes = it }
            .onError { e ->
                hasError = true
                updateState { copy(isInitialLoading = false, initialLoadError = e.message ?: "Failed to load license types") }
            }

        if (hasError) return

        val activeStates = loadedStates.filter { it.isActive }
        val activeTypes = loadedTypes.filter { it.isActive }.sortedBy { it.displayOrder }

        if (activeStates.isEmpty()) {
            updateState { copy(isInitialLoading = false, initialLoadError = "No states available") }
            return
        }

        val savedStateId = onboardingPreferences.getSelectedStateId()
        val savedTypeId = onboardingPreferences.getSelectedLicenseTypeId()

        val defaultStateId = savedStateId?.takeIf { id -> activeStates.any { it.id == id } }
            ?: activeStates.firstOrNull { it.id == DEFAULT_STATE_ID }?.id
            ?: activeStates.first().id

        val defaultTypeId = savedTypeId?.takeIf { id -> activeTypes.any { it.id == id } }
            ?: activeTypes.firstOrNull { it.id == DEFAULT_TYPE_ID }?.id
            ?: activeTypes.firstOrNull()?.id

        updateState {
            copy(
                states = loadedStates,
                selectedStateId = defaultStateId,
                licenseTypes = activeTypes,
                selectedLicenseTypeId = defaultTypeId
            )
        }

        loadQuestionSetsAndSync(defaultStateId, defaultTypeId)
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.loadQuestionSetsAndSync(
        stateId: String,
        typeId: String?
    ) {
        getStateResources(stateId)
            .onSuccess { resources ->
                updateState { copy(stateResources = resources) }
            }
            .onError {
                updateState { copy(stateResources = emptyList()) }
            }

        getQuestionSetsByState(stateId)
            .onSuccess { questionSets ->
                val activeQuestionSets = questionSets.filter { it.isActive }
                val matchingQuestionSet = if (typeId != null) {
                    activeQuestionSets.firstOrNull { it.licenseTypeId == typeId }
                } else {
                    activeQuestionSets.firstOrNull()
                }

                updateState {
                    copy(
                        questionSets = activeQuestionSets,
                        selectedQuestionSet = matchingQuestionSet,
                        isInitialLoading = false,
                        isSyncing = matchingQuestionSet != null
                    )
                }

                if (matchingQuestionSet != null) {
                    setSelectedQuestionSet(matchingQuestionSet.id)
                    syncInBackground()
                }
            }
            .onError { e ->
                updateState {
                    copy(
                        isInitialLoading = false,
                        initialLoadError = e.message ?: "Failed to load question sets"
                    )
                }
            }
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.selectState(stateId: String) {
        var currentStateId: String? = null
        var currentTypeId: String? = null
        withState {
            currentStateId = selectedStateId
            currentTypeId = selectedLicenseTypeId
        }
        if (currentStateId == stateId) return

        analytics.track(AnalyticsEvent.StateSelected(stateId))
        onboardingPreferences.setSelectedStateId(stateId)
        updateState { copy(selectedStateId = stateId, isSyncing = true) }
        loadQuestionSetsAndSync(stateId, currentTypeId)
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.selectLicenseType(typeId: String) {
        var currentTypeId: String? = null
        var questionSets: List<QuestionSet> = emptyList()
        withState {
            currentTypeId = selectedLicenseTypeId
            questionSets = this.questionSets
        }
        if (currentTypeId == typeId) return

        analytics.track(AnalyticsEvent.LicenseTypeSelected(typeId))
        onboardingPreferences.setSelectedLicenseTypeId(typeId)
        val matchingQuestionSet = questionSets.firstOrNull { it.licenseTypeId == typeId }

        updateState {
            copy(
                selectedLicenseTypeId = typeId,
                selectedQuestionSet = matchingQuestionSet
            )
        }

        matchingQuestionSet?.let {
            setSelectedQuestionSet(it.id)
            syncInBackground()
        }
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.syncInBackground() {
        updateState { copy(isSyncing = true) }
        syncQuestions()
            .onSuccess { updateState { copy(isSyncing = false, syncError = null) } }
            .onError { e -> updateState { copy(isSyncing = false, syncError = e.message) } }
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.trackExternalLink(
        linkType: String,
        url: String
    ) {
        withState {
            val stateId = selectedStateId ?: "unknown"
            analytics.track(AnalyticsEvent.ExternalLinkOpened(linkType, stateId))
        }
    }

    companion object {
        private const val DEFAULT_STATE_ID = "nsw"
        private const val DEFAULT_TYPE_ID = "car"
    }
}
