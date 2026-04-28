package com.merkost.honq.data.repository

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.Result
import com.merkost.honq.core.util.runLogged
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.CategoryProgress
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.repository.QuestionRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.kimplify.cedar.logging.Cedar

class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val dispatchers: AppDispatchers,
    private val json: Json,
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
}
