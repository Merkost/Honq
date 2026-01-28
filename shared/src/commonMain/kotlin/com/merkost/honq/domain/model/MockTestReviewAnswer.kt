package com.merkost.honq.domain.model

data class MockTestReviewAnswer(
    val question: Question,
    val selectedAnswerIndex: Int,
    val wasCorrect: Boolean
) {
    val selectedAnswer: String get() = question.options.getOrElse(selectedAnswerIndex) { "" }
}
