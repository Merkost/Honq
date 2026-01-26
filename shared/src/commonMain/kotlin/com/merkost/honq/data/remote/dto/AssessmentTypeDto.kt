package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssessmentTypeDto(
    val id: String,
    val name: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
