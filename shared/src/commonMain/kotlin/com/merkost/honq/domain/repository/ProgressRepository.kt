package com.merkost.honq.domain.repository

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.MockTestReviewAnswer
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.model.WeakQuestion
import kotlinx.coroutines.flow.Flow

data class MockTestAnswer(
    val questionId: String,
    val selectedAnswerIndex: Int,
    val wasCorrect: Boolean
)

interface ProgressRepository {
    suspend fun recordAnswer(questionId: String, wasCorrect: Boolean)
    suspend fun saveMockTestResult(result: MockTestResult)
    suspend fun saveMockTestResultWithAnswers(result: MockTestResult, answers: List<MockTestAnswer>)
    fun observeUserProgress(): Flow<UserProgress>
    fun observeMockTestResults(): Flow<List<MockTestResult>>
    suspend fun clearAllProgress()
    suspend fun getWeakestQuestions(limit: Int = 50): List<WeakQuestion>
    suspend fun getUnansweredQuestions(limit: Int = 50): List<Question>
    fun observeWeakestQuestionCount(): Flow<Int>
    fun observeUnansweredQuestionCount(): Flow<Int>
    suspend fun getMockTestIncorrectAnswers(mockTestResultId: Long): List<MockTestReviewAnswer>
}
