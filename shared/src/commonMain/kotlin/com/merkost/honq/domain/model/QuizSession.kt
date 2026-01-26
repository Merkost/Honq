package com.merkost.honq.domain.model

import kotlinx.datetime.Instant

data class QuizSession(
    val questions: List<Question>,
    val answers: Map<String, Int> = emptyMap(),
    val currentIndex: Int = 0,
    val startTime: Instant? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isComplete: Boolean get() = currentIndex >= questions.size
    val progress: Float get() = if (questions.isEmpty()) 0f else currentIndex.toFloat() / questions.size
    val answeredCount: Int get() = answers.size
}
