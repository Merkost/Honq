package com.merkost.honq.presentation.screens.review

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.ObserveIncorrectAnswersUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class ReviewIncorrectContainer(
    private val observeIncorrectAnswers: ObserveIncorrectAnswersUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<ReviewIncorrectState, ReviewIncorrectIntent, ReviewIncorrectAction> {

    override val store = store(ReviewIncorrectState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("review_incorrect"))
        }

        whileSubscribed {
            observeIncorrectAnswers().collect { answers ->
                updateState {
                    copy(
                        incorrectAnswers = answers,
                        isLoading = false
                    )
                }
            }
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        reduce { intent ->
            when (intent) {
                ReviewIncorrectIntent.NextQuestion -> goToNextQuestion()
                ReviewIncorrectIntent.PreviousQuestion -> goToPreviousQuestion()
                is ReviewIncorrectIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                ReviewIncorrectIntent.Exit -> action(ReviewIncorrectAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<ReviewIncorrectState, ReviewIncorrectIntent, ReviewIncorrectAction>.goToNextQuestion() {
        updateState {
            copy(currentIndex = (currentIndex + 1).coerceAtMost(incorrectAnswers.lastIndex))
        }
    }

    private suspend fun PipelineContext<ReviewIncorrectState, ReviewIncorrectIntent, ReviewIncorrectAction>.goToPreviousQuestion() {
        updateState {
            copy(currentIndex = (currentIndex - 1).coerceAtLeast(0))
        }
    }

    private suspend fun PipelineContext<ReviewIncorrectState, ReviewIncorrectIntent, ReviewIncorrectAction>.toggleFavorite(
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
