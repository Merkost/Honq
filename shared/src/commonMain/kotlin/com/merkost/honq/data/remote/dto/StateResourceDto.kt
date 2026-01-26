package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateResourceDto(
    val id: String,
    @SerialName("state_id") val stateId: StateIdDto,
    val title: String,
    val url: String,
    @SerialName("resource_type") val resourceType: ResourceTypeDto,
    @SerialName("license_type") val licenseType: LicenseTypeIdDto? = null,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)
