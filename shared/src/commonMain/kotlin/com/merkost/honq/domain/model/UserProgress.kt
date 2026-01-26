package com.merkost.honq.domain.model

import kotlinx.datetime.Instant

data class UserProgress(
    val totalPracticed: Int,
    val correctAnswers: Int,
    val mockTestsTaken: Int,
    val mockTestsPassed: Int,
    val lastPracticeDate: Instant?
) {
    val practiceAccuracy: Float get() = if (totalPracticed == 0) 0f else correctAnswers.toFloat() / totalPracticed
    val mockTestPassRate: Float get() = if (mockTestsTaken == 0) 0f else mockTestsPassed.toFloat() / mockTestsTaken
}
