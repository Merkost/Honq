package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "states")
data class StateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val externalPracticeUrl: String? = null,
    val handbookUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
