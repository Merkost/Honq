package com.merkost.honq.data.local.datasource

import com.merkost.honq.data.local.db.AnswerHistoryDao
import com.merkost.honq.data.local.db.MockTestResultDao
import com.merkost.honq.data.local.db.QuestionDao
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.MockTestResultEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.mapper.toDomain
import com.merkost.honq.data.local.mapper.toEntity
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class QuestionLocalDataSource(
    private val questionDao: QuestionDao,
    private val answerHistoryDao: AnswerHistoryDao,
    private val mockTestResultDao: MockTestResultDao,
    private val json: Json
) {
    suspend fun getRandomQuestions(count: Int): List<Question> =
        questionDao.getRandomQuestions(count).map { it.toDomain(json) }

    suspend fun getMockTestQuestions(): List<Question> =
        questionDao.getMockTestQuestions().map { it.toDomain(json) }

    suspend fun insertQuestions(questions: List<QuestionEntity>) =
        questionDao.insertAll(questions)

    suspend fun recordAnswer(questionId: String, wasCorrect: Boolean) {
        answerHistoryDao.insert(
            AnswerHistoryEntity(
                questionId = questionId,
                wasCorrect = wasCorrect,
                answeredAt = kotlin.time.Clock.System.now().toString()
            )
        )
    }

    fun observeTotalAnswered(): Flow<Int> = answerHistoryDao.observeTotalCount()

    fun observeCorrectAnswers(): Flow<Int> = answerHistoryDao.observeCorrectCount()

    suspend fun saveMockTestResult(result: MockTestResult) {
        mockTestResultDao.insert(result.toEntity())
    }

    fun observeMockTestResults(): Flow<List<MockTestResult>> =
        mockTestResultDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeMockTestCount(): Flow<Int> = mockTestResultDao.observeTotalCount()

    fun observeMockTestPassedCount(): Flow<Int> = mockTestResultDao.observePassedCount()
}
