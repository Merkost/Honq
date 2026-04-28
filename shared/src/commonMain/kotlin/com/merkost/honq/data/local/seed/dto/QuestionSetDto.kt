package com.merkost.honq.data.local.seed.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetDto(
    val id: String,
    @SerialName("state_id") val stateId: String,
    @SerialName("license_type_id") val licenseTypeId: String,
    @SerialName("assessment_type_id") val assessmentTypeId: String,
    @SerialName("mock_test_question_count") val mockTestQuestionCount: Int = 45,
    @SerialName("mock_test_time_limit_minutes") val mockTestTimeLimitMinutes: Int = 45,
    @SerialName("mock_test_pass_percentage") val mockTestPassPercentage: Int = 75,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
