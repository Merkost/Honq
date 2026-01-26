package com.merkost.honq.domain.model

data class Question(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val categoryId: String,
    val categoryName: String,
    val questionSetId: String,
    val stateId: String = "nsw",
    val difficulty: Difficulty = Difficulty.MEDIUM
) {
    val correctAnswer: String get() = options[correctIndex]
    val isValid: Boolean get() = options.size >= 2 && correctIndex in options.indices
}
