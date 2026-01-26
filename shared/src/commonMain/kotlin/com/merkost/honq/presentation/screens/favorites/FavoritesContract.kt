package com.merkost.honq.presentation.screens.favorites

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class FavoritesState(
    val favorites: List<Question> = emptyList(),
    val isLoading: Boolean = true
) : MVIState

sealed interface FavoritesIntent : MVIIntent {
    data class ToggleFavorite(val questionId: String) : FavoritesIntent
    data object NavigateBack : FavoritesIntent
}

sealed interface FavoritesAction : MVIAction {
    data object NavigateBack : FavoritesAction
}
