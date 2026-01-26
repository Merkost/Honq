package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.repository.QuestionRepository

class GetStatesUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<List<State>> =
        repository.getStates()
}
