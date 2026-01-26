package com.merkost.honq.presentation.screens.favorites

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionsUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class FavoritesContainer(
    private val observeFavoriteQuestions: ObserveFavoriteQuestionsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val questionRepository: QuestionRepository,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<FavoritesState, FavoritesIntent, FavoritesAction> {

    override val store = store(FavoritesState(), scope) {
        init {
            questionRepository.syncStates()
            val favorites = observeFavoriteQuestions().first()
            updateState { copy(favorites = favorites, isLoading = false) }
        }

        whileSubscribed {
            observeFavoriteQuestions().collect { favorites ->
                updateState { copy(favorites = favorites) }
            }
        }

        reduce { intent ->
            when (intent) {
                is FavoritesIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                FavoritesIntent.NavigateBack -> action(FavoritesAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<FavoritesState, FavoritesIntent, FavoritesAction>.toggleFavorite(
        questionId: String
    ) {
        analytics.track(AnalyticsEvent.FavoriteRemoved(questionId))
        toggleFavoriteQuestion(questionId)
    }
}
