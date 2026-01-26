package com.merkost.honq.presentation.screens.practice

import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetRandomQuestionsUseCase
import com.merkost.honq.domain.usecase.RecordAnswerUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class PracticeContainer(
    private val getRandomQuestions: GetRandomQuestionsUseCase,
    private val recordAnswer: RecordAnswerUseCase,
    scope: CoroutineScope
) : Container<PracticeState, PracticeIntent, PracticeAction> {

    override val store = store(PracticeState(), scope) {
        init {
            loadNextQuestion()
        }

        reduce { intent ->
            when (intent) {
                is PracticeIntent.AnswerSelected -> handleAnswerSelected(intent.index)
                PracticeIntent.NextQuestion -> loadNextQuestion()
                PracticeIntent.Exit -> action(PracticeAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.handleAnswerSelected(index: Int) {
        withState {
            val question = currentQuestion ?: return@withState
            val isCorrect = index == question.correctIndex

            recordAnswer(question.id, isCorrect)

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
        updateState { copy(isLoading = true, selectedAnswer = null, answerRevealed = false) }
        getRandomQuestions(1)
            .onSuccess { questions ->
                updateState {
                    copy(
                        currentQuestion = questions.firstOrNull(),
                        isLoading = false,
                        questionsAnswered = questionsAnswered + 1,
                        error = null
                    )
                }
            }
            .onError { e ->
                updateState { copy(error = e.message, isLoading = false) }
            }
    }
}
