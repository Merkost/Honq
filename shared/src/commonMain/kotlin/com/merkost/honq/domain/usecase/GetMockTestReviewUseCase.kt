package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.MockTestReviewAnswer
import com.merkost.honq.domain.repository.ProgressRepository

class GetMockTestReviewUseCase(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(mockTestResultId: Long): Result<List<MockTestReviewAnswer>> =
        runCatching {
            progressRepository.getMockTestIncorrectAnswers(mockTestResultId)
        }
}
