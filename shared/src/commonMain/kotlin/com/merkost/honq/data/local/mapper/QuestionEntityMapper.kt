package com.merkost.honq.data.local.mapper

import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionCategory
import kotlinx.serialization.json.Json

fun QuestionEntity.toDomain(json: Json): Question = Question(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = json.decodeFromString(options),
    correctIndex = correctIndex,
    explanation = explanation,
    category = QuestionCategory.valueOf(category.uppercase())
)
