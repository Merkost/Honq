package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateDto(
    val id: String,
    val name: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("mock_test_question_count") val mockTestQuestionCount: Int = 45,
    @SerialName("mock_test_time_limit_minutes") val mockTestTimeLimitMinutes: Int = 45,
    @SerialName("mock_test_pass_percentage") val mockTestPassPercentage: Int = 80,
    @SerialName("external_practice_url") val externalPracticeUrl: String? = null,
    @SerialName("handbook_url") val handbookUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
