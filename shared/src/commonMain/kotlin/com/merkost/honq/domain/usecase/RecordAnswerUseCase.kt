package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.ProgressRepository

class RecordAnswerUseCase(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(questionId: String, wasCorrect: Boolean) =
        repository.recordAnswer(questionId, wasCorrect)
}
