package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository,
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
        questionSetSelectionRepository.selectedQuestionSetId
            .flatMapLatest { questionSetId ->
                questionSetId?.let { observeProgressForQuestionSet(it) }
                    ?: flowOf(UserProgress.EMPTY)
            }
            .flowOn(dispatchers.io)

    private fun observeProgressForQuestionSet(questionSetId: String): Flow<UserProgress> {
        val questionSetStats = combine(
            localDataSource.observeQuestionCountByQuestionSet(questionSetId),
            localDataSource.observeUniqueQuestionsAnsweredByQuestionSet(questionSetId),
            localDataSource.observeTotalAnsweredByQuestionSet(questionSetId),
            localDataSource.observeCorrectAnswersByQuestionSet(questionSetId)
        ) { total, unique, practiced, correct -> QuestionSetStats(total, unique, practiced, correct) }

        val mockTestStats = combine(
            localDataSource.observeMockTestCount(),
            localDataSource.observeMockTestPassedCount()
        ) { taken, passed -> MockTestStats(taken, passed) }

        return combine(questionSetStats, mockTestStats) { qs, mt ->
            UserProgress(
                totalQuestions = qs.totalQuestions,
                uniqueQuestionsAnswered = qs.uniqueAnswered,
                totalPracticed = qs.totalPracticed,
                correctAnswers = qs.correctAnswers,
                mockTestsTaken = mt.taken,
                mockTestsPassed = mt.passed,
                lastPracticeDate = null
            )
        }
    }

    private data class QuestionSetStats(
        val totalQuestions: Int,
        val uniqueAnswered: Int,
        val totalPracticed: Int,
        val correctAnswers: Int
    )

    private data class MockTestStats(val taken: Int, val passed: Int)

    override fun observeMockTestResults(): Flow<List<MockTestResult>> =
        localDataSource.observeMockTestResults().flowOn(dispatchers.io)

    override suspend fun clearAllProgress() = withContext(dispatchers.io) {
        localDataSource.clearAllProgress()
    }
}
