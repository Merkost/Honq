package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.repository.QuestionRepository

class SyncQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        repository.syncQuestions()
}
