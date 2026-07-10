package com.merkost.honq.presentation.screens.mocktest

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.model.CategoryScore
import com.merkost.honq.domain.model.IncorrectAnswer
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.QuizSession
import com.merkost.honq.domain.repository.MockTestAnswer
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.usecase.GetMockTestQuestionsUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.SaveIncorrectAnswersUseCase
import com.merkost.honq.domain.usecase.SaveMockTestResultUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.kimplify.cedar.logging.Cedar
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
    private val saveIncorrectAnswers: SaveIncorrectAnswersUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository,
    private val questionRepository: QuestionRepository,
    private val premiumManager: PremiumManager,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<MockTestState, MockTestIntent, MockTestAction> {

    override val store = store(MockTestState(), scope) {
        init {
            analytics.track(AnalyticsEvent.MockTestStarted)
            loadQuestions()
        }

        whileSubscribed {
            startTimer()
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        reduce { intent ->
            when (intent) {
                is MockTestIntent.AnswerSelected -> handleAnswerSelected(intent.index)
                is MockTestIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                MockTestIntent.NextQuestion -> goToNextQuestion()
                MockTestIntent.PreviousQuestion -> goToPreviousQuestion()
                MockTestIntent.SubmitTest -> submitTest()
                MockTestIntent.Exit -> action(MockTestAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.loadQuestions() {
        Cedar.tag("MockTest").d("loadQuestions: loading mock test questions...")
        val startTime = clock.now()

        // Fetch pass percentage from the selected question set
        val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
        val passPercentage = if (questionSetId != null) {
            questionRepository.getQuestionSetById(questionSetId)
                .let { result ->
                    when (result) {
                        is com.merkost.honq.core.util.Result.Success ->
                            result.data?.mockTestPassPercentage ?: MockTestResult.DEFAULT_PASS_PERCENTAGE
                        is com.merkost.honq.core.util.Result.Error ->
                            MockTestResult.DEFAULT_PASS_PERCENTAGE
                    }
                }
        } else {
            MockTestResult.DEFAULT_PASS_PERCENTAGE
        }

        getMockTestQuestions()
            .onSuccess { questions ->
                Cedar.tag("MockTest").d("loadQuestions: loaded ${questions.size} questions, passPercentage=$passPercentage")
                updateState {
                    copy(
                        session = QuizSession(questions = questions, startTime = startTime),
                        passPercentage = passPercentage,
                        isLoading = false
                    )
                }
            }
            .onError { e ->
                Cedar.tag("MockTest").e("loadQuestions: failed: ${e.message}", e)
                updateState { copy(error = e.message, isLoading = false) }
            }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.startTimer() {
        while (true) {
            delay(1.seconds)
            var shouldSubmit = false
            withState {
                if (!isSubmitting && timeRemaining <= 0.seconds) {
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
                selectedAnswer = session.answers[session.questions.getOrNull(session.currentIndex + 1)?.id],
                navigationDirection = NavigationDirection.Forward
            )
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.goToPreviousQuestion() {
        updateState {
            copy(
                session = session.copy(currentIndex = (session.currentIndex - 1).coerceAtLeast(0)),
                selectedAnswer = session.answers[session.questions.getOrNull(session.currentIndex - 1)?.id],
                navigationDirection = NavigationDirection.Backward
            )
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.submitTest() {
        var alreadySubmitting = false
        updateState {
            if (isSubmitting) {
                alreadySubmitting = true
                this
            } else {
                copy(isSubmitting = true)
            }
        }
        if (alreadySubmitting) return
        Cedar.tag("MockTest").d("submitTest: submitting...")

        val now = clock.now()
        val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value ?: ""
        withState {
            val correctCount = session.questions.count { question ->
                session.answers[question.id] == question.correctIndex
            }

            val incorrectAnswers = session.questions
                .filter { question ->
                    val userAnswer = session.answers[question.id]
                    userAnswer != null && userAnswer != question.correctIndex
                }
                .map { question ->
                    IncorrectAnswer(
                        question = question,
                        selectedAnswerIndex = session.answers[question.id]!!
                    )
                }

            saveIncorrectAnswers(incorrectAnswers)

            val startTime = session.startTime ?: now
            val timeTaken = now - startTime
            val totalQuestions = session.questions.size

            val result = MockTestResult(
                questionSetId = questionSetId,
                totalQuestions = totalQuestions,
                correctAnswers = correctCount,
                timeTaken = timeTaken,
                completedAt = now,
                passPercentage = passPercentage
            )

            val allAnswers = session.questions.mapNotNull { question ->
                val selectedIndex = session.answers[question.id] ?: return@mapNotNull null
                MockTestAnswer(
                    questionId = question.id,
                    selectedAnswerIndex = selectedIndex,
                    wasCorrect = selectedIndex == question.correctIndex
                )
            }

            saveMockTestResult(result, allAnswers)

            if (!premiumManager.isPremium.value) {
                val remainingBefore = premiumManager.freeTrialMockTestsRemaining.value
                if (remainingBefore > 0) {
                    premiumManager.consumeFreeMockTest()
                    analytics.track(AnalyticsEvent.FreeMockTestConsumed(remainingBefore - 1))
                }
            }

            // Compute per-category breakdown
            val categoryBreakdown = session.questions
                .groupBy { it.categoryId }
                .map { (categoryId, questions) ->
                    val categoryCorrect = questions.count { q ->
                        session.answers[q.id] == q.correctIndex
                    }
                    CategoryScore(
                        categoryId = categoryId,
                        categoryName = questions.first().categoryName,
                        correct = categoryCorrect,
                        total = questions.size
                    )
                }
                .sortedByDescending { it.total }

            Cedar.tag("MockTest").d("submitTest: score=$correctCount/$totalQuestions, passed=${result.passed}, passPercentage=$passPercentage, timeTaken=${timeTaken.inWholeSeconds}s")

            analytics.track(
                AnalyticsEvent.MockTestCompleted(
                    score = correctCount,
                    total = totalQuestions,
                    passed = result.passed,
                    timeSpentSeconds = timeTaken.inWholeSeconds
                )
            )

            action(MockTestAction.NavigateToResults(correctCount, totalQuestions, incorrectAnswers.isNotEmpty(), passPercentage, categoryBreakdown))
        }
    }

    private suspend fun PipelineContext<MockTestState, MockTestIntent, MockTestAction>.toggleFavorite(
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
