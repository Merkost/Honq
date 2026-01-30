package com.merkost.honq.presentation.util

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.ReviewEligibilityManager
import com.merkost.honq.review.InAppReviewService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.kimplify.cedar.logging.Cedar

private object AndroidInAppReview : KoinComponent {
    val reviewEligibilityManager: ReviewEligibilityManager by inject()
    val reviewService: InAppReviewService by inject()
    val analytics: Analytics by inject()
}

actual fun requestInAppReview(trigger: String) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            if (!AndroidInAppReview.reviewEligibilityManager.shouldRequestReview(trigger)) {
                Cedar.tag("InAppReview").d("Review not eligible for trigger: $trigger")
                return@launch
            }

            AndroidInAppReview.analytics.track(AnalyticsEvent.ReviewRequested(trigger))
            AndroidInAppReview.reviewService.launchReviewFlow(
                onComplete = {
                    AndroidInAppReview.reviewEligibilityManager.markReviewRequested()
                    AndroidInAppReview.analytics.track(AnalyticsEvent.ReviewCompleted(trigger))
                    Cedar.tag("InAppReview").d("Review flow completed for trigger: $trigger")
                },
                onError = { error ->
                    AndroidInAppReview.analytics.track(
                        AnalyticsEvent.ReviewFailed(trigger, error.message)
                    )
                    Cedar.tag("InAppReview").e("Review flow failed for trigger: $trigger - ${error.message}")
                }
            )
        } catch (e: Exception) {
            Cedar.tag("InAppReview").e("Error requesting review: ${e.message}")
        }
    }
}
