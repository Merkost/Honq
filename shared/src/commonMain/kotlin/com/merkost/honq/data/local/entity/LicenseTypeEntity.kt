package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "license_types")
data class LicenseTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
