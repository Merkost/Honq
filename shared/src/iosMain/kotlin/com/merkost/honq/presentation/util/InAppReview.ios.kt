package com.merkost.honq.presentation.util

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.ReviewEligibilityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

private object IOSInAppReview : KoinComponent {
    val reviewEligibilityManager: ReviewEligibilityManager by inject()
    val analytics: Analytics by inject()
}

actual fun requestInAppReview(trigger: String) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            if (!IOSInAppReview.reviewEligibilityManager.shouldRequestReview(trigger)) {
                return@launch
            }

            IOSInAppReview.analytics.track(AnalyticsEvent.ReviewRequested(trigger))

            val windowScene = UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull()

            if (windowScene != null) {
                SKStoreReviewController.requestReviewInScene(windowScene)
            } else {
                SKStoreReviewController.requestReview()
            }

            IOSInAppReview.reviewEligibilityManager.markReviewRequested()
            IOSInAppReview.analytics.track(AnalyticsEvent.ReviewCompleted(trigger))
        } catch (e: Exception) {
            IOSInAppReview.analytics.track(
                AnalyticsEvent.ReviewFailed(trigger, e.message)
            )
        }
    }
}
