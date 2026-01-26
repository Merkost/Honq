package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val dispatchers: AppDispatchers
) : ProgressRepository {

    override suspend fun recordAnswer(questionId: String, wasCorrect: Boolean) =
        withContext(dispatchers.io) {
            localDataSource.recordAnswer(questionId, wasCorrect)
        }

    override suspend fun saveMockTestResult(result: MockTestResult) =
        withContext(dispatchers.io) {
            localDataSource.saveMockTestResult(result)
        }

    override fun observeUserProgress(): Flow<UserProgress> =
        combine(
            localDataSource.observeTotalAnswered(),
            localDataSource.observeCorrectAnswers(),
            localDataSource.observeMockTestCount(),
            localDataSource.observeMockTestPassedCount()
        ) { total, correct, mockTests, mockTestsPassed ->
            UserProgress(
                totalPracticed = total,
                correctAnswers = correct,
                mockTestsTaken = mockTests,
                mockTestsPassed = mockTestsPassed,
                lastPracticeDate = null
            )
        }.flowOn(dispatchers.io)

    override fun observeMockTestResults(): Flow<List<MockTestResult>> =
        localDataSource.observeMockTestResults().flowOn(dispatchers.io)
}
