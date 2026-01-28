package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.local.entity.MockTestAnswerEntity
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.MockTestReviewAnswer
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.model.WeakQuestion
import com.merkost.honq.domain.repository.MockTestAnswer
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.kimplify.cedar.logging.Cedar

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

    override suspend fun saveMockTestResultWithAnswers(
        result: MockTestResult,
        answers: List<MockTestAnswer>
    ) = withContext(dispatchers.io) {
        Cedar.tag("Progress").d("saveMockTestResultWithAnswers: correct=${result.correctAnswers}/${result.totalQuestions}, answers=${answers.size}")
        localDataSource.saveMockTestResult(result)
        val resultId = localDataSource.getLastMockTestResultId()
        if (resultId == null) {
            Cedar.tag("Progress").w("saveMockTestResultWithAnswers: could not get last result ID, skipping answer save")
            return@withContext
        }
        val answerEntities = answers.map {
            MockTestAnswerEntity(
                mockTestResultId = resultId,
                questionId = it.questionId,
                selectedAnswerIndex = it.selectedAnswerIndex,
                wasCorrect = it.wasCorrect
            )
        }
        localDataSource.saveMockTestAnswers(resultId, answerEntities)
        Cedar.tag("Progress").d("saveMockTestResultWithAnswers: saved resultId=$resultId with ${answerEntities.size} answers")
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
        Cedar.tag("Progress").d("clearAllProgress: clearing all answer history and mock test results")
        localDataSource.clearAllProgress()
    }

    override suspend fun getWeakestQuestions(limit: Int): List<WeakQuestion> =
        withContext(dispatchers.io) {
            val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
            if (questionSetId == null) {
                Cedar.tag("Progress").w("getWeakestQuestions: no question set selected")
                return@withContext emptyList()
            }
            val result = localDataSource.getWeakestQuestions(questionSetId, limit).map { (question, result) ->
                WeakQuestion(
                    question = question,
                    wrongCount = result.wrongCount,
                    totalAttempts = result.totalAttempts
                )
            }
            Cedar.tag("Progress").d("getWeakestQuestions: returned ${result.size} weak questions")
            result
        }

    override suspend fun getUnansweredQuestions(limit: Int): List<Question> =
        withContext(dispatchers.io) {
            val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
            if (questionSetId == null) {
                Cedar.tag("Progress").w("getUnansweredQuestions: no question set selected")
                return@withContext emptyList()
            }
            val result = localDataSource.getUnansweredQuestions(questionSetId, limit)
            Cedar.tag("Progress").d("getUnansweredQuestions: returned ${result.size} unanswered questions")
            result
        }

    override fun observeWeakestQuestionCount(): Flow<Int> =
        questionSetSelectionRepository.selectedQuestionSetId
            .flatMapLatest { questionSetId ->
                questionSetId?.let { localDataSource.observeWeakestQuestionCount(it) }
                    ?: flowOf(0)
            }
            .flowOn(dispatchers.io)

    override fun observeUnansweredQuestionCount(): Flow<Int> =
        questionSetSelectionRepository.selectedQuestionSetId
            .flatMapLatest { questionSetId ->
                questionSetId?.let { localDataSource.observeUnansweredQuestionCount(it) }
                    ?: flowOf(0)
            }
            .flowOn(dispatchers.io)

    override suspend fun getMockTestIncorrectAnswers(mockTestResultId: Long): List<MockTestReviewAnswer> =
        withContext(dispatchers.io) {
            localDataSource.getMockTestIncorrectAnswers(mockTestResultId)
        }
}
