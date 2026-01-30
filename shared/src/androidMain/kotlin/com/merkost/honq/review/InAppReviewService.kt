package com.merkost.honq.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.kimplify.cedar.logging.Cedar
import kotlin.coroutines.resume

class InAppReviewService(private val context: Context) {

    private val reviewManager = ReviewManagerFactory.create(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cachedReviewInfo: ReviewInfo? = null

    init {
        scope.launch {
            warmup()
        }
    }

    private suspend fun warmup() {
        try {
            cachedReviewInfo = requestReviewInfo()
            Cedar.tag("InAppReview").d("ReviewInfo pre-warmed successfully")
        } catch (e: Exception) {
            Cedar.tag("InAppReview").e("Failed to pre-warm ReviewInfo: ${e.message}")
        }
    }

    private suspend fun requestReviewInfo(): ReviewInfo = suspendCancellableCoroutine { cont ->
        reviewManager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                cont.resume(reviewInfo)
            }
            .addOnFailureListener { e ->
                cont.cancel(e)
            }
    }

    fun launchReviewFlow(
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onError(IllegalStateException("Context is not an Activity"))
            return
        }

        val reviewInfo = cachedReviewInfo
        if (reviewInfo == null) {
            reviewManager.requestReviewFlow()
                .addOnSuccessListener { info ->
                    reviewManager.launchReviewFlow(activity, info)
                        .addOnCompleteListener {
                            onComplete()
                            scope.launch { warmup() }
                        }
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
            return
        }

        reviewManager.launchReviewFlow(activity, reviewInfo)
            .addOnCompleteListener {
                onComplete()
                cachedReviewInfo = null
                scope.launch { warmup() }
            }
    }
}
