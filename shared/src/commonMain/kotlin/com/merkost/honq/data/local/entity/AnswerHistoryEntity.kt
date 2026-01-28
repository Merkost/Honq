package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "answer_history",
    indices = [Index(value = ["questionId"])]
)
data class AnswerHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val wasCorrect: Boolean,
    val answeredAt: String
)
