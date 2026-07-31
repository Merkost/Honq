package com.merkost.honq.presentation.screens.home

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.model.StateResource
import com.merkost.honq.domain.model.UserProgress
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class HomeState(
    val progress: UserProgress = UserProgress(0, 0, 0, 0, 0, 0, null),
    val states: List<State> = emptyList(),
    val selectedStateId: String? = null,
    val licenseTypes: List<LicenseType> = emptyList(),
    val selectedLicenseTypeId: String? = null,
    val questionSets: List<QuestionSet> = emptyList(),
    val selectedQuestionSet: QuestionSet? = null,
    val favoriteQuestions: List<Question> = emptyList(),
    val stateResources: List<StateResource> = emptyList(),
    val isInitialLoading: Boolean = true,
    val initialLoadError: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
) : MVIState {
    val isReady: Boolean
        get() = !isInitialLoading && initialLoadError == null && selectedQuestionSet != null
}

sealed interface HomeIntent : MVIIntent {
    data object StartPractice : HomeIntent
    data object StartMockTest : HomeIntent
    data class SelectState(val stateId: String) : HomeIntent
    data class SelectLicenseType(val typeId: String) : HomeIntent
    data class OpenExternalLink(val linkType: String, val url: String) : HomeIntent
    data object Retry : HomeIntent
    data object RetrySync : HomeIntent
}

sealed interface HomeAction : MVIAction {
    data object NavigateToPractice : HomeAction
    data object NavigateToMockTest : HomeAction
}
