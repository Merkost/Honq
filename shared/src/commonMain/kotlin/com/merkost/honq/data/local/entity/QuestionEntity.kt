package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val options: String,
    val correctIndex: Int,
    val explanation: String,
    @ColumnInfo(name = "category") val categoryId: String,
    val questionSetId: String = "",
    val imageUrl: String? = null,
    val updatedAt: String = "",
    val stateId: String = "nsw",
    val difficulty: Int = 2,
    val isActive: Boolean = true,
    val version: Int = 1,
    val source: String = "manual",
    val createdAt: String = ""
)
