package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.repository.ProgressRepository

class SaveMockTestResultUseCase(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(result: MockTestResult) =
        repository.saveMockTestResult(result)
}
