package com.merkost.honq.presentation.screens.practice

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetRandomQuestionsUseCase
import com.merkost.honq.domain.usecase.GetSmartPracticeQuestionsUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.RecordAnswerUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import org.kimplify.cedar.logging.Cedar
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class PracticeContainer(
    private val categoryId: String?,
    private val categoryName: String?,
    private val smartMode: Boolean,
    private val getRandomQuestions: GetRandomQuestionsUseCase,
    private val getSmartPracticeQuestions: GetSmartPracticeQuestionsUseCase,
    private val recordAnswer: RecordAnswerUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<PracticeState, PracticeIntent, PracticeAction> {

    private var smartQueue: MutableList<com.merkost.honq.domain.model.Question> = mutableListOf()

    override val store = store(PracticeState(categoryId = categoryId, categoryName = categoryName, smartMode = smartMode), scope) {
        init {
            if (smartMode) {
                analytics.track(AnalyticsEvent.SmartPracticeStarted)
            } else if (categoryId != null) {
                analytics.track(AnalyticsEvent.CategoryPracticeStarted(categoryId))
            } else {
                analytics.track(AnalyticsEvent.PracticeStarted)
            }
            loadNewQuestion()
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
                PracticeIntent.NextQuestion -> handleNextQuestion()
                PracticeIntent.PreviousQuestion -> goToPreviousQuestion()
                PracticeIntent.Exit -> action(PracticeAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.handleAnswerSelected(
        index: Int
    ) {
        withState {
            val entry = questionHistory.getOrNull(currentIndex) ?: return@withState
            if (entry.answerRevealed) return@withState

            val question = entry.question
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
                val updatedHistory = questionHistory.toMutableList()
                updatedHistory[currentIndex] = entry.copy(
                    selectedAnswer = index,
                    answerRevealed = true
                )
                copy(
                    questionHistory = updatedHistory,
                    correctAnswers = if (isCorrect) correctAnswers + 1 else correctAnswers
                )
            }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.handleNextQuestion() {
        var needsNewQuestion = false

        withState {
            if (currentIndex < questionHistory.lastIndex) {
                updateState {
                    copy(
                        currentIndex = currentIndex + 1,
                        navigationDirection = PracticeNavigationDirection.Forward
                    )
                }
            } else {
                needsNewQuestion = true
                updateState { copy(isLoadingNext = true) }
            }
        }

        if (needsNewQuestion) {
            loadNewQuestion()
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.goToPreviousQuestion() {
        updateState {
            copy(
                currentIndex = (currentIndex - 1).coerceAtLeast(0),
                navigationDirection = PracticeNavigationDirection.Backward
            )
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.loadNewQuestion() {
        if (smartMode) {
            loadSmartQuestion()
        } else {
            loadRandomQuestion()
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.loadSmartQuestion() {
        try {
            if (smartQueue.isEmpty()) {
                val batch = getSmartPracticeQuestions(SMART_BATCH_SIZE)
                smartQueue.addAll(batch)
            }

            val nextQuestion = smartQueue.removeFirstOrNull()
            if (nextQuestion == null) {
                Cedar.tag("Practice").w("loadSmartQuestion: no questions available")
                updateState { copy(isLoading = false, isLoadingNext = false) }
                return
            }
            updateState {
                val newEntry = PracticeHistoryEntry(question = nextQuestion)
                copy(
                    questionHistory = questionHistory + newEntry,
                    currentIndex = questionHistory.size,
                    navigationDirection = PracticeNavigationDirection.Forward,
                    isLoading = false,
                    isLoadingNext = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            Cedar.tag("Practice").e("loadSmartQuestion: failed: ${e.message}", e)
            updateState { copy(error = e.message, isLoading = false, isLoadingNext = false) }
        }
    }

    private suspend fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.loadRandomQuestion() {
        getRandomQuestions(1, categoryId)
            .onSuccess { questions ->
                val newQuestion = questions.firstOrNull()
                if (newQuestion == null) {
                    Cedar.tag("Practice").w("loadNextQuestion: no questions returned for category=$categoryId")
                    updateState { copy(isLoading = false, isLoadingNext = false) }
                    return@onSuccess
                }
                updateState {
                    val newEntry = PracticeHistoryEntry(question = newQuestion)
                    copy(
                        questionHistory = questionHistory + newEntry,
                        currentIndex = questionHistory.size,
                        navigationDirection = PracticeNavigationDirection.Forward,
                        isLoading = false,
                        isLoadingNext = false,
                        error = null
                    )
                }
            }
            .onError { e ->
                Cedar.tag("Practice").e("loadNextQuestion: failed: ${e.message}", e)
                updateState { copy(error = e.message, isLoading = false, isLoadingNext = false) }
            }
    }

    companion object {
        private const val SMART_BATCH_SIZE = 20
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
