package com.merkost.honq.data.repository

import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFavoritesRepository(
    private val questionsProvider: () -> List<Question>
) : FavoritesRepository {

    private val favoriteIds = MutableStateFlow(setOf("1", "5", "7", "12"))

    override fun observeFavoriteQuestions(): Flow<List<Question>> =
        favoriteIds.map { ids ->
            questionsProvider().filter { it.id in ids }
        }

    override fun observeFavoriteQuestionIds(): Flow<Set<String>> = favoriteIds

    override suspend fun toggleFavorite(questionId: String) {
        favoriteIds.update { current ->
            if (questionId in current) current - questionId else current + questionId
        }
    }
}
