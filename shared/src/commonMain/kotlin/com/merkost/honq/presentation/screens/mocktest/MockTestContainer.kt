package com.merkost.honq.presentation.screens.mocktest

import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.QuizSession
import com.merkost.honq.domain.usecase.GetMockTestQuestionsUseCase
import com.merkost.honq.domain.usecase.SaveMockTestResultUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import kotlin.time.Clock.System as SystemClock
import kotlin.time.Duration.Companion.seconds

private val clock = SystemClock

class MockTestContainer(
    private val getMockTestQuestions: GetMockTestQuestionsUseCase,
    private val saveMockTestResult: SaveMockTestResultUseCase,
    scope: CoroutineScope
) : Container<MockTestState, MockTestIntent, MockTestAction> {

    override val store = store(MockTestState(), scope) {
        init {
            loadQuestions()
        }

        whileSubscribed {
            startTimer()
        }

        reduce { intent ->
            when (intent) {
                is MockTestIntent.AnswerSelected -> handleAnswerSelected(intent.index)
                MockTestIntent.NextQuestion -> goToNextQuestion()
                MockTestIntent.PreviousQuestion -> goToPreviousQuestion()
                MockTestIntent.SubmitTest -> submitTest()
                MockTestIntent.Exit -> action(MockTestAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.loadQuestions() {
        val startTime = clock.now()
        getMockTestQuestions()
            .onSuccess { questions ->
                updateState {
                    copy(
                        session = QuizSession(questions = questions, startTime = startTime),
                        isLoading = false
                    )
                }
            }
            .onError { e ->
                updateState { copy(error = e.message, isLoading = false) }
            }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.startTimer() {
        while (true) {
            delay(1.seconds)
            var shouldSubmit = false
            withState {
                if (timeRemaining <= 0.seconds) {
                    shouldSubmit = true
                }
            }
            if (shouldSubmit) {
                submitTest()
                break
            }
            updateState { copy(timeRemaining = timeRemaining - 1.seconds) }
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.handleAnswerSelected(index: Int) {
        withState {
            val currentQuestion = session.currentQuestion ?: return@withState
            updateState {
                copy(
                    session = session.copy(
                        answers = session.answers + (currentQuestion.id to index)
                    ),
                    selectedAnswer = index
                )
            }
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.goToNextQuestion() {
        updateState {
            copy(
                session = session.copy(currentIndex = (session.currentIndex + 1).coerceAtMost(session.questions.lastIndex)),
                selectedAnswer = session.answers[session.questions.getOrNull(session.currentIndex + 1)?.id]
            )
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.goToPreviousQuestion() {
        updateState {
            copy(
                session = session.copy(currentIndex = (session.currentIndex - 1).coerceAtLeast(0)),
                selectedAnswer = session.answers[session.questions.getOrNull(session.currentIndex - 1)?.id]
            )
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.submitTest() {
        updateState { copy(isSubmitting = true) }

        val now = clock.now()
        withState {
            val correctCount = session.questions.count { question ->
                session.answers[question.id] == question.correctIndex
            }

            val startTime = session.startTime ?: now
            val timeTaken = now - startTime

            val result = MockTestResult(
                totalQuestions = session.questions.size,
                correctAnswers = correctCount,
                timeTaken = timeTaken,
                completedAt = now
            )

            saveMockTestResult(result)

            action(MockTestAction.NavigateToResults(correctCount, session.questions.size))
        }
    }
}
