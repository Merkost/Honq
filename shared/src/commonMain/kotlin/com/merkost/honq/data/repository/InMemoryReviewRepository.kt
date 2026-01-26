package com.merkost.honq.data.repository

import com.merkost.honq.domain.model.IncorrectAnswer
import com.merkost.honq.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryReviewRepository : ReviewRepository {

    private val incorrectAnswersFlow = MutableStateFlow<List<IncorrectAnswer>>(emptyList())

    override fun observeIncorrectAnswers(): Flow<List<IncorrectAnswer>> =
        incorrectAnswersFlow.asStateFlow()

    override suspend fun saveIncorrectAnswers(answers: List<IncorrectAnswer>) {
        incorrectAnswersFlow.value = answers
    }

    override suspend fun clearIncorrectAnswers() {
        incorrectAnswersFlow.value = emptyList()
    }

    override suspend fun hasIncorrectAnswers(): Boolean =
        incorrectAnswersFlow.value.isNotEmpty()
}
