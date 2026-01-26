package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val iconName: String = "",
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
