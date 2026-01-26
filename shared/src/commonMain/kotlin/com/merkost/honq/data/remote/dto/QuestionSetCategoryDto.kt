package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetCategoryDto(
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)
