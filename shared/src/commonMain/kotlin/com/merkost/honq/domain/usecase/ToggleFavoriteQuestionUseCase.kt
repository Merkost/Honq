package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.FavoritesRepository

class ToggleFavoriteQuestionUseCase(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(questionId: String) =
        repository.toggleFavorite(questionId)
}
