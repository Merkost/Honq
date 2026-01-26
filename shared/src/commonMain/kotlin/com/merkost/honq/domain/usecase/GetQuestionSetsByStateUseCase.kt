package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.repository.QuestionRepository

class GetQuestionSetsByStateUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(stateId: String): Result<List<QuestionSet>> =
        repository.getQuestionSetsByState(stateId)
}
