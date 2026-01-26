package com.merkost.honq.presentation.screens.favorites

import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetQuestionByIdUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class FavoriteQuestionContainer(
    private val questionId: String,
    private val getQuestionById: GetQuestionByIdUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    scope: CoroutineScope
) : Container<FavoriteQuestionState, FavoriteQuestionIntent, FavoriteQuestionAction> {

    override val store = store(FavoriteQuestionState(), scope) {
        init {
            loadQuestion()
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        reduce { intent ->
            when (intent) {
                is FavoriteQuestionIntent.AnswerSelected -> handleAnswerSelected(intent.index)
                FavoriteQuestionIntent.TryAgain -> resetAnswer()
                FavoriteQuestionIntent.ToggleFavorite -> toggleFavorite()
                FavoriteQuestionIntent.NavigateBack -> action(FavoriteQuestionAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<FavoriteQuestionState, FavoriteQuestionIntent, FavoriteQuestionAction>.loadQuestion() {
        updateState { copy(isLoading = true, error = null) }
        getQuestionById(questionId)
            .onSuccess { question ->
                if (question == null) {
                    updateState { copy(isLoading = false, error = "Question not found") }
                } else {
                    updateState { copy(question = question, isLoading = false, error = null) }
                }
            }
            .onError { e ->
                updateState { copy(isLoading = false, error = e.message) }
            }
    }

    private suspend fun PipelineContext<FavoriteQuestionState, FavoriteQuestionIntent, FavoriteQuestionAction>.handleAnswerSelected(
        index: Int
    ) {
        withState {
            if (question == null || answerRevealed) return@withState
            updateState { copy(selectedAnswer = index, answerRevealed = true) }
        }
    }

    private suspend fun PipelineContext<FavoriteQuestionState, FavoriteQuestionIntent, FavoriteQuestionAction>.resetAnswer() {
        updateState { copy(selectedAnswer = null, answerRevealed = false) }
    }

    private suspend fun PipelineContext<FavoriteQuestionState, FavoriteQuestionIntent, FavoriteQuestionAction>.toggleFavorite() {
        toggleFavoriteQuestion(questionId)
    }
}
