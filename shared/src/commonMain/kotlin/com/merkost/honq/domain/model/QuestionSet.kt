package com.merkost.honq.domain.model

data class QuestionSet(
    val id: String,
    val stateId: String,
    val licenseTypeId: String,
    val assessmentTypeId: String,
    val mockTestQuestionCount: Int,
    val mockTestTimeLimitMinutes: Int,
    val mockTestPassPercentage: Int,
    val isActive: Boolean = true
)
