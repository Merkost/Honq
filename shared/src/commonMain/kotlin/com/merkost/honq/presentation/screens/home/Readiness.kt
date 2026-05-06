package com.merkost.honq.presentation.screens.home

import com.merkost.honq.domain.model.UserProgress
import kotlin.math.roundToInt

object Readiness {

    private const val DEFAULT_PASS_PERCENTAGE = 80

    private const val ACCURACY_WEIGHT = 0.55f
    private const val COVERAGE_WEIGHT = 0.30f
    private const val MOCK_WEIGHT = 0.15f

    private const val FULL_CONFIDENCE_COVERAGE = 0.5f
    private const val AMBER_BAND_BELOW_PASS = 20

    fun score(progress: UserProgress): Int {
        if (progress.totalQuestions == 0) return 0

        val coverage = progress.completionProgress.coerceIn(0f, 1f)
        val accuracy = progress.practiceAccuracy.coerceIn(0f, 1f)
        val mockSignal = if (progress.mockTestsTaken == 0) accuracy
        else progress.mockTestPassRate.coerceIn(0f, 1f)

        val confidence = (coverage / FULL_CONFIDENCE_COVERAGE).coerceAtMost(1f)

        val raw = ACCURACY_WEIGHT * accuracy * confidence +
            COVERAGE_WEIGHT * coverage +
            MOCK_WEIGHT * mockSignal * confidence

        return (raw * 100f).roundToInt().coerceIn(0, 100)
    }

    fun zone(score: Int, passMark: Int = DEFAULT_PASS_PERCENTAGE): ReadinessZone = when {
        score >= passMark -> ReadinessZone.Green
        score >= passMark - AMBER_BAND_BELOW_PASS -> ReadinessZone.Amber
        else -> ReadinessZone.Red
    }

    fun pointsToPass(score: Int, passMark: Int = DEFAULT_PASS_PERCENTAGE): Int =
        (passMark - score).coerceAtLeast(0)
}

enum class ReadinessZone { Red, Amber, Green }
