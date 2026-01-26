package com.merkost.honq.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "question_set_categories",
    primaryKeys = ["questionSetId", "categoryId"]
)
data class QuestionSetCategoryEntity(
    val questionSetId: String,
    val categoryId: String,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
