package com.merkost.honq.data.local.mapper

import com.merkost.honq.data.local.entity.MockTestResultEntity
import com.merkost.honq.domain.model.MockTestResult
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

fun MockTestResultEntity.toDomain(): MockTestResult = MockTestResult(
    id = id,
    questionSetId = questionSetId,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    timeTaken = timeTakenSeconds.seconds,
    completedAt = Instant.parse(completedAt),
    passPercentage = passPercentage
)

fun MockTestResult.toEntity(): MockTestResultEntity = MockTestResultEntity(
    id = id,
    questionSetId = questionSetId,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    timeTakenSeconds = timeTaken.inWholeSeconds,
    passed = passed,
    passPercentage = passPercentage,
    completedAt = completedAt.toString()
)
