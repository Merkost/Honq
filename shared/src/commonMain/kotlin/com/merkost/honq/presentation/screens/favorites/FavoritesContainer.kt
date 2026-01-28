package com.merkost.honq.presentation.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionsUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.flow.first
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class FavoritesContainer(
    private val observeFavoriteQuestions: ObserveFavoriteQuestionsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
) : Container<FavoritesState, FavoritesIntent, FavoritesAction>, ViewModel() {

    override val store = store(FavoritesState(), viewModelScope) {
        whileSubscribed {
            observeFavoriteQuestions().collect { favorites ->
                updateState { copy(favorites = favorites, isLoading = false) }
            }
        }

        reduce { intent ->
            when (intent) {
                is FavoritesIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                FavoritesIntent.NavigateBack -> action(FavoritesAction.NavigateBack)
            }
        }
    }

    private suspend fun toggleFavorite(questionId: String) {
        analytics.track(AnalyticsEvent.FavoriteRemoved(questionId))
        toggleFavoriteQuestion(questionId)
    }
}
