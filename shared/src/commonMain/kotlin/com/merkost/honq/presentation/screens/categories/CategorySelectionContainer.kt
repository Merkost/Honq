package com.merkost.honq.presentation.screens.categories

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetCategoriesUseCase
import com.merkost.honq.domain.usecase.GetCategoryProgressUseCase
import kotlinx.coroutines.CoroutineScope
import org.kimplify.cedar.logging.Cedar
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class CategorySelectionContainer(
    private val getCategories: GetCategoriesUseCase,
    private val getCategoryProgress: GetCategoryProgressUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<CategorySelectionState, CategorySelectionIntent, CategorySelectionAction> {

    override val store = store(CategorySelectionState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("category_selection"))
            loadCategories()
            loadProgress()
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
        Cedar.tag("Categories").d("CategorySelectionContainer: loadCategories() called")
        getCategories()
            .onSuccess { categories ->
                Cedar.tag("Categories").d("CategorySelectionContainer: received ${categories.size} categories: ${categories.map { "${it.id}(active=${it.isActive})" }}")
                val filtered = categories.filter { it.isActive }.sortedBy { it.displayOrder }
                Cedar.tag("Categories").d("CategorySelectionContainer: after isActive filter: ${filtered.size} categories: ${filtered.map { it.id }}")
                updateState {
                    copy(
                        categories = filtered,
                        isLoading = false
                    )
                }
            }
            .onError { e ->
                Cedar.tag("Categories").e("CategorySelectionContainer: loadCategories ERROR: ${e.message}", e)
                updateState { copy(error = e.message, isLoading = false) }
            }
    }

    private suspend fun PipelineContext<CategorySelectionState, CategorySelectionIntent, CategorySelectionAction>.loadProgress() {
        getCategoryProgress()
            .onSuccess { progressMap ->
                val totalQuestions = progressMap.values.sumOf { it.totalQuestions }
                val totalAnswered = progressMap.values.sumOf { it.answeredQuestions }
                Cedar.tag("Categories").d("loadProgress: $totalAnswered/$totalQuestions across ${progressMap.size} categories")
                updateState {
                    copy(
                        progressMap = progressMap,
                        totalQuestions = totalQuestions,
                        totalAnswered = totalAnswered
                    )
                }
            }
            .onError { e ->
                Cedar.tag("Categories").e("loadProgress failed: ${e.message}", e)
            }
    }
}
