package com.merkost.honq.presentation.screens.mocktestview

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.MockTestReviewAnswer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class MockTestReviewState(
    val answers: List<MockTestReviewAnswer> = emptyList(),
    val currentIndex: Int = 0,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState {
    val currentAnswer: MockTestReviewAnswer? get() = answers.getOrNull(currentIndex)
    val currentQuestionNumber: Int get() = currentIndex + 1
    val totalQuestions: Int get() = answers.size
    val isFirstQuestion: Boolean get() = currentIndex == 0
    val isLastQuestion: Boolean get() = currentIndex >= answers.lastIndex
    val progress: Float get() = if (answers.isEmpty()) 0f else (currentIndex + 1f) / answers.size
}

sealed interface MockTestReviewIntent : MVIIntent {
    data object NextQuestion : MockTestReviewIntent
    data object PreviousQuestion : MockTestReviewIntent
    data class ToggleFavorite(val questionId: String) : MockTestReviewIntent
    data object Exit : MockTestReviewIntent
}

sealed interface MockTestReviewAction : MVIAction {
    data object NavigateBack : MockTestReviewAction
}
