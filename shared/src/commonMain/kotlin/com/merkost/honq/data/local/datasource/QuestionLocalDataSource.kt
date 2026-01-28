package com.merkost.honq.data.local.datasource

import com.merkost.honq.data.local.db.AnswerHistoryDao
import com.merkost.honq.data.local.db.AssessmentTypeDao
import com.merkost.honq.data.local.db.CategoryDao
import com.merkost.honq.data.local.db.FavoriteQuestionDao
import com.merkost.honq.data.local.db.LicenseTypeDao
import com.merkost.honq.data.local.db.MockTestAnswerDao
import com.merkost.honq.data.local.db.MockTestResultDao
import com.merkost.honq.data.local.db.QuestionDao
import com.merkost.honq.data.local.db.QuestionSetCategoryDao
import com.merkost.honq.data.local.db.QuestionSetDao
import com.merkost.honq.data.local.db.StateDao
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.WeakQuestionResult
import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.FavoriteQuestionEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.MockTestAnswerEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.data.local.mapper.toDomain
import com.merkost.honq.data.local.mapper.toEntity
import com.merkost.honq.data.remote.mapper.toDomain
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.CategoryProgress
import com.merkost.honq.domain.model.MockTestReviewAnswer
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import org.kimplify.cedar.logging.Cedar

