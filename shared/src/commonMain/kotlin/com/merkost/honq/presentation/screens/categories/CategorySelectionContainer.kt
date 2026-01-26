package com.merkost.honq.presentation.screens.categories

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class CategorySelectionContainer(
    private val getCategories: GetCategoriesUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<CategorySelectionState, CategorySelectionIntent, CategorySelectionAction> {

    override val store = store(CategorySelectionState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("category_selection"))
            loadCategories()
        }

        reduce { intent ->
            when (intent) {
                is CategorySelectionIntent.SelectCategory -> {
                    analytics.track(AnalyticsEvent.CategorySelected(intent.categoryId))
                    withState {
                        val categoryName = categories.find { it.id == intent.categoryId }?.name
                        action(CategorySelectionAction.NavigateToPractice(intent.categoryId, categoryName))
                    }
                }
                CategorySelectionIntent.SelectAllCategories -> {
                    analytics.track(AnalyticsEvent.CategorySelected("all"))
                    action(CategorySelectionAction.NavigateToPractice(null, null))
                }
                CategorySelectionIntent.Exit -> action(CategorySelectionAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<CategorySelectionState, CategorySelectionIntent, CategorySelectionAction>.loadCategories() {
        getCategories()
            .onSuccess { categories ->
                updateState {
                    copy(
                        categories = categories.filter { it.isActive }.sortedBy { it.displayOrder },
                        isLoading = false
                    )
                }
            }
            .onError { e ->
                updateState { copy(error = e.message, isLoading = false) }
            }
    }
}
