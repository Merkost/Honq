package com.merkost.honq.data.remote.api

import com.merkost.honq.data.remote.dto.AssessmentTypeDto
import com.merkost.honq.data.remote.dto.CategoryDto
import com.merkost.honq.data.remote.dto.LicenseTypeDto
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.data.remote.dto.QuestionSetCategoryDto
import com.merkost.honq.data.remote.dto.QuestionSetDto
import com.merkost.honq.data.remote.dto.StateDto
import com.merkost.honq.data.remote.dto.StateResourceDto
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import org.kimplify.cedar.logging.Cedar

class FirestoreContentApi(
    private val firestore: FirebaseFirestore
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching all questions...")
        val result = firestore.collection("questions")
            .get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} questions")
        return result
    }

    suspend fun fetchQuestionsByQuestionSet(questionSetId: String): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching questions for questionSet=$questionSetId...")
        val result = firestore.collection("questions")
            .where {
                ("question_set_id" equalTo questionSetId) and ("is_active" equalTo true)
            }
            .get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchUpdatedQuestions(questionSetId: String, since: String): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching updated questions for questionSet=$questionSetId since=$since...")
        val result = firestore.collection("questions")
            .where {
                ("question_set_id" equalTo questionSetId) and ("updated_at" greaterThan since)
            }
            .get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} updated questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchStates(includeInactive: Boolean = false): List<StateDto> {
        Cedar.tag("FsContentApi").d("Fetching states (includeInactive=$includeInactive)...")
        var query: Query = firestore.collection("states")
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<StateDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} states")
        return result
    }

    suspend fun fetchLicenseTypes(includeInactive: Boolean = false): List<LicenseTypeDto> {
        Cedar.tag("FsContentApi").d("Fetching license types (includeInactive=$includeInactive)...")
        var query: Query = firestore.collection("license_types")
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<LicenseTypeDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} license types")
        return result
    }

    suspend fun fetchAssessmentTypes(includeInactive: Boolean = false): List<AssessmentTypeDto> {
        Cedar.tag("FsContentApi").d("Fetching assessment types (includeInactive=$includeInactive)...")
        var query: Query = firestore.collection("assessment_types")
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<AssessmentTypeDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} assessment types")
        return result
    }

    suspend fun fetchQuestionSets(
        stateId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetDto> {
        Cedar.tag("FsContentApi").d("Fetching question sets (stateId=$stateId, includeInactive=$includeInactive)...")
        var query: Query = firestore.collection("question_sets")
        if (stateId != null) query = query.where { "state_id" equalTo stateId }
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<QuestionSetDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} question sets")
        return result
    }

    suspend fun fetchQuestionSetCategories(
        questionSetId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetCategoryDto> {
        Cedar.tag("FsContentApi").d("Fetching question set categories (questionSetId=$questionSetId)...")
        var query: Query = firestore.collection("question_set_categories")
        if (questionSetId != null) query = query.where { "question_set_id" equalTo questionSetId }
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<QuestionSetCategoryDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} question set categories")
        return result
    }

    suspend fun fetchCategories(includeInactive: Boolean = false): List<CategoryDto> {
        Cedar.tag("FsContentApi").d("Fetching categories (includeInactive=$includeInactive)...")
        var query: Query = firestore.collection("categories")
        if (!includeInactive) query = query.where { "is_active" equalTo true }
        val result = query.get().documents.map { it.data<CategoryDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} categories")
        return result
    }

    suspend fun fetchStateResources(stateId: String): List<StateResourceDto> {
        Cedar.tag("FsContentApi").d("Fetching state resources for stateId=$stateId...")
        val result = firestore.collection("state_resources")
            .where {
                ("state_id" equalTo stateId) and ("is_active" equalTo true)
            }
            .get().documents
            .map { it.data<StateResourceDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} state resources for stateId=$stateId")
        return result
    }
}
