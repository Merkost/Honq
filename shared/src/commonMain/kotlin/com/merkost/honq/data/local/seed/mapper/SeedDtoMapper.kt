package com.merkost.honq.data.local.seed.mapper

import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.data.local.seed.dto.AssessmentTypeDto
import com.merkost.honq.data.local.seed.dto.CategoryDto
import com.merkost.honq.data.local.seed.dto.LicenseTypeDto
import com.merkost.honq.data.local.seed.dto.QuestionDto
import com.merkost.honq.data.local.seed.dto.QuestionSetCategoryDto
import com.merkost.honq.data.local.seed.dto.QuestionSetDto
import com.merkost.honq.data.local.seed.dto.StateDto
import com.merkost.honq.data.local.seed.dto.StateResourceDto
import com.merkost.honq.domain.model.Difficulty
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.StateResource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun QuestionDto.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    code = code,
    text = text,
    imageUrl = imageUrl,
    options = json.encodeToString(options),
    correctIndex = correctIndex,
    explanation = explanation.orEmpty(),
    categoryId = category.lowercase(),
    questionSetId = questionSetId,
    updatedAt = updatedAt,
    stateId = stateId.lowercase(),
    difficulty = difficulty ?: Difficulty.MEDIUM.value,
    isActive = isActive,
    version = version,
    source = source,
    createdAt = createdAt
)

fun StateDto.toEntity(): StateEntity = StateEntity(
    id = id,
    name = name,
    shortName = shortName,
    externalPracticeUrl = externalPracticeUrl,
    handbookUrl = handbookUrl,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description.orEmpty(),
    iconName = iconName.orEmpty(),
    displayOrder = displayOrder,
    isActive = isActive,
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt.orEmpty()
)

fun LicenseTypeDto.toEntity(): LicenseTypeEntity = LicenseTypeEntity(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AssessmentTypeDto.toEntity(): AssessmentTypeEntity = AssessmentTypeEntity(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuestionSetDto.toEntity(): QuestionSetEntity = QuestionSetEntity(
    id = id,
    stateId = stateId.lowercase(),
    licenseTypeId = licenseTypeId.lowercase(),
    assessmentTypeId = assessmentTypeId.lowercase(),
    mockTestQuestionCount = mockTestQuestionCount,
    mockTestTimeLimitMinutes = mockTestTimeLimitMinutes,
    mockTestPassPercentage = mockTestPassPercentage,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuestionSetCategoryDto.toEntity(): QuestionSetCategoryEntity = QuestionSetCategoryEntity(
    questionSetId = questionSetId,
    categoryId = categoryId,
    displayOrder = displayOrder,
    isActive = isActive
)

fun StateResourceDto.toDomain(): StateResource = StateResource(
    id = id,
    stateId = stateId.lowercase(),
    title = title,
    url = url,
    resourceType = when (resourceType.lowercase()) {
        "practice_test" -> ResourceType.PRACTICE_TEST
        "pdf" -> ResourceType.PDF
        "handbook" -> ResourceType.HANDBOOK
        else -> ResourceType.OTHER
    },
    licenseType = licenseType?.lowercase(),
    displayOrder = displayOrder
)
