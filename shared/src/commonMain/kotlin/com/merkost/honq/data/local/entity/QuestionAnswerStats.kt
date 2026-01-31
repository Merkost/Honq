package com.merkost.honq.data.local.entity

data class QuestionAnswerStats(
    val questionId: String,
    val totalAttempts: Int,
    val wrongCount: Int,
    val lastAnsweredAt: String
)
