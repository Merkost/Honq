package com.merkost.honq.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_sets",
    indices = [
        Index(value = ["stateId"]),
        Index(value = ["licenseTypeId"]),
        Index(value = ["assessmentTypeId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = StateEntity::class,
            parentColumns = ["id"],
            childColumns = ["stateId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = LicenseTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["licenseTypeId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = AssessmentTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["assessmentTypeId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class QuestionSetEntity(
    @PrimaryKey val id: String,
    val stateId: String,
    val licenseTypeId: String,
    val assessmentTypeId: String,
    val mockTestQuestionCount: Int,
    val mockTestTimeLimitMinutes: Int,
    val mockTestPassPercentage: Int,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
