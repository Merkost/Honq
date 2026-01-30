package com.merkost.honq.domain.usecase

import com.merkost.honq.data.local.InAppReviewPreferences
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first

class ReviewEligibilityManager(
    private val prefs: InAppReviewPreferences,
    private val progressRepository: ProgressRepository
) {
    companion object {
        private const val MAX_REVIEW_REQUESTS = 2
        private const val COOLDOWN_MILLIS = 7L * 24 * 60 * 60 * 1000 // 7 days
        private const val MIN_QUESTIONS_ANSWERED = 10
        private const val PRACTICE_MILESTONE_THRESHOLD = 20
        private const val STATISTICS_QUESTIONS_THRESHOLD = 30
        private const val STATISTICS_MOCK_TESTS_THRESHOLD = 1
    }

    suspend fun shouldRequestReview(trigger: String): Boolean {
        if (prefs.getReviewRequestCount() >= MAX_REVIEW_REQUESTS) return false

        val lastMillis = prefs.getLastReviewRequestMillis()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        if (lastMillis > 0 && (now - lastMillis) < COOLDOWN_MILLIS) return false

        val progress = progressRepository.observeUserProgress().first()
        if (progress.uniqueQuestionsAnswered < MIN_QUESTIONS_ANSWERED) return false

        return when (trigger) {
            "MOCK_TEST_PASSED" -> true
            "PRACTICE_MILESTONE" -> progress.uniqueQuestionsAnswered >= PRACTICE_MILESTONE_THRESHOLD
            "STATISTICS_VIEWED" -> progress.uniqueQuestionsAnswered >= STATISTICS_QUESTIONS_THRESHOLD
                    && progress.mockTestsTaken >= STATISTICS_MOCK_TESTS_THRESHOLD
            else -> false
        }
    }

    fun markReviewRequested() {
        prefs.incrementReviewRequestCount()
        prefs.setLastReviewRequestMillis(kotlin.time.Clock.System.now().toEpochMilliseconds())
    }
}
