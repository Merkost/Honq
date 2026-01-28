package com.merkost.honq.presentation.screens.weakest

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.WeakQuestion
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class WeakestQuestionsState(
    val questions: List<WeakQuestion> = emptyList(),
    val isLoading: Boolean = true
) : MVIState

sealed interface WeakestQuestionsIntent : MVIIntent {
    data object NavigateBack : WeakestQuestionsIntent
    data class OpenQuestion(val questionId: String) : WeakestQuestionsIntent
}

sealed interface WeakestQuestionsAction : MVIAction {
    data object NavigateBack : WeakestQuestionsAction
    data class NavigateToQuestion(val questionId: String) : WeakestQuestionsAction
}
