package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["questionSetId", "isActive"]),
        Index(value = ["categoryId"])
    ]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val code: String,
    val text: String,
    val options: String,
    val correctIndex: Int,
    val explanation: String,
    val categoryId: String,
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
