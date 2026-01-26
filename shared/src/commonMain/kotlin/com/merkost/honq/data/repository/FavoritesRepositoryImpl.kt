package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val dispatchers: AppDispatchers
) : FavoritesRepository {
    override fun observeFavoriteQuestions(): Flow<List<Question>> =
        localDataSource.observeFavoriteQuestions().flowOn(dispatchers.io)

    override fun observeFavoriteQuestionIds(): Flow<Set<String>> =
        localDataSource.observeFavoriteQuestionIds().flowOn(dispatchers.io)

    override suspend fun toggleFavorite(questionId: String) =
        withContext(dispatchers.io) {
            localDataSource.toggleFavorite(questionId)
        }
}
