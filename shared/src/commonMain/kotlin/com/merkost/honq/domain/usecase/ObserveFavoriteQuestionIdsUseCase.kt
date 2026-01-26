package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteQuestionIdsUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<Set<String>> =
        repository.observeFavoriteQuestionIds()
}
