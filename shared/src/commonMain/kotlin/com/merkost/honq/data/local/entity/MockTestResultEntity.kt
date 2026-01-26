package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_test_results")
data class MockTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeTakenSeconds: Long,
    val passed: Boolean,
    val completedAt: String
)
