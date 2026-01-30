package com.merkost.honq.core.analytics

sealed class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any?> = emptyMap()
) {
    data object OnboardingStarted : AnalyticsEvent("onboarding_started")
    data class OnboardingCompleted(
        val stateId: String,
        val licenseTypeId: String
    ) : AnalyticsEvent(
        "onboarding_completed",
        mapOf("state_id" to stateId, "license_type_id" to licenseTypeId)
    )

    data class StateSelected(val stateId: String) : AnalyticsEvent(
        "state_selected",
        mapOf("state_id" to stateId)
    )
    data class LicenseTypeSelected(val licenseTypeId: String) : AnalyticsEvent(
        "license_type_selected",
        mapOf("license_type_id" to licenseTypeId)
    )

    data object PracticeStarted : AnalyticsEvent("practice_started")
    data class QuestionAnswered(
        val questionId: String,
        val isCorrect: Boolean,
        val categoryId: String
    ) : AnalyticsEvent(
        "question_answered",
        mapOf(
            "question_id" to questionId,
            "is_correct" to isCorrect,
            "category_id" to categoryId
        )
    )

    data object MockTestStarted : AnalyticsEvent("mock_test_started")
    data class MockTestCompleted(
        val score: Int,
        val total: Int,
        val passed: Boolean,
        val timeSpentSeconds: Long
    ) : AnalyticsEvent(
        "mock_test_completed",
        mapOf(
            "score" to score,
            "total" to total,
            "passed" to passed,
            "percentage" to if (total > 0) (score * 100 / total) else 0,
            "time_spent_seconds" to timeSpentSeconds
        )
    )

    data class FavoriteAdded(val questionId: String) : AnalyticsEvent(
        "favorite_added",
        mapOf("question_id" to questionId)
    )
    data class FavoriteRemoved(val questionId: String) : AnalyticsEvent(
        "favorite_removed",
        mapOf("question_id" to questionId)
    )

    data class ScreenViewed(val screenName: String) : AnalyticsEvent(
        "screen_viewed",
        mapOf("screen_name" to screenName)
    )

    data class ExternalLinkOpened(val linkType: String, val stateId: String) : AnalyticsEvent(
        "external_link_opened",
        mapOf("link_type" to linkType, "state_id" to stateId)
    )

    data class CategorySelected(val categoryId: String) : AnalyticsEvent(
        "category_selected",
        mapOf("category_id" to categoryId)
    )

    data class CategoryPracticeStarted(val categoryId: String?) : AnalyticsEvent(
        "category_practice_started",
        mapOf("category_id" to (categoryId ?: "all"))
    )

    data class SearchPerformed(val query: String, val resultsCount: Int) : AnalyticsEvent(
        "search_performed",
        mapOf("query" to query, "results_count" to resultsCount)
    )

    data class SearchResultSelected(val questionId: String) : AnalyticsEvent(
        "search_result_selected",
        mapOf("question_id" to questionId)
    )

    data class ReviewRequested(val trigger: String) : AnalyticsEvent(
        "review_requested",
        mapOf("trigger" to trigger)
    )

    data class ReviewCompleted(val trigger: String) : AnalyticsEvent(
        "review_completed",
        mapOf("trigger" to trigger)
    )

    data class ReviewFailed(val trigger: String, val error: String?) : AnalyticsEvent(
        "review_failed",
        mapOf("trigger" to trigger, "error" to error)
    )

    data class StatisticsViewed(
        val totalPracticed: Int,
        val accuracy: Float,
        val mockTestsTaken: Int
    ) : AnalyticsEvent(
        "statistics_viewed",
        mapOf(
            "total_practiced" to totalPracticed,
            "accuracy" to accuracy,
            "mock_tests_taken" to mockTestsTaken
        )
    )
}
