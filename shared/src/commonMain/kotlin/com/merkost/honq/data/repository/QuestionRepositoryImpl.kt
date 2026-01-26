package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.Result
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.mapper.toEntity
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.LicenseStage
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.repository.QuestionRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val questionApi: QuestionApi,
    private val dispatchers: AppDispatchers,
    private val json: Json,
    private val syncPreferences: SyncPreferences
) : QuestionRepository {

    companion object {
        private const val DEFAULT_STATE_ID = "nsw"
        private const val DEFAULT_LICENSE_TYPE_ID = "car"
        private const val DEFAULT_LICENSE_STAGE_ID = "learner"
        private const val DEFAULT_ASSESSMENT_TYPE_ID = "knowledge_test"
        private const val DEFAULT_MOCK_TEST_COUNT = 45
    }

    private suspend fun resolveDefaultQuestionSetId(stateId: String): String? {
        var questionSets = localDataSource.getQuestionSetsByState(stateId)
        if (questionSets.isEmpty()) {
            syncStates()
            questionSets = localDataSource.getQuestionSetsByState(stateId)
        }
        return questionSets.firstOrNull {
            it.isActive &&
                it.licenseTypeId == DEFAULT_LICENSE_TYPE_ID &&
                it.licenseStageId == DEFAULT_LICENSE_STAGE_ID &&
                it.assessmentTypeId == DEFAULT_ASSESSMENT_TYPE_ID
        }?.id
            ?: questionSets.firstOrNull { it.isActive }?.id
            ?: questionSets.firstOrNull()?.id
    }

    override suspend fun getRandomQuestions(count: Int): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                val questionSetId = resolveDefaultQuestionSetId(DEFAULT_STATE_ID)
                    ?: return@withContext Result.Success(localDataSource.getRandomQuestions(count))
                getRandomQuestions(questionSetId, count, null)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getRandomQuestions(
        questionSetId: String,
        count: Int,
        categoryId: String?
    ): Result<List<Question>> = withContext(dispatchers.io) {
        try {
            var questions = if (categoryId != null) {
                localDataSource.getRandomQuestionsByQuestionSetAndCategory(questionSetId, categoryId, count)
            } else {
                localDataSource.getRandomQuestionsByQuestionSet(questionSetId, count)
            }

            if (questions.isEmpty()) {
                syncQuestions(questionSetId)
                questions = if (categoryId != null) {
                    localDataSource.getRandomQuestionsByQuestionSetAndCategory(questionSetId, categoryId, count)
                } else {
                    localDataSource.getRandomQuestionsByQuestionSet(questionSetId, count)
                }
            }

            if (questions.isEmpty()) {
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
                val questionSetId = resolveDefaultQuestionSetId(DEFAULT_STATE_ID)
                    ?: return@withContext Result.Success(localDataSource.getMockTestQuestions())
                getMockTestQuestions(questionSetId)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getMockTestQuestions(questionSetId: String): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                val questionSet = localDataSource.getQuestionSetById(questionSetId)
                val questionCount = questionSet?.mockTestQuestionCount ?: DEFAULT_MOCK_TEST_COUNT

                var questions = localDataSource.getMockTestQuestionsByQuestionSet(questionSetId, questionCount)

                if (questions.isEmpty()) {
                    syncQuestions(questionSetId)
                    questions = localDataSource.getMockTestQuestionsByQuestionSet(questionSetId, questionCount)
                }

                if (questions.isEmpty()) {
                    questions = localDataSource.getMockTestQuestions()
                }

                Result.Success(questions)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getQuestionById(questionId: String): Result<Question?> =
        withContext(dispatchers.io) {
            try {
                Result.Success(localDataSource.getQuestionById(questionId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun syncQuestions(): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                val questionSetId = resolveDefaultQuestionSetId(DEFAULT_STATE_ID)
                    ?: return@withContext Result.Success(Unit)
                syncQuestions(questionSetId)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun syncQuestions(questionSetId: String): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                val lastSync = syncPreferences.getLastSyncTime(questionSetId)
                val localCount = localDataSource.getQuestionCountByQuestionSet(questionSetId)

                val remoteQuestions = if (lastSync != null && localCount > 0) {
                    val lastUpdatedAt = localDataSource.getLastUpdatedAt(questionSetId)
                    if (lastUpdatedAt != null) {
                        questionApi.fetchUpdatedQuestions(questionSetId, lastUpdatedAt)
                    } else {
                        questionApi.fetchQuestionsByQuestionSet(questionSetId)
                    }
                } else {
                    localDataSource.deleteQuestionsByQuestionSet(questionSetId)
                    questionApi.fetchQuestionsByQuestionSet(questionSetId)
                }

                if (remoteQuestions.isNotEmpty()) {
                    localDataSource.upsertQuestions(remoteQuestions.map { it.toEntity(json) })
                    val now =
                        kotlin.time.Instant.fromEpochMilliseconds(
                            Clock.System.now().toEpochMilliseconds()
                        )
                    syncPreferences.setLastSyncTime(questionSetId, now)
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getStates(): Result<List<State>> = withContext(dispatchers.io) {
        try {
            var states = localDataSource.getStates()
            syncStates()
            val refreshedStates = localDataSource.getStates()
            if (refreshedStates.isNotEmpty()) {
                states = refreshedStates
            }
            Result.Success(states)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncStates(): Result<Unit> = withContext(dispatchers.io) {
        try {
            val remoteStates = questionApi.fetchStates(includeInactive = true)
            if (remoteStates.isNotEmpty()) {
                localDataSource.insertStates(remoteStates.map { it.toEntity() })
            }
            val remoteLicenseTypes = questionApi.fetchLicenseTypes(includeInactive = true)
            if (remoteLicenseTypes.isNotEmpty()) {
                localDataSource.upsertLicenseTypes(remoteLicenseTypes.map { it.toEntity() })
            }

            val remoteLicenseStages = questionApi.fetchLicenseStages(includeInactive = true)
            if (remoteLicenseStages.isNotEmpty()) {
                localDataSource.upsertLicenseStages(remoteLicenseStages.map { it.toEntity() })
            }

            val remoteAssessmentTypes = questionApi.fetchAssessmentTypes(includeInactive = true)
            if (remoteAssessmentTypes.isNotEmpty()) {
                localDataSource.upsertAssessmentTypes(remoteAssessmentTypes.map { it.toEntity() })
            }

            val remoteQuestionSets = questionApi.fetchQuestionSets(includeInactive = true)
            if (remoteQuestionSets.isNotEmpty()) {
                localDataSource.upsertQuestionSets(remoteQuestionSets.map { it.toEntity() })
            }

            val remoteCategories = questionApi.fetchCategories(includeInactive = true)
            if (remoteCategories.isNotEmpty()) {
                localDataSource.upsertCategories(remoteCategories.map { it.toEntity() })
            }

            val remoteQuestionSetCategories = questionApi.fetchQuestionSetCategories(includeInactive = true)
            if (remoteQuestionSetCategories.isNotEmpty()) {
                localDataSource.upsertQuestionSetCategories(remoteQuestionSetCategories.map { it.toEntity() })
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLicenseTypes(): Result<List<LicenseType>> = withContext(dispatchers.io) {
        try {
            Result.Success(localDataSource.getLicenseTypes())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLicenseStages(): Result<List<LicenseStage>> = withContext(dispatchers.io) {
        try {
            Result.Success(localDataSource.getLicenseStages())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAssessmentTypes(): Result<List<AssessmentType>> = withContext(dispatchers.io) {
        try {
            Result.Success(localDataSource.getAssessmentTypes())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getQuestionSetsByState(stateId: String): Result<List<QuestionSet>> =
        withContext(dispatchers.io) {
            try {
                Result.Success(localDataSource.getQuestionSetsByState(stateId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getQuestionSetById(questionSetId: String): Result<QuestionSet?> =
        withContext(dispatchers.io) {
            try {
                Result.Success(localDataSource.getQuestionSetById(questionSetId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getCategoriesByQuestionSet(questionSetId: String): Result<List<Category>> =
        withContext(dispatchers.io) {
            try {
                Result.Success(localDataSource.getCategoriesForQuestionSet(questionSetId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? =
        syncPreferences.getLastSyncTime(questionSetId)
}