class QuestionLocalDataSource(
    private val questionDao: QuestionDao,
    private val answerHistoryDao: AnswerHistoryDao,
    private val favoriteQuestionDao: FavoriteQuestionDao,
    private val mockTestResultDao: MockTestResultDao,
    private val mockTestAnswerDao: MockTestAnswerDao,
    private val stateDao: StateDao,
    private val categoryDao: CategoryDao,
    private val questionSetDao: QuestionSetDao,
    private val licenseTypeDao: LicenseTypeDao,
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

    suspend fun searchQuestions(questionSetId: String, query: String): List<Question> {
        val searchPattern = "%${query.lowercase()}%"
        return questionDao.searchQuestions(questionSetId, searchPattern)
            .map { it.toDomain(json, getCategoryNameMap(questionSetId)) }
    }

    suspend fun insertQuestions(questions: List<QuestionEntity>) =
        questionDao.insertAll(questions)

    suspend fun upsertQuestions(questions: List<QuestionEntity>) {
        Cedar.tag("LocalData").d("upsertQuestions: ${questions.size} questions")
        questionDao.upsertQuestions(questions)
    }

    suspend fun getLastUpdatedAt(questionSetId: String): String? =
        questionDao.getLastUpdatedAt(questionSetId)

    suspend fun getQuestionCountByQuestionSet(questionSetId: String): Int =
        questionDao.getQuestionCountByQuestionSet(questionSetId)

    suspend fun deleteQuestionsByQuestionSet(questionSetId: String) {
        Cedar.tag("LocalData").d("deleteQuestionsByQuestionSet: $questionSetId")
        questionDao.deleteByQuestionSet(questionSetId)
    }

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
            Cedar.tag("LocalData").d("toggleFavorite: removing $questionId from favorites")
            favoriteQuestionDao.delete(questionId)
        } else {
            Cedar.tag("LocalData").d("toggleFavorite: adding $questionId to favorites")
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

    suspend fun getLastMockTestResultId(): Long? =
        mockTestResultDao.getLastInsertedId()

    suspend fun saveMockTestAnswers(resultId: Long, answers: List<MockTestAnswerEntity>) {
        mockTestAnswerDao.insertAll(answers)
    }

    fun observeMockTestAnswers(resultId: Long): Flow<List<MockTestAnswerEntity>> =
        mockTestAnswerDao.observeByMockTestResultId(resultId)

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

    suspend fun getAssessmentTypes(): List<AssessmentType> =
        assessmentTypeDao.getActiveAssessmentTypes().map { it.toDomain() }

    suspend fun getQuestionSetsByState(stateId: String): List<QuestionSet> =
        questionSetDao.getQuestionSetsByState(stateId).map { it.toDomain() }

    suspend fun getQuestionSetById(questionSetId: String): QuestionSet? =
        questionSetDao.getQuestionSetById(questionSetId)?.toDomain()

    suspend fun getCategoriesForQuestionSet(questionSetId: String): List<Category> {
        val fromJunction = categoryDao.getCategoriesForQuestionSet(questionSetId)
        if (fromJunction.isNotEmpty()) return fromJunction.map { it.toDomain() }
        return categoryDao.getCategoriesFromQuestions(questionSetId).map { it.toDomain() }
    }

    suspend fun getAllActiveCategories(): List<Category> =
        categoryDao.getActiveCategories().map { it.toDomain() }

    suspend fun insertLicenseTypes(licenseTypes: List<LicenseTypeEntity>) =
        licenseTypeDao.insertAll(licenseTypes)

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

    suspend fun upsertAssessmentTypes(types: List<AssessmentTypeEntity>) =
        assessmentTypeDao.upsertAll(types)

    suspend fun upsertQuestionSets(questionSets: List<QuestionSetEntity>) =
        questionSetDao.upsertAll(questionSets)

    suspend fun upsertCategories(categories: List<CategoryEntity>) =
        categoryDao.upsertCategories(categories)

    suspend fun upsertQuestionSetCategories(categories: List<QuestionSetCategoryEntity>) =
        questionSetCategoryDao.upsertAll(categories)

    suspend fun clearAllProgress() {
        Cedar.tag("LocalData").d("clearAllProgress: deleting all answer history and mock test results")
        answerHistoryDao.deleteAll()
        mockTestResultDao.deleteAll()
        Cedar.tag("LocalData").d("clearAllProgress: completed")
    }

    suspend fun getWeakestQuestionResults(questionSetId: String, limit: Int): List<WeakQuestionResult> =
        answerHistoryDao.getWeakestQuestionIds(questionSetId, limit)

    suspend fun getWeakestQuestions(questionSetId: String, limit: Int): List<Pair<Question, WeakQuestionResult>> {
        val weakResults = answerHistoryDao.getWeakestQuestionIds(questionSetId, limit)
        val categoryNameMap = getCategoryNameMap(questionSetId)
        return weakResults.mapNotNull { result ->
            questionDao.getQuestionById(result.questionId)?.let { entity ->
                entity.toDomain(json, categoryNameMap) to result
            }
        }
    }

    suspend fun getUnansweredQuestions(questionSetId: String, limit: Int): List<Question> {
        val categoryNameMap = getCategoryNameMap(questionSetId)
        return questionDao.getUnansweredQuestions(questionSetId, limit)
            .map { it.toDomain(json, categoryNameMap) }
    }

    fun observeWeakestQuestionCount(questionSetId: String): Flow<Int> =
        answerHistoryDao.observeWeakestQuestionCount(questionSetId)

    fun observeUnansweredQuestionCount(questionSetId: String): Flow<Int> =
        answerHistoryDao.observeUnansweredQuestionCount(questionSetId)

    suspend fun getCategoryProgress(questionSetId: String): Map<String, CategoryProgress> {
        val totals = questionDao.getQuestionCountsByCategory(questionSetId).associate { it.categoryId to it.count }
        val answered = answerHistoryDao.getAnsweredCountsByCategory(questionSetId).associate { it.categoryId to it.count }
        return totals.mapValues { (categoryId, total) ->
            CategoryProgress(totalQuestions = total, answeredQuestions = answered[categoryId] ?: 0)
        }
    }

    suspend fun getMockTestIncorrectAnswers(mockTestResultId: Long): List<MockTestReviewAnswer> {
        val incorrectEntities = mockTestAnswerDao.getIncorrectByMockTestResultId(mockTestResultId)
        val categoryNameMap = getCategoryNameMap(null)
        return incorrectEntities.mapNotNull { entity ->
            questionDao.getQuestionById(entity.questionId)?.let { questionEntity ->
                MockTestReviewAnswer(
                    question = questionEntity.toDomain(json, categoryNameMap),
                    selectedAnswerIndex = entity.selectedAnswerIndex,
                    wasCorrect = entity.wasCorrect
                )
            }
        }
    }
}
