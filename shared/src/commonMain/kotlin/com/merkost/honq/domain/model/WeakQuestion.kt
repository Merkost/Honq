package com.merkost.honq.domain.model

data class WeakQuestion(
    val question: Question,
    val wrongCount: Int,
    val totalAttempts: Int
) {
    val wrongPercentage: Float
        get() = if (totalAttempts == 0) 0f else wrongCount.toFloat() / totalAttempts
}
