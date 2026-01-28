package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mock_test_answers",
    indices = [Index(value = ["mockTestResultId"])],
    foreignKeys = [ForeignKey(
        entity = MockTestResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["mockTestResultId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MockTestAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mockTestResultId: Long,
    val questionId: String,
    val selectedAnswerIndex: Int,
    val wasCorrect: Boolean
)
