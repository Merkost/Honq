package com.merkost.honq.data.remote.api

import com.merkost.honq.data.remote.dto.AssessmentTypeDto
import com.merkost.honq.data.remote.dto.CategoryDto
import com.merkost.honq.data.remote.dto.LicenseStageDto
import com.merkost.honq.data.remote.dto.LicenseTypeDto
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.data.remote.dto.QuestionSetCategoryDto
import com.merkost.honq.data.remote.dto.QuestionSetDto
import com.merkost.honq.data.remote.dto.StateDto
import com.merkost.honq.data.remote.dto.StateResourceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class QuestionApi(
    private val client: SupabaseClient
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> =
        client.postgrest["questions"]
            .select()
            .decodeList()

    suspend fun fetchQuestionsByQuestionSet(questionSetId: String): List<QuestionDto> =
        client.postgrest["questions"]
            .select {
                filter {
                    eq("question_set_id", questionSetId)
                    eq("is_active", true)
                }
            }
            .decodeList()

    suspend fun fetchUpdatedQuestions(questionSetId: String, since: String): List<QuestionDto> =
        client.postgrest["questions"]
            .select {
                filter {
                    eq("question_set_id", questionSetId)
                    gt("updated_at", since)
                }
            }
            .decodeList()

    suspend fun fetchStates(includeInactive: Boolean = false): List<StateDto> =
        client.postgrest["states"]
            .select {
                if (!includeInactive) {
                    filter {
                        eq("is_active", true)
                    }
                }
            }
            .decodeList()

    suspend fun fetchLicenseTypes(includeInactive: Boolean = false): List<LicenseTypeDto> =
        client.postgrest["license_types"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList()

    suspend fun fetchLicenseStages(includeInactive: Boolean = false): List<LicenseStageDto> =
        client.postgrest["license_stages"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList()

    suspend fun fetchAssessmentTypes(includeInactive: Boolean = false): List<AssessmentTypeDto> =
        client.postgrest["assessment_types"]
            .select {
                if (!includeInactive) {
                    filter { eq("is_active", true) }
                }
            }
            .decodeList()

    suspend fun fetchQuestionSets(
        stateId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetDto> =
        client.postgrest["question_sets"]
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
            .decodeList()

    suspend fun fetchQuestionSetCategories(
        questionSetId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetCategoryDto> =
        client.postgrest["question_set_categories"]
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
            .decodeList()

    suspend fun fetchCategories(includeInactive: Boolean = false): List<CategoryDto> =
        client.postgrest["categories"]
            .select {
                this.filter {
                    //TODO: Add proper filtering for includeInactive
                }
            }
            .decodeList<CategoryDto>()
            .let { categories -> if (includeInactive) categories else categories.filter { it.isActive } }

    suspend fun fetchStateResources(stateId: String): List<StateResourceDto> =
        client.postgrest["state_resources"]
            .select {
                filter {
                    eq("state_id", stateId)
                    eq("is_active", true)
                }
            }
            .decodeList()
}
