package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.Result
import com.merkost.honq.data.local.SampleDataSeeder
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.mapper.toEntity
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.QuestionRepository
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val questionApi: QuestionApi,
    private val dispatchers: AppDispatchers,
    private val json: Json,
    private val sampleDataSeeder: SampleDataSeeder
) : QuestionRepository {

    override suspend fun getRandomQuestions(count: Int): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                var questions = localDataSource.getRandomQuestions(count)
                if (questions.isEmpty()) {
                    sampleDataSeeder.seedIfEmpty()
                    questions = localDataSource.getRandomQuestions(count)
                }
                Result.Success(questions)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getMockTestQuestions(): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                var questions = localDataSource.getMockTestQuestions()
                if (questions.isEmpty()) {
                    sampleDataSeeder.seedIfEmpty()
                    questions = localDataSource.getMockTestQuestions()
                }
                Result.Success(questions)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun syncQuestions(): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                val remoteQuestions = questionApi.fetchAllQuestions()
                if (remoteQuestions.isNotEmpty()) {
                    localDataSource.insertQuestions(remoteQuestions.map { it.toEntity(json) })
                } else {
                    sampleDataSeeder.seedIfEmpty()
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                sampleDataSeeder.seedIfEmpty()
                Result.Success(Unit)
            }
        }

    override fun getLastSyncTime(): kotlin.time.Instant? = null
}
