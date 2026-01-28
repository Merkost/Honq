package com.merkost.honq.presentation.screens.mocktestview

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.GetMockTestReviewUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import kotlinx.coroutines.CoroutineScope
import org.kimplify.cedar.logging.Cedar
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class MockTestReviewContainer(
    private val getMockTestReview: GetMockTestReviewUseCase,
    private val observeFavoriteQuestionIds: ObserveFavoriteQuestionIdsUseCase,
    private val toggleFavoriteQuestion: ToggleFavoriteQuestionUseCase,
    private val analytics: Analytics,
    private val mockTestResultId: Long,
    scope: CoroutineScope
) : Container<MockTestReviewState, MockTestReviewIntent, MockTestReviewAction> {

    override val store = store(MockTestReviewState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("mock_test_review"))
            Cedar.tag("MockTestReview").d("Loading review for mockTestResultId=$mockTestResultId")
            getMockTestReview(mockTestResultId)
                .onSuccess { answers ->
                    Cedar.tag("MockTestReview").d("Loaded ${answers.size} incorrect answers for review")
                    updateState { copy(answers = answers, isLoading = false) }
                }
                .onFailure { error ->
                    Cedar.tag("MockTestReview").e("Failed to load review: ${error.message}", error)
                    updateState { copy(error = error.message ?: "Failed to load answers", isLoading = false) }
                }
        }

        whileSubscribed {
            observeFavoriteQuestionIds().collect { favoriteIds ->
                updateState { copy(favoriteQuestionIds = favoriteIds) }
            }
        }

        reduce { intent ->
            when (intent) {
                MockTestReviewIntent.NextQuestion -> goToNextQuestion()
                MockTestReviewIntent.PreviousQuestion -> goToPreviousQuestion()
                is MockTestReviewIntent.ToggleFavorite -> toggleFavorite(intent.questionId)
                MockTestReviewIntent.Exit -> action(MockTestReviewAction.NavigateBack)
            }
        }
    }

    private suspend fun PipelineContext<MockTestReviewState, MockTestReviewIntent, MockTestReviewAction>.goToNextQuestion() {
        updateState {
            copy(currentIndex = (currentIndex + 1).coerceAtMost(answers.lastIndex))
        }
    }

    private suspend fun PipelineContext<MockTestReviewState, MockTestReviewIntent, MockTestReviewAction>.goToPreviousQuestion() {
        updateState {
            copy(currentIndex = (currentIndex - 1).coerceAtLeast(0))
        }
    }

    private suspend fun PipelineContext<MockTestReviewState, MockTestReviewIntent, MockTestReviewAction>.toggleFavorite(
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
