package com.merkost.honq.domain.repository

import com.merkost.honq.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteQuestions(): Flow<List<Question>>
    fun observeFavoriteQuestionIds(): Flow<Set<String>>
    suspend fun toggleFavorite(questionId: String)
}
