package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val options: String,
    val correctIndex: Int,
    val explanation: String,
    val category: String,
    val imageUrl: String? = null,
    val updatedAt: String = ""
)
