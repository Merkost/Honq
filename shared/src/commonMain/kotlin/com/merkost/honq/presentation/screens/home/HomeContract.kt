package com.merkost.honq.presentation.screens.home

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.UserProgress
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class HomeState(
    val progress: UserProgress = UserProgress(0, 0, 0, 0, null),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncError: String? = null
) : MVIState

sealed interface HomeIntent : MVIIntent {
    data object StartPractice : HomeIntent
    data object StartMockTest : HomeIntent
    data object Retry : HomeIntent
}

sealed interface HomeAction : MVIAction {
    data object NavigateToPractice : HomeAction
    data object NavigateToMockTest : HomeAction
}
