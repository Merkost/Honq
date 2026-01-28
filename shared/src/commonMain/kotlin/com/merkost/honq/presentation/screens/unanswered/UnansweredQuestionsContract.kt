package com.merkost.honq.presentation.screens.unanswered

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class UnansweredQuestionsState(
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = true
) : MVIState

sealed interface UnansweredQuestionsIntent : MVIIntent {
    data object NavigateBack : UnansweredQuestionsIntent
    data class OpenQuestion(val questionId: String) : UnansweredQuestionsIntent
}

sealed interface UnansweredQuestionsAction : MVIAction {
    data object NavigateBack : UnansweredQuestionsAction
    data class NavigateToQuestion(val questionId: String) : UnansweredQuestionsAction
}
