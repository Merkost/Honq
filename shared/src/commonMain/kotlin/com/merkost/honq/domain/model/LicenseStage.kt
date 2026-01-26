package com.merkost.honq.domain.model

data class LicenseStage(
    val id: String,
    val name: String,
    val shortName: String,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
