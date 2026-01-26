package com.merkost.honq.domain.repository

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Question

interface QuestionRepository {
    suspend fun getRandomQuestions(count: Int): Result<List<Question>>
    suspend fun getMockTestQuestions(): Result<List<Question>>
    suspend fun syncQuestions(): Result<Unit>
    fun getLastSyncTime(): kotlin.time.Instant?
}
