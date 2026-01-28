package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.repository.MockTestAnswer
import com.merkost.honq.domain.repository.ProgressRepository

class SaveMockTestResultUseCase(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(result: MockTestResult) =
        repository.saveMockTestResult(result)

    suspend operator fun invoke(result: MockTestResult, answers: List<MockTestAnswer>) =
        repository.saveMockTestResultWithAnswers(result, answers)
}
