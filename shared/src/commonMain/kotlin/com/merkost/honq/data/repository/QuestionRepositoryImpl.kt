package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.Result
import com.merkost.honq.core.util.runLogged
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.local.seed.mapper.toEntity
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.CategoryProgress
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.repository.QuestionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.kimplify.cedar.logging.Cedar
import kotlin.time.Clock

class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val questionApi: QuestionApi,
    private val dispatchers: AppDispatchers,
    private val json: Json,
    private val syncPreferences: SyncPreferences,
    private val dataSyncManager: DataSyncManager
) : QuestionRepository {

    companion object {
        private const val DEFAULT_STATE_ID = "nsw"
        private const val DEFAULT_LICENSE_TYPE_ID = "car"
        private const val DEFAULT_ASSESSMENT_TYPE_ID = "knowledge_test"
        private const val DEFAULT_MOCK_TEST_COUNT = 45
    }

    private suspend fun resolveDefaultQuestionSetId(stateId: String): String? {
        val questionSets = localDataSource.getQuestionSetsByState(stateId)
        return questionSets.firstOrNull {
            it.isActive &&
                it.licenseTypeId == DEFAULT_LICENSE_TYPE_ID &&
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
                Cedar.tag("QuestionRepo").e("getRandomQuestions failed: ${e.message}", e)
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
                Cedar.tag("QuestionRepo").w("getRandomQuestions: no questions for questionSet=$questionSetId, falling back to all")
                questions = localDataSource.getRandomQuestions(count)
            }

            Result.Success(questions)
        } catch (e: Exception) {
            Cedar.tag("QuestionRepo").e("getRandomQuestions(questionSet) failed: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun getMockTestQuestions(): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                val questionSetId = resolveDefaultQuestionSetId(DEFAULT_STATE_ID)
                    ?: return@withContext Result.Success(localDataSource.getMockTestQuestions(DEFAULT_MOCK_TEST_COUNT))
                getMockTestQuestions(questionSetId)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("getMockTestQuestions failed: ${e.message}", e)
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
                    Cedar.tag("QuestionRepo").w("getMockTestQuestions: no questions for questionSet=$questionSetId, falling back to all")
                    questions = localDataSource.getMockTestQuestions(questionCount)
                }

                Result.Success(questions)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("getMockTestQuestions(questionSet=$questionSetId) failed: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun getQuestionById(questionId: String): Result<Question?> =
        withContext(dispatchers.io) {
            runLogged("QuestionRepo", "getQuestionById($questionId)") {
                localDataSource.getQuestionById(questionId)
            }
        }

    override suspend fun searchQuestions(questionSetId: String, query: String): Result<List<Question>> =
        withContext(dispatchers.io) {
            try {
                if (query.isBlank()) {
                    return@withContext Result.Success(emptyList())
                }
                val questions = localDataSource.searchQuestions(questionSetId, query)
                Cedar.tag("QuestionRepo").d("searchQuestions: query='$query' returned ${questions.size} results")
                Result.Success(questions)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("searchQuestions failed for query='$query': ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun syncQuestions(): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                val questionSetId = resolveDefaultQuestionSetId(DEFAULT_STATE_ID)
                if (questionSetId == null) {
                    Cedar.tag("QuestionRepo").w("syncQuestions: no default question set found, skipping")
                    return@withContext Result.Success(Unit)
                }
                syncQuestions(questionSetId)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("syncQuestions failed: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun syncQuestions(questionSetId: String): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                Cedar.tag("QuestionRepo").d("syncQuestions: starting for questionSet=$questionSetId")
                val lastSync = syncPreferences.getLastSyncTime(questionSetId)
                val localCount = localDataSource.getQuestionCountByQuestionSet(questionSetId)
                Cedar.tag("QuestionRepo").d("syncQuestions: lastSync=$lastSync, localCount=$localCount")

                val isFullFetch = lastSync == null || localCount == 0
                val remoteQuestions = if (!isFullFetch) {
                    val lastUpdatedAt = localDataSource.getLastUpdatedAt(questionSetId)
                    if (lastUpdatedAt != null) {
                        questionApi.fetchUpdatedQuestions(questionSetId, lastUpdatedAt)
                    } else {
                        questionApi.fetchQuestionsByQuestionSet(questionSetId)
                    }
                } else {
                    questionApi.fetchQuestionsByQuestionSet(questionSetId)
                }

                if (remoteQuestions.isNotEmpty()) {
                    val entities = remoteQuestions.map { it.toEntity(json) }
                    Cedar.tag("QuestionRepo").d("syncQuestions: upserting ${entities.size} questions")
                    localDataSource.upsertQuestions(entities)

                    if (isFullFetch) {
                        // Clean up stale local questions not present in the remote set
                        val remoteIds = entities.map { it.id }.toSet()
                        localDataSource.deleteStaleQuestions(questionSetId, remoteIds)
                    }

                    val now = kotlin.time.Instant.fromEpochMilliseconds(
                        Clock.System.now().toEpochMilliseconds()
                    )
                    syncPreferences.setLastSyncTime(questionSetId, now)
                } else {
                    Cedar.tag("QuestionRepo").w("syncQuestions: API returned 0 questions for questionSet=$questionSetId")
                }
                Cedar.tag("QuestionRepo").d("syncQuestions: completed for questionSet=$questionSetId")
                Result.Success(Unit)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("syncQuestions($questionSetId) failed: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun getStates(): Result<List<State>> = withContext(dispatchers.io) {
        try {
            val states = localDataSource.getStates()
            Cedar.tag("QuestionRepo").d("getStates: returned ${states.size} states")
            Result.Success(states)
        } catch (e: Exception) {
            Cedar.tag("QuestionRepo").e("getStates failed: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun syncStates(): Result<Unit> = withContext(dispatchers.io) {
        try {
            Cedar.tag("QuestionRepo").d("syncStates: starting full state sync...")

            // Fetch all reference data in parallel
            val (states, licenseTypes, assessmentTypes, questionSets, categories, qsCategories) =
                coroutineScope {
                    val statesDeferred = async { questionApi.fetchStates(includeInactive = true) }
                    val licenseTypesDeferred = async { questionApi.fetchLicenseTypes(includeInactive = true) }
                    val assessmentTypesDeferred = async { questionApi.fetchAssessmentTypes(includeInactive = true) }
                    val questionSetsDeferred = async { questionApi.fetchQuestionSets(includeInactive = true) }
                    val categoriesDeferred = async { questionApi.fetchCategories(includeInactive = true) }
                    val qsCategoriesDeferred = async {
                        try {
                            questionApi.fetchQuestionSetCategories(includeInactive = true)
                        } catch (e: Exception) {
                            Cedar.tag("SyncCategories").w("question_set_categories fetch failed: ${e.message}")
                            emptyList()
                        }
                    }
                    SyncData(
                        statesDeferred.await(),
                        licenseTypesDeferred.await(),
                        assessmentTypesDeferred.await(),
                        questionSetsDeferred.await(),
                        categoriesDeferred.await(),
                        qsCategoriesDeferred.await()
                    )
                }

            // Upsert all reference data in a transaction
            // Order matters due to foreign keys: independent tables first, then dependent ones
            Cedar.tag("QuestionRepo").d("syncStates: upserting ${states.size} states, ${licenseTypes.size} license types, ${assessmentTypes.size} assessment types, ${questionSets.size} question sets, ${categories.size} categories, ${qsCategories.size} question set categories")
            localDataSource.upsertAllReferenceData(
                states = states.map { it.toEntity() },
                licenseTypes = licenseTypes.map { it.toEntity() },
                assessmentTypes = assessmentTypes.map { it.toEntity() },
                categories = categories.map { it.toEntity() },
                questionSets = questionSets.map { it.toEntity() },
                questionSetCategories = qsCategories.map { it.toEntity() }
            )

            Cedar.tag("QuestionRepo").d("syncStates: completed")
            Result.Success(Unit)
        } catch (e: Exception) {
            Cedar.tag("QuestionRepo").e("syncStates FAILED: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun getLicenseTypes(): Result<List<LicenseType>> = withContext(dispatchers.io) {
        try {
            val types = localDataSource.getLicenseTypes()
            Cedar.tag("QuestionRepo").d("getLicenseTypes: returned ${types.size} types")
            Result.Success(types)
        } catch (e: Exception) {
            Cedar.tag("QuestionRepo").e("getLicenseTypes failed: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun getAssessmentTypes(): Result<List<AssessmentType>> =
        withContext(dispatchers.io) {
            runLogged("QuestionRepo", "getAssessmentTypes") {
                localDataSource.getAssessmentTypes()
            }
        }

    override suspend fun getQuestionSetsByState(stateId: String): Result<List<QuestionSet>> =
        withContext(dispatchers.io) {
            try {
                val sets = localDataSource.getQuestionSetsByState(stateId)
                Cedar.tag("QuestionRepo").d("getQuestionSetsByState($stateId): returned ${sets.size} sets")
                Result.Success(sets)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("getQuestionSetsByState($stateId) failed: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun getQuestionSetById(questionSetId: String): Result<QuestionSet?> =
        withContext(dispatchers.io) {
            runLogged("QuestionRepo", "getQuestionSetById($questionSetId)") {
                localDataSource.getQuestionSetById(questionSetId)
            }
        }

    override suspend fun getCategoriesByQuestionSet(questionSetId: String): Result<List<Category>> =
        withContext(dispatchers.io) {
            try {
                val categories = localDataSource.getCategoriesForQuestionSet(questionSetId)
                Cedar.tag("Categories").d("getCategoriesByQuestionSet($questionSetId) returned ${categories.size} categories")
                Result.Success(categories)
            } catch (e: Exception) {
                Cedar.tag("Categories").e("getCategoriesByQuestionSet FAILED: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun fullSync(questionSetId: String?, remoteVersion: Int?): Result<Unit> =
        withContext(dispatchers.io) {
            try {
                Cedar.tag("QuestionRepo").d("fullSync: starting (questionSetId=$questionSetId, remoteVersion=$remoteVersion)")
                val syncResult = syncStates()
                if (syncResult is Result.Error) {
                    Cedar.tag("QuestionRepo").w("fullSync: syncStates failed: ${syncResult.exception.message}")
                    return@withContext syncResult
                }
                if (questionSetId != null) {
                    val questionsResult = syncQuestions(questionSetId)
                    if (questionsResult is Result.Error) {
                        Cedar.tag("QuestionRepo").w("fullSync: syncQuestions failed: ${questionsResult.exception.message}")
                        return@withContext questionsResult
                    }
                }
                if (remoteVersion != null) {
                    dataSyncManager.markSyncCompleted(remoteVersion)
                }
                Cedar.tag("QuestionRepo").d("fullSync: completed")
                Result.Success(Unit)
            } catch (e: Exception) {
                Cedar.tag("QuestionRepo").e("fullSync failed: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun getAllActiveCategories(): Result<List<Category>> =
        withContext(dispatchers.io) {
            try {
                val categories = localDataSource.getAllActiveCategories()
                Cedar.tag("Categories").d("getAllActiveCategories() returned ${categories.size} categories: ${categories.map { "${it.id}(active=${it.isActive})" }}")
                Result.Success(categories)
            } catch (e: Exception) {
                Cedar.tag("Categories").e("getAllActiveCategories FAILED: ${e.message}", e)
                Result.Error(e)
            }
        }

    override suspend fun getCategoryProgress(questionSetId: String): Result<Map<String, CategoryProgress>> =
        withContext(dispatchers.io) {
            runLogged("QuestionRepo", "getCategoryProgress($questionSetId)") {
                localDataSource.getCategoryProgress(questionSetId)
            }
        }

    override suspend fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? =
        syncPreferences.getLastSyncTime(questionSetId)

    override suspend fun isDatabaseEmpty(): Boolean = withContext(dispatchers.io) {
        localDataSource.getStates().isEmpty()
    }

    override suspend fun hasQuestionsForSet(questionSetId: String): Boolean = withContext(dispatchers.io) {
        localDataSource.getQuestionCountByQuestionSet(questionSetId) > 0
    }
}

private data class SyncData(
    val states: List<com.merkost.honq.data.local.seed.dto.StateDto>,
    val licenseTypes: List<com.merkost.honq.data.local.seed.dto.LicenseTypeDto>,
    val assessmentTypes: List<com.merkost.honq.data.local.seed.dto.AssessmentTypeDto>,
    val questionSets: List<com.merkost.honq.data.local.seed.dto.QuestionSetDto>,
    val categories: List<com.merkost.honq.data.local.seed.dto.CategoryDto>,
    val qsCategories: List<com.merkost.honq.data.local.seed.dto.QuestionSetCategoryDto>
)
