package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val options: List<String>,
    @SerialName("correct_index") val correctIndex: Int,
    val explanation: String? = null,
    val category: QuestionCategoryDto,
    @SerialName("question_set_id") val questionSetId: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("state_id") val stateId: StateIdDto = StateIdDto.NSW,
    val difficulty: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val version: Int = 1,
    val source: String = "manual",
    @SerialName("created_at") val createdAt: String = ""
)
