package com.merkost.honq.data.local.datasource

import com.merkost.honq.data.local.db.AnswerHistoryDao
import com.merkost.honq.data.local.db.AssessmentTypeDao
import com.merkost.honq.data.local.db.CategoryDao
import com.merkost.honq.data.local.db.FavoriteQuestionDao
import com.merkost.honq.data.local.db.LicenseStageDao
import com.merkost.honq.data.local.db.LicenseTypeDao
import com.merkost.honq.data.local.db.MockTestResultDao
import com.merkost.honq.data.local.db.QuestionDao
import com.merkost.honq.data.local.db.QuestionSetCategoryDao
import com.merkost.honq.data.local.db.QuestionSetDao
import com.merkost.honq.data.local.db.StateDao
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.FavoriteQuestionEntity
import com.merkost.honq.data.local.entity.LicenseStageEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.data.local.mapper.toDomain
import com.merkost.honq.data.local.mapper.toEntity
import com.merkost.honq.data.remote.mapper.toDomain
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.LicenseStage
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.serialization.json.Json

class QuestionLocalDataSource(
    private val questionDao: QuestionDao,
    private val answerHistoryDao: AnswerHistoryDao,
    private val favoriteQuestionDao: FavoriteQuestionDao,
    private val mockTestResultDao: MockTestResultDao,
    private val stateDao: StateDao,
    private val categoryDao: CategoryDao,
    private val questionSetDao: QuestionSetDao,
    private val licenseTypeDao: LicenseTypeDao,
    private val licenseStageDao: LicenseStageDao,
    private val assessmentTypeDao: AssessmentTypeDao,
    private val questionSetCategoryDao: QuestionSetCategoryDao,
    private val json: Json
) {
    private suspend fun getCategoryNameMap(questionSetId: String?): Map<String, String> {
        val categories = if (questionSetId != null) {
            categoryDao.getCategoriesForQuestionSet(questionSetId)
                .ifEmpty { categoryDao.getActiveCategories() }
        } else {
            categoryDao.getActiveCategories()
        }
        return categories.associate { it.id to it.name }
    }

    suspend fun getRandomQuestions(count: Int): List<Question> =
        questionDao.getRandomQuestions(count).map { it.toDomain(json, getCategoryNameMap(null)) }

    suspend fun getRandomQuestionsByQuestionSet(questionSetId: String, count: Int): List<Question> =
        questionDao.getRandomQuestionsByQuestionSet(questionSetId, count)
            .map { it.toDomain(json, getCategoryNameMap(questionSetId)) }

    suspend fun getRandomQuestionsByQuestionSetAndCategory(
        questionSetId: String,
        categoryId: String,
        count: Int
    ): List<Question> =
        questionDao.getRandomQuestionsByQuestionSetAndCategory(questionSetId, categoryId, count)
            .map { it.toDomain(json, getCategoryNameMap(questionSetId)) }

    suspend fun getMockTestQuestions(): List<Question> =
        questionDao.getMockTestQuestions().map { it.toDomain(json, getCategoryNameMap(null)) }

    suspend fun getMockTestQuestionsByQuestionSet(questionSetId: String, count: Int): List<Question> =
        questionDao.getMockTestQuestionsByQuestionSet(questionSetId, count)
            .map { it.toDomain(json, getCategoryNameMap(questionSetId)) }

    suspend fun getQuestionById(questionId: String): Question? =
        questionDao.getQuestionById(questionId)?.toDomain(json, getCategoryNameMap(null))

    suspend fun insertQuestions(questions: List<QuestionEntity>) =
        questionDao.insertAll(questions)

    suspend fun upsertQuestions(questions: List<QuestionEntity>) =
        questionDao.upsertQuestions(questions)

    suspend fun getLastUpdatedAt(questionSetId: String): String? =
        questionDao.getLastUpdatedAt(questionSetId)

    suspend fun getQuestionCountByQuestionSet(questionSetId: String): Int =
        questionDao.getQuestionCountByQuestionSet(questionSetId)

    suspend fun deleteQuestionsByQuestionSet(questionSetId: String) =
        questionDao.deleteByQuestionSet(questionSetId)

    suspend fun recordAnswer(questionId: String, wasCorrect: Boolean) {
        answerHistoryDao.insert(
            AnswerHistoryEntity(
                questionId = questionId,
                wasCorrect = wasCorrect,
                answeredAt = Clock.System.now().toString()
            )
        )
    }

    suspend fun toggleFavorite(questionId: String) {
        val isFavorite = favoriteQuestionDao.isFavorite(questionId)
        if (isFavorite) {
            favoriteQuestionDao.delete(questionId)
        } else {
            favoriteQuestionDao.insert(
                FavoriteQuestionEntity(
                    questionId = questionId,
                    addedAt = Clock.System.now().toString()
                )
            )
        }
    }

    fun observeTotalAnswered(): Flow<Int> = answerHistoryDao.observeTotalCount()

    fun observeCorrectAnswers(): Flow<Int> = answerHistoryDao.observeCorrectCount()

    fun observeUniqueQuestionsAnswered(): Flow<Int> = answerHistoryDao.observeUniqueQuestionsAnswered()

    fun observeTotalQuestionCount(): Flow<Int> = questionDao.observeTotalQuestionCount()

    fun observeTotalAnsweredByQuestionSet(questionSetId: String): Flow<Int> =
        answerHistoryDao.observeTotalCountByQuestionSet(questionSetId)

    fun observeCorrectAnswersByQuestionSet(questionSetId: String): Flow<Int> =
        answerHistoryDao.observeCorrectCountByQuestionSet(questionSetId)

    fun observeUniqueQuestionsAnsweredByQuestionSet(questionSetId: String): Flow<Int> =
        answerHistoryDao.observeUniqueQuestionsAnsweredByQuestionSet(questionSetId)

    fun observeQuestionCountByQuestionSet(questionSetId: String): Flow<Int> =
        questionDao.observeQuestionCountByQuestionSet(questionSetId)

    fun observeFavoriteQuestionIds(): Flow<Set<String>> =
        favoriteQuestionDao.observeFavoriteIds().map { it.toSet() }

    fun observeFavoriteQuestions(): Flow<List<Question>> =
        combine(
            favoriteQuestionDao.observeFavoriteQuestions(),
            categoryDao.observeActiveCategories()
        ) { entities, categories ->
            val categoryNames = categories.associate { it.id to it.name }
            entities.map { it.toDomain(json, categoryNames) }
        }

    suspend fun saveMockTestResult(result: MockTestResult) {
        mockTestResultDao.insert(result.toEntity())
    }

    fun observeMockTestResults(): Flow<List<MockTestResult>> =
        mockTestResultDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeMockTestCount(): Flow<Int> = mockTestResultDao.observeTotalCount()

    fun observeMockTestPassedCount(): Flow<Int> = mockTestResultDao.observePassedCount()

    suspend fun getStates(): List<State> =
        stateDao.getStates().map { it.toDomain() }

    suspend fun insertStates(states: List<StateEntity>) =
        stateDao.insertAll(states)

    suspend fun getStateById(stateId: String): State? =
        stateDao.getStateById(stateId)?.toDomain()

    suspend fun getLicenseTypes(): List<LicenseType> =
        licenseTypeDao.getActiveLicenseTypes().map { it.toDomain() }

    suspend fun getLicenseStages(): List<LicenseStage> =
        licenseStageDao.getActiveLicenseStages().map { it.toDomain() }

    suspend fun getAssessmentTypes(): List<AssessmentType> =
        assessmentTypeDao.getActiveAssessmentTypes().map { it.toDomain() }

    suspend fun getQuestionSetsByState(stateId: String): List<QuestionSet> =
        questionSetDao.getQuestionSetsByState(stateId).map { it.toDomain() }

    suspend fun getQuestionSetById(questionSetId: String): QuestionSet? =
        questionSetDao.getQuestionSetById(questionSetId)?.toDomain()

    suspend fun getCategoriesForQuestionSet(questionSetId: String): List<Category> =
        categoryDao.getCategoriesForQuestionSet(questionSetId).map { it.toDomain() }

    suspend fun insertLicenseTypes(licenseTypes: List<LicenseTypeEntity>) =
        licenseTypeDao.insertAll(licenseTypes)

    suspend fun insertLicenseStages(stages: List<LicenseStageEntity>) =
        licenseStageDao.insertAll(stages)

    suspend fun insertAssessmentTypes(types: List<AssessmentTypeEntity>) =
        assessmentTypeDao.insertAll(types)

    suspend fun insertQuestionSets(questionSets: List<QuestionSetEntity>) =
        questionSetDao.insertAll(questionSets)

    suspend fun insertCategories(categories: List<CategoryEntity>) =
        categoryDao.insertAll(categories)

    suspend fun insertQuestionSetCategories(categories: List<QuestionSetCategoryEntity>) =
        questionSetCategoryDao.insertAll(categories)

    suspend fun upsertLicenseTypes(licenseTypes: List<LicenseTypeEntity>) =
        licenseTypeDao.upsertAll(licenseTypes)

    suspend fun upsertLicenseStages(stages: List<LicenseStageEntity>) =
        licenseStageDao.upsertAll(stages)

    suspend fun upsertAssessmentTypes(types: List<AssessmentTypeEntity>) =
        assessmentTypeDao.upsertAll(types)

    suspend fun upsertQuestionSets(questionSets: List<QuestionSetEntity>) =
        questionSetDao.upsertAll(questionSets)

    suspend fun upsertCategories(categories: List<CategoryEntity>) =
        categoryDao.upsertCategories(categories)

    suspend fun upsertQuestionSetCategories(categories: List<QuestionSetCategoryEntity>) =
        questionSetCategoryDao.upsertAll(categories)

    suspend fun clearAllProgress() {
        answerHistoryDao.deleteAll()
        mockTestResultDao.deleteAll()
    }
}
