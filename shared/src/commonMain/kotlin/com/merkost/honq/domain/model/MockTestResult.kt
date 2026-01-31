package com.merkost.honq.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class MockTestResult(
    val id: Long = 0,
    val questionSetId: String = "",
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeTaken: Duration,
    val completedAt: Instant,
    val passPercentage: Int = DEFAULT_PASS_PERCENTAGE
) {
    val passed: Boolean get() = scorePercentage >= passPercentage
    val scorePercentage: Int
        get() = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions) * 100).toInt() else 0

    companion object {
        const val DEFAULT_PASS_PERCENTAGE = 80
    }
}
