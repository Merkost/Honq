package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteQuestionsUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Question>> =
        repository.observeFavoriteQuestions()
}
