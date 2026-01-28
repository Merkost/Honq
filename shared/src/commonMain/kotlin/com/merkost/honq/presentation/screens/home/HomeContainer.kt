package com.merkost.honq.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.data.repository.DataSyncManager
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
import com.merkost.honq.domain.repository.QuestionRepository
import org.kimplify.cedar.logging.Cedar
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
    private val analytics: Analytics,
    private val dataSyncManager: DataSyncManager,
    private val repository: QuestionRepository
) : Container<HomeState, HomeIntent, HomeAction>, ViewModel() {

    /** Cached from the version check done once during [loadInitialData]. */
    private var pendingSyncVersion: Int? = null

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
        Cedar.tag("Home").d("loadInitialData: starting...")
        updateState { copy(isInitialLoading = true, initialLoadError = null) }

        val dbEmpty = !dataSyncManager.needsInitialSync() && repository.isDatabaseEmpty()
        if (dbEmpty) {
            Cedar.tag("Home").d("loadInitialData: DB empty but sync flag set, resetting sync state")
            dataSyncManager.resetSyncState()
        }
        if (dataSyncManager.needsInitialSync()) {
            Cedar.tag("Home").d("loadInitialData: first launch, running full sync")
            val remoteVersion = dataSyncManager.fetchRemoteVersion().getOrDefault(0)
            repository.fullSync(null)
            dataSyncManager.markSyncCompleted(remoteVersion)
        } else {
            val check = dataSyncManager.checkIfSyncNeeded()
            if (check.needsSync) {
                Cedar.tag("Home").d("loadInitialData: data version changed, pending sync version=${check.remoteVersion}")
                pendingSyncVersion = check.remoteVersion
            }
        }

        var loadedStates: List<State> = emptyList()
        var loadedTypes: List<LicenseType> = emptyList()
        var hasError = false

        getStates()
            .onSuccess { loadedStates = it }
            .onError { e ->
                Cedar.tag("Home").e("loadInitialData: failed to load states: ${e.message}", e)
                hasError = true
                updateState { copy(isInitialLoading = false, initialLoadError = e.message ?: "Failed to load states") }
            }

        if (hasError) return

        getLicenseTypes()
            .onSuccess { loadedTypes = it }
            .onError { e ->
                Cedar.tag("Home").e("loadInitialData: failed to load license types: ${e.message}", e)
                hasError = true
                updateState { copy(isInitialLoading = false, initialLoadError = e.message ?: "Failed to load license types") }
            }

        if (hasError) return

        val activeStates = loadedStates.filter { it.isActive }
        val activeTypes = loadedTypes.filter { it.isActive }.sortedBy { it.displayOrder }

        if (activeStates.isEmpty()) {
            Cedar.tag("Home").w("loadInitialData: no active states available")
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

        Cedar.tag("Home").d("loadInitialData: state=$defaultStateId, type=$defaultTypeId, states=${activeStates.size}, types=${activeTypes.size}")
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
                Cedar.tag("Home").e("loadQuestionSetsAndSync: failed to load question sets for state=$stateId: ${e.message}", e)
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
        var questionSetId: String? = null
        withState { questionSetId = selectedQuestionSet?.id }

        val alreadySynced = questionSetId?.let { repository.getLastSyncTime(it) } != null
        val hasQuestions = questionSetId?.let { repository.hasQuestionsForSet(it) } ?: false

        if (alreadySynced && hasQuestions && pendingSyncVersion == null) {
            Cedar.tag("Home").d("syncInBackground: skipping sync (already synced, has questions, no version change)")
            updateState { copy(isSyncing = false, syncError = null) }
            return
        }

        Cedar.tag("Home").d("syncInBackground: starting sync for questionSet=$questionSetId")
        updateState { copy(isSyncing = true) }
        syncQuestions()
            .onSuccess {
                pendingSyncVersion?.let { version ->
                    dataSyncManager.markSyncCompleted(version)
                    pendingSyncVersion = null
                }
                Cedar.tag("Home").d("syncInBackground: sync completed successfully")
                updateState { copy(isSyncing = false, syncError = null) }
            }
            .onError { e ->
                Cedar.tag("Home").e("syncInBackground: sync failed: ${e.message}", e)
                updateState { copy(isSyncing = false, syncError = e.message) }
            }
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
