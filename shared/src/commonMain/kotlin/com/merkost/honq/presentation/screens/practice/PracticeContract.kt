package com.merkost.honq.presentation.screens.practice

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class PracticeHistoryEntry(
    val question: Question,
    val selectedAnswer: Int? = null,
    val answerRevealed: Boolean = false
)

enum class PracticeNavigationDirection {
    Forward, Backward
}

@Immutable
data class PracticeState(
    val questionHistory: List<PracticeHistoryEntry> = emptyList(),
    val currentIndex: Int = -1,
    val navigationDirection: PracticeNavigationDirection = PracticeNavigationDirection.Forward,
    val correctAnswers: Int = 0,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isLoadingNext: Boolean = false,
    val error: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val smartMode: Boolean = false
) : MVIState {
    val isFiltered: Boolean get() = categoryId != null
    val currentEntry: PracticeHistoryEntry? get() = questionHistory.getOrNull(currentIndex)
    val currentQuestion: Question? get() = currentEntry?.question
    val selectedAnswer: Int? get() = currentEntry?.selectedAnswer
    val answerRevealed: Boolean get() = currentEntry?.answerRevealed ?: false
    val questionsAnswered: Int get() = questionHistory.size
    val currentQuestionNumber: Int get() = currentIndex + 1
    val isFirstQuestion: Boolean get() = currentIndex <= 0
}

sealed interface PracticeIntent : MVIIntent {
    data class AnswerSelected(val index: Int) : PracticeIntent
    data class ToggleFavorite(val questionId: String) : PracticeIntent
    data object NextQuestion : PracticeIntent
    data object PreviousQuestion : PracticeIntent
    data object Exit : PracticeIntent
}

sealed interface PracticeAction : MVIAction {
    data object NavigateBack : PracticeAction
}
