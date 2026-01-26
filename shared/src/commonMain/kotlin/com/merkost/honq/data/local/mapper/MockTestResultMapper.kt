package com.merkost.honq.data.local.mapper

import com.merkost.honq.data.local.entity.MockTestResultEntity
import com.merkost.honq.domain.model.MockTestResult
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

fun MockTestResultEntity.toDomain(): MockTestResult = MockTestResult(
    id = id,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    timeTaken = timeTakenSeconds.seconds,
    completedAt = Instant.parse(completedAt)
)

fun MockTestResult.toEntity(): MockTestResultEntity = MockTestResultEntity(
    id = id,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    timeTakenSeconds = timeTaken.inWholeSeconds,
    passed = passed,
    completedAt = completedAt.toString()
)
