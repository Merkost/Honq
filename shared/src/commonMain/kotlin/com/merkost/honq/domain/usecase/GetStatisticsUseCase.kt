package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class Statistics(
    val progress: UserProgress,
    val mockTestResults: List<MockTestResult>
)

class GetStatisticsUseCase(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Statistics> {
        return combine(
            progressRepository.observeUserProgress(),
            progressRepository.observeMockTestResults()
        ) { progress, mockTestResults ->
            Statistics(
                progress = progress,
                mockTestResults = mockTestResults.sortedByDescending { it.completedAt }
            )
        }
    }
}
