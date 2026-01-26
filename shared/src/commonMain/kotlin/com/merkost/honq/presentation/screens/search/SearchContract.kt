package com.merkost.honq.presentation.screens.search

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Question
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class SearchState(
    val query: String = "",
    val results: List<Question> = emptyList(),
    val favoriteQuestionIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
) : MVIState {
    val isEmpty: Boolean get() = results.isEmpty() && hasSearched && query.isNotBlank()
}

sealed interface SearchIntent : MVIIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
    data class SelectQuestion(val questionId: String) : SearchIntent
    data class ToggleFavorite(val questionId: String) : SearchIntent
    data object Exit : SearchIntent
}

sealed interface SearchAction : MVIAction {
    data object NavigateBack : SearchAction
    data class NavigateToQuestion(val questionId: String) : SearchAction
}
