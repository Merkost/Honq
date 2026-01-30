package com.merkost.honq.domain.model

data class CategoryProgress(
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int = 0,
    val totalAttempts: Int = 0
) {
    val completionPercent: Float
        get() = if (totalQuestions > 0) answeredQuestions.toFloat() / totalQuestions else 0f
    val accuracyPercent: Int
        get() = if (totalAttempts > 0) ((correctAnswers.toFloat() / totalAttempts) * 100).toInt() else 0
}
