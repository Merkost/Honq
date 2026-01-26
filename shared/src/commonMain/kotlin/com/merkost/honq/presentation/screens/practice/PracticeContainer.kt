package com.merkost.honq.presentation.screens.practice

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetRandomQuestionsUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.RecordAnswerUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class PracticeContainer(
    private val getRandomQuestions: GetRandomQuestionsUseCase,
    private val recordAnswer: RecordAnswerUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<PracticeState, PracticeIntent, PracticeAction> {

    override val store = store(PracticeState(), scope) {
        init {
            analytics.track(AnalyticsEvent.PracticeStarted)
            loadNextQuestion()
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        reduce { intent ->
            when (intent) {
                is PracticeIntent.AnswerSelected -> handleAnswerSelected(intent.index)
                is PracticeIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                PracticeIntent.NextQuestion -> loadNextQuestion()
                PracticeIntent.Exit -> action(PracticeAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.handleAnswerSelected(
        index: Int
    ) {
        withState {
            val question = currentQuestion ?: return@withState
            val isCorrect = index == question.correctIndex

            recordAnswer(question.id, isCorrect)
            analytics.track(
                AnalyticsEvent.QuestionAnswered(
                    questionId = question.id,
                    isCorrect = isCorrect,
                    categoryId = question.categoryId
                )
            )

            updateState {
                copy(
                    selectedAnswer = index,
                    answerRevealed = true,
                    correctAnswers = if (isCorrect) correctAnswers + 1 else correctAnswers
                )
            }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.loadNextQuestion() {
        withState {
            if (currentQuestion == null) {
                updateState { copy(isLoading = true) }
            } else {
                updateState { copy(isLoadingNext = true) }
            }
        }


        getRandomQuestions(1)
            .onSuccess { questions ->
                updateState {
                    copy(
                        currentQuestion = questions.firstOrNull(),
                        selectedAnswer = null,
                        answerRevealed = false,
                        isLoading = false,
                        isLoadingNext = false,
                        questionsAnswered = questionsAnswered + 1,
                        error = null
                    )
                }
            }
            .onError { e ->
                updateState { copy(error = e.message, isLoading = false, isLoadingNext = false) }
            }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.toggleFavorite(
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
