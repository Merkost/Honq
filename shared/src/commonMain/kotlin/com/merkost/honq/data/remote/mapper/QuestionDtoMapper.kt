package com.merkost.honq.data.remote.mapper

import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun QuestionDto.toDomain(): Question = Question(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = options,
    correctIndex = correctIndex,
    explanation = explanation,
    category = QuestionCategory.valueOf(category.uppercase())
)

fun QuestionDto.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = json.encodeToString(options),
    correctIndex = correctIndex,
    explanation = explanation,
    category = category,
    updatedAt = updatedAt
)
