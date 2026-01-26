package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_sets")
data class QuestionSetEntity(
    @PrimaryKey val id: String,
    val stateId: String,
    val licenseTypeId: String,
    val licenseStageId: String,
    val assessmentTypeId: String,
    val mockTestQuestionCount: Int,
    val mockTestTimeLimitMinutes: Int,
    val mockTestPassPercentage: Int,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
