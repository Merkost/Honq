package com.merkost.honq.data.remote.mapper

import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.BuildKonfig
import com.merkost.honq.data.remote.api.HostedImageUrlBuilder
import com.merkost.honq.data.remote.dto.AssessmentTypeDto
import com.merkost.honq.data.remote.dto.CategoryDto
import com.merkost.honq.data.remote.dto.LicenseTypeDto
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.data.remote.dto.QuestionSetCategoryDto
import com.merkost.honq.data.remote.dto.QuestionSetDto
import com.merkost.honq.data.remote.dto.StateDto
import com.merkost.honq.data.remote.dto.StateResourceDto
import com.merkost.honq.domain.model.AssessmentType
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.model.Difficulty
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.State
import com.merkost.honq.domain.model.StateResource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val imageUrlBuilder by lazy { HostedImageUrlBuilder(BuildKonfig.FIREBASE_HOSTING_BASE_URL) }

fun QuestionDto.toDomain(): Question = Question(
    id = id,
    code = code,
    text = text,
    imageUrl = imageUrlBuilder.buildUrl(imageUrl),
    options = options,
    correctIndex = correctIndex,
    explanation = explanation.orEmpty(),
    categoryId = category.lowercase(),
    categoryName = category.lowercase(),
    questionSetId = questionSetId,
    stateId = stateId.lowercase(),
    difficulty = Difficulty.fromValue(difficulty ?: Difficulty.MEDIUM.value)
)

fun QuestionDto.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    code = code,
    text = text,
    imageUrl = imageUrlBuilder.buildUrl(imageUrl),
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

fun StateDto.toDomain(): State = State(
    id = id,
    name = name,
    shortName = shortName,
    externalPracticeUrl = externalPracticeUrl,
    handbookUrl = handbookUrl,
    isActive = isActive
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

fun StateEntity.toDomain(): State = State(
    id = id,
    name = name,
    shortName = shortName,
    externalPracticeUrl = externalPracticeUrl,
    handbookUrl = handbookUrl,
    isActive = isActive
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description.orEmpty(),
    iconName = iconName.orEmpty(),
    displayOrder = displayOrder,
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
