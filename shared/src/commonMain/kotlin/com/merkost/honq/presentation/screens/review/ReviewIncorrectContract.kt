package com.merkost.honq.presentation.screens.review

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.IncorrectAnswer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class ReviewIncorrectState(
    val incorrectAnswers: List<IncorrectAnswer> = emptyList(),
    val currentIndex: Int = 0,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState {
    val currentAnswer: IncorrectAnswer? get() = incorrectAnswers.getOrNull(currentIndex)
    val currentQuestionNumber: Int get() = currentIndex + 1
    val totalQuestions: Int get() = incorrectAnswers.size
    val isFirstQuestion: Boolean get() = currentIndex == 0
    val isLastQuestion: Boolean get() = currentIndex == incorrectAnswers.lastIndex
    val progress: Float get() = if (incorrectAnswers.isEmpty()) 0f else (currentIndex + 1).toFloat() / incorrectAnswers.size
}

sealed interface ReviewIncorrectIntent : MVIIntent {
    data object NextQuestion : ReviewIncorrectIntent
    data object PreviousQuestion : ReviewIncorrectIntent
    data class ToggleFavorite(val questionId: String) : ReviewIncorrectIntent
    data object Exit : ReviewIncorrectIntent
}

sealed interface ReviewIncorrectAction : MVIAction {
    data object NavigateBack : ReviewIncorrectAction
}
