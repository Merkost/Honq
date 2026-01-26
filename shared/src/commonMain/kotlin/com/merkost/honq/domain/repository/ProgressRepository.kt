package com.merkost.honq.domain.repository

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    suspend fun recordAnswer(questionId: String, wasCorrect: Boolean)
    suspend fun saveMockTestResult(result: MockTestResult)
    fun observeUserProgress(): Flow<UserProgress>
    fun observeMockTestResults(): Flow<List<MockTestResult>>
    suspend fun clearAllProgress()
}
