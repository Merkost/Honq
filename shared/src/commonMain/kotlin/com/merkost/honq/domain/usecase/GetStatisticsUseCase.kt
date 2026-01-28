package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class Statistics(
    val progress: UserProgress,
    val mockTestResults: List<MockTestResult>,
    val weakestQuestionCount: Int,
    val unansweredQuestionCount: Int
)

class GetStatisticsUseCase(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Statistics> {
        return combine(
            progressRepository.observeUserProgress(),
            progressRepository.observeMockTestResults(),
            progressRepository.observeWeakestQuestionCount(),
            progressRepository.observeUnansweredQuestionCount()
        ) { progress, mockTestResults, weakestCount, unansweredCount ->
            Statistics(
                progress = progress,
                mockTestResults = mockTestResults.sortedByDescending { it.completedAt },
                weakestQuestionCount = weakestCount,
                unansweredQuestionCount = unansweredCount
            )
        }
    }
}
