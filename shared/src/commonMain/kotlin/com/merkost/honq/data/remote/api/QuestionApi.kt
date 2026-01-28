package com.merkost.honq.data.remote.api

import com.merkost.honq.data.remote.dto.AssessmentTypeDto
import com.merkost.honq.data.remote.dto.CategoryDto
import com.merkost.honq.data.remote.dto.LicenseTypeDto
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.data.remote.dto.QuestionSetCategoryDto
import com.merkost.honq.data.remote.dto.QuestionSetDto
import com.merkost.honq.data.remote.dto.StateDto
import com.merkost.honq.data.remote.dto.StateResourceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import org.kimplify.cedar.logging.Cedar

class QuestionApi(
    private val client: SupabaseClient
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> {
        Cedar.tag("QuestionApi").d("Fetching all questions...")
        val result = client.postgrest["questions"]
            .select()
            .decodeList<QuestionDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} questions")
        return result
    }

    suspend fun fetchQuestionsByQuestionSet(questionSetId: String): List<QuestionDto> {
        Cedar.tag("QuestionApi").d("Fetching questions for questionSet=$questionSetId...")
        val result = client.postgrest["questions"]
            .select {
                filter {
                    eq("question_set_id", questionSetId)
                    eq("is_active", true)
                }
            }
            .decodeList<QuestionDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchUpdatedQuestions(questionSetId: String, since: String): List<QuestionDto> {
        Cedar.tag("QuestionApi").d("Fetching updated questions for questionSet=$questionSetId since=$since...")
        val result = client.postgrest["questions"]
            .select {
                filter {
                    eq("question_set_id", questionSetId)
                    gt("updated_at", since)
                }
            }
            .decodeList<QuestionDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} updated questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchStates(includeInactive: Boolean = false): List<StateDto> {
        Cedar.tag("QuestionApi").d("Fetching states (includeInactive=$includeInactive)...")
        val result = client.postgrest["states"]
            .select {
                if (!includeInactive) {
                    filter {
                        eq("is_active", true)
                    }
                }
            }
            .decodeList<StateDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} states")
        return result
    }

    suspend fun fetchLicenseTypes(includeInactive: Boolean = false): List<LicenseTypeDto> {
        Cedar.tag("QuestionApi").d("Fetching license types (includeInactive=$includeInactive)...")
        val result = client.postgrest["license_types"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList<LicenseTypeDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} license types")
        return result
    }

    suspend fun fetchAssessmentTypes(includeInactive: Boolean = false): List<AssessmentTypeDto> {
        Cedar.tag("QuestionApi").d("Fetching assessment types (includeInactive=$includeInactive)...")
        val result = client.postgrest["assessment_types"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList<AssessmentTypeDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} assessment types")
        return result
    }

    suspend fun fetchQuestionSets(
        stateId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetDto> {
        Cedar.tag("QuestionApi").d("Fetching question sets (stateId=$stateId, includeInactive=$includeInactive)...")
        val result = client.postgrest["question_sets"]
            .select {
                filter {
                    if (stateId != null) {
                        eq("state_id", stateId)
                    }
                    if (!includeInactive) {
                        eq("is_active", true)
                    }
                }
            }
            .decodeList<QuestionSetDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} question sets")
        return result
    }

    suspend fun fetchQuestionSetCategories(
        questionSetId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetCategoryDto> {
        Cedar.tag("QuestionApi").d("Fetching question set categories (questionSetId=$questionSetId)...")
        val result = client.postgrest["question_set_categories"]
            .select {
                filter {
                    if (questionSetId != null) {
                        eq("question_set_id", questionSetId)
                    }
                    if (!includeInactive) {
                        eq("is_active", true)
                    }
                }
            }
            .decodeList<QuestionSetCategoryDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} question set categories")
        return result
    }

    suspend fun fetchCategories(includeInactive: Boolean = false): List<CategoryDto> {
        Cedar.tag("QuestionApi").d("Fetching categories (includeInactive=$includeInactive)...")
        val result = client.postgrest["categories"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList<CategoryDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} categories")
        return result
    }

    suspend fun fetchStateResources(stateId: String): List<StateResourceDto> {
        Cedar.tag("QuestionApi").d("Fetching state resources for stateId=$stateId...")
        val result = client.postgrest["state_resources"]
            .select {
                filter {
                    eq("state_id", stateId)
                    eq("is_active", true)
                }
            }
            .decodeList<StateResourceDto>()
        Cedar.tag("QuestionApi").d("Fetched ${result.size} state resources for stateId=$stateId")
        return result
    }
}
