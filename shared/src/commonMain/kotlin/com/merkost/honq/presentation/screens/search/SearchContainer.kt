package com.merkost.honq.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.SearchQuestionsUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.kimplify.cedar.logging.Cedar
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

@OptIn(FlowPreview::class)
class SearchContainer(
    private val searchQuestions: SearchQuestionsUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
) : Container<SearchState, SearchIntent, SearchAction>, ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")

    override val store = store(SearchState(), viewModelScope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("search"))
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        whileSubscribed {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    performSearch(query)
                }
        }

        reduce { intent ->
            when (intent) {
                is SearchIntent.UpdateQuery -> {
                    updateState { copy(query = intent.query, error = null) }
                    searchQueryFlow.value = intent.query
                    if (intent.query.isBlank()) {
                        updateState { copy(results = emptyList(), hasSearched = false) }
                    }
                }
                SearchIntent.ClearQuery -> {
                    searchQueryFlow.value = ""
                    updateState { copy(query = "", results = emptyList(), hasSearched = false, error = null) }
                }
                is SearchIntent.SelectQuestion -> {
                    analytics.track(AnalyticsEvent.SearchResultSelected(intent.questionId))
                    action(SearchAction.NavigateToQuestion(intent.questionId))
                }
                is SearchIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                SearchIntent.Exit -> action(SearchAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<SearchState, SearchIntent, SearchAction>.performSearch(query: String) {
        updateState { copy(isSearching = true) }

        val minDelay = async { delay(MIN_LOADING_DURATION) }
        val result = searchQuestions(query)
        minDelay.await()

        result
            .onSuccess { questions ->
                analytics.track(AnalyticsEvent.SearchPerformed(query, questions.size))
                updateState {
                    copy(
                        results = questions,
                        isSearching = false,
                        hasSearched = true,
                        error = null
                    )
                }
            }
            .onError { e ->
                Cedar.tag("Search").e("performSearch: query='$query' failed: ${e.message}", e)
                updateState {
                    copy(
                        isSearching = false,
                        hasSearched = true,
                        error = e.message
                    )
                }
            }
    }

    companion object {
        private const val MIN_LOADING_DURATION = 400L
    }

    private suspend fun PipelineContext<SearchState, SearchIntent, SearchAction>.toggleFavorite(
        questionId: String
    ) {
        withState {
            val isCurrentlyFavorite = favoriteQuestionIds.contains(questionId)
            if (isCurrentlyFavorite) {
                analytics.track(AnalyticsEvent.FavoriteRemoved(questionId))
            } else {
                analytics.track(AnalyticsEvent.FavoriteAdded(questionId))
            }
        }
        toggleFavoriteQuestion(questionId)
    }
}
