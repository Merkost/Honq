package com.merkost.honq.data.local.mapper

import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.domain.model.Difficulty
import com.merkost.honq.domain.model.Question
import kotlinx.serialization.json.Json

fun QuestionEntity.toDomain(
    json: Json,
    categoryNames: Map<String, String> = emptyMap()
): Question = Question(
    id = id,
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
