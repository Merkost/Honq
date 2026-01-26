package com.merkost.honq.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class MockTestResult(
    val id: Long = 0,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeTaken: Duration,
    val completedAt: Instant
) {
    val passed: Boolean get() = correctAnswers >= (totalQuestions * 0.9).toInt()
    val scorePercentage: Int get() = ((correctAnswers.toFloat() / totalQuestions) * 100).toInt()
}
