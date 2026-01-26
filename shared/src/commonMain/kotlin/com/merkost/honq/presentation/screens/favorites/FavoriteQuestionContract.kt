package com.merkost.honq.presentation.screens.favorites

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class FavoriteQuestionState(
    val question: Question? = null,
    val selectedAnswer: Int? = null,
    val answerRevealed: Boolean = false,
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState

sealed interface FavoriteQuestionIntent : MVIIntent {
    data class AnswerSelected(val index: Int) : FavoriteQuestionIntent
    data object TryAgain : FavoriteQuestionIntent
    data object ToggleFavorite : FavoriteQuestionIntent
    data object NavigateBack : FavoriteQuestionIntent
}

sealed interface FavoriteQuestionAction : MVIAction {
    data object NavigateBack : FavoriteQuestionAction
}
