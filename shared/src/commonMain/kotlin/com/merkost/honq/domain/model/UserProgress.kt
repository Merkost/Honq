package com.merkost.honq.domain.model

import kotlinx.datetime.Instant

data class UserProgress(
    val totalQuestions: Int,
    val uniqueQuestionsAnswered: Int,
    val totalPracticed: Int,
    val correctAnswers: Int,
    val mockTestsTaken: Int,
    val mockTestsPassed: Int,
    val lastPracticeDate: Instant?
) {
    val practiceAccuracy: Float
        get() = if (totalPracticed == 0) 0f else correctAnswers.toFloat() / totalPracticed

    val mockTestPassRate: Float
        get() = if (mockTestsTaken == 0) 0f else mockTestsPassed.toFloat() / mockTestsTaken

    val completionProgress: Float
        get() = if (totalQuestions == 0) 0f else uniqueQuestionsAnswered.toFloat() / totalQuestions

    companion object {
        val EMPTY = UserProgress(
            totalQuestions = 0,
            uniqueQuestionsAnswered = 0,
            totalPracticed = 0,
            correctAnswers = 0,
            mockTestsTaken = 0,
            mockTestsPassed = 0,
            lastPracticeDate = null
        )
    }
}
