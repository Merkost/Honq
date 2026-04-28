package com.merkost.honq.data.local.mapper

import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.Difficulty
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.State
import kotlinx.serialization.json.Json

fun QuestionEntity.toDomain(
    json: Json,
    categoryNames: Map<String, String> = emptyMap()
): Question = Question(
    id = id,
    code = code,
    text = text,
    imageUrl = imageUrl,
    options = json.decodeFromString(options),
    correctIndex = correctIndex,
    explanation = explanation,
    categoryId = categoryId,
    categoryName = categoryNames[categoryId] ?: "",
    questionSetId = questionSetId,
    stateId = stateId,
    difficulty = Difficulty.fromValue(difficulty)
)

fun StateEntity.toDomain(): State = State(
    id = id,
    name = name,
    shortName = shortName,
    externalPracticeUrl = externalPracticeUrl,
    handbookUrl = handbookUrl,
    isActive = isActive
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
    iconName = iconName,
    displayOrder = displayOrder,
    isActive = isActive
)

fun LicenseTypeEntity.toDomain(): LicenseType = LicenseType(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder
)

fun AssessmentTypeEntity.toDomain(): AssessmentType = AssessmentType(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder
)

fun QuestionSetEntity.toDomain(): QuestionSet = QuestionSet(
    id = id,
    stateId = stateId,
    licenseTypeId = licenseTypeId,
    assessmentTypeId = assessmentTypeId,
    mockTestQuestionCount = mockTestQuestionCount,
    mockTestTimeLimitMinutes = mockTestTimeLimitMinutes,
    mockTestPassPercentage = mockTestPassPercentage,
    isActive = isActive
)
