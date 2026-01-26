package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.QuestionRepository

class GetQuestionByIdUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(questionId: String) =
        repository.getQuestionById(questionId)
}
