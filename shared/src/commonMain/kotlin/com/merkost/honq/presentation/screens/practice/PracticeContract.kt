package com.merkost.honq.presentation.screens.practice

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class PracticeState(
    val currentQuestion: Question? = null,
    val selectedAnswer: Int? = null,
    val answerRevealed: Boolean = false,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isLoadingNext: Boolean = false,
    val error: String? = null
) : MVIState

sealed interface PracticeIntent : MVIIntent {
    data class AnswerSelected(val index: Int) : PracticeIntent
    data class ToggleFavorite(val questionId: String) : PracticeIntent
    data object NextQuestion : PracticeIntent
    data object Exit : PracticeIntent
}

sealed interface PracticeAction : MVIAction {
    data object NavigateBack : PracticeAction
}
