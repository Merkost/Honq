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
    val explanation: String,
    val category: String,
    @SerialName("updated_at") val updatedAt: String
)
