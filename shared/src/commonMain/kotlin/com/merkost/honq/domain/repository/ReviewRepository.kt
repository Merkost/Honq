package com.merkost.honq.domain.repository

import com.merkost.honq.domain.model.IncorrectAnswer
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeIncorrectAnswers(): Flow<List<IncorrectAnswer>>
    suspend fun saveIncorrectAnswers(answers: List<IncorrectAnswer>)
    suspend fun clearIncorrectAnswers()
    suspend fun hasIncorrectAnswers(): Boolean
}
