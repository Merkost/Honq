package com.merkost.honq.presentation.screens.mocktest

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.QuizSession
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
data class MockTestState(
    val session: QuizSession = QuizSession(emptyList()),
    val selectedAnswer: Int? = null,
    val timeRemaining: Duration = 30.minutes,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val navigationDirection: NavigationDirection = NavigationDirection.Forward
) : MVIState {
    val currentQuestionNumber: Int get() = session.currentIndex + 1
    val totalQuestions: Int get() = session.questions.size
}

enum class NavigationDirection {
    Forward, Backward
}

sealed interface MockTestIntent : MVIIntent {
    data class AnswerSelected(val index: Int) : MockTestIntent
    data class ToggleFavorite(val questionId: String) : MockTestIntent
    data object NextQuestion : MockTestIntent
    data object PreviousQuestion : MockTestIntent
    data object SubmitTest : MockTestIntent
    data object Exit : MockTestIntent
}

sealed interface MockTestAction : MVIAction {
    data object NavigateBack : MockTestAction
    data class NavigateToResults(val score: Int, val total: Int) : MockTestAction
}
