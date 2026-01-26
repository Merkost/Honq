package com.merkost.honq.domain.model

data class IncorrectAnswer(
    val question: Question,
    val selectedAnswerIndex: Int
) {
    val selectedAnswer: String get() = question.options[selectedAnswerIndex]
}
