package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mock_test_results",
    indices = [Index(value = ["questionSetId"])]
)
data class MockTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionSetId: String = "",
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeTakenSeconds: Long,
    val passed: Boolean,
    val passPercentage: Int = 80,
    val completedAt: String
)
