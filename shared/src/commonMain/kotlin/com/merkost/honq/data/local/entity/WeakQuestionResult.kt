package com.merkost.honq.data.local.entity

data class WeakQuestionResult(
    val questionId: String,
    val totalAttempts: Int,
    val wrongCount: Int
)
