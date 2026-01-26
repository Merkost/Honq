package com.merkost.honq.domain.repository

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.LicenseStage
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State

interface QuestionRepository {
    suspend fun getRandomQuestions(count: Int): Result<List<Question>>
    suspend fun getRandomQuestions(questionSetId: String, count: Int, categoryId: String? = null): Result<List<Question>>
    suspend fun getMockTestQuestions(): Result<List<Question>>
    suspend fun getMockTestQuestions(questionSetId: String): Result<List<Question>>
    suspend fun getQuestionById(questionId: String): Result<Question?>
    suspend fun searchQuestions(questionSetId: String, query: String): Result<List<Question>>
    suspend fun syncQuestions(): Result<Unit>
    suspend fun syncQuestions(questionSetId: String): Result<Unit>
    suspend fun getStates(): Result<List<State>>
    suspend fun syncStates(): Result<Unit>
    suspend fun getLicenseTypes(): Result<List<LicenseType>>
    suspend fun getLicenseStages(): Result<List<LicenseStage>>
    suspend fun getAssessmentTypes(): Result<List<AssessmentType>>
    suspend fun getQuestionSetsByState(stateId: String): Result<List<QuestionSet>>
    suspend fun getQuestionSetById(questionSetId: String): Result<QuestionSet?>
    suspend fun getCategoriesByQuestionSet(questionSetId: String): Result<List<Category>>
    fun getLastSyncTime(questionSetId: String): kotlin.time.Instant?
}
