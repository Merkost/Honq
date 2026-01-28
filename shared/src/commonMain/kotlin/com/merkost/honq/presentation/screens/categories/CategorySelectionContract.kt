package com.merkost.honq.presentation.screens.categories

import androidx.compose.runtime.Immutable
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.CategoryProgress
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class CategorySelectionState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val progressMap: Map<String, CategoryProgress> = emptyMap(),
    val totalQuestions: Int = 0,
    val totalAnswered: Int = 0
) : MVIState

sealed interface CategorySelectionIntent : MVIIntent {
    data class SelectCategory(val categoryId: String) : CategorySelectionIntent
    data object SelectAllCategories : CategorySelectionIntent
    data object Exit : CategorySelectionIntent
}

sealed interface CategorySelectionAction : MVIAction {
    data object NavigateBack : CategorySelectionAction
    data class NavigateToPractice(val categoryId: String?, val categoryName: String?) : CategorySelectionAction
}
