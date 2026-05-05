package com.merkost.honq.presentation.screens.home

import com.merkost.honq.domain.model.UserProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadinessTest {

    private fun progress(
        total: Int = 100,
        unique: Int = 0,
        practiced: Int = 0,
        correct: Int = 0,
        mocksTaken: Int = 0,
        mocksPassed: Int = 0
    ) = UserProgress(
        totalQuestions = total,
        uniqueQuestionsAnswered = unique,
        totalPracticed = practiced,
        correctAnswers = correct,
        mockTestsTaken = mocksTaken,
        mockTestsPassed = mocksPassed,
        lastPracticeDate = null
    )

    @Test
    fun zero_when_no_questions_in_set() {
        assertEquals(0, Readiness.score(UserProgress.EMPTY))
    }

    @Test
    fun zero_when_nothing_practiced() {
        assertEquals(0, Readiness.score(progress(total = 100)))
    }

    @Test
    fun low_score_when_high_accuracy_but_almost_no_coverage() {
        // 5/100 covered, 100% accuracy: shouldn't claim more than ~10
        val score = Readiness.score(
            progress(total = 100, unique = 5, practiced = 5, correct = 5)
        )
        assertTrue(score in 0..15, "expected low confidence-dampened score, got $score")
    }

    @Test
    fun strong_score_when_well_practiced_and_accurate() {
        // 80/100 covered, 80% accuracy, all mocks passed: should be ready
        val score = Readiness.score(
            progress(
                total = 100,
                unique = 80,
                practiced = 100,
                correct = 80,
                mocksTaken = 3,
                mocksPassed = 3
            )
        )
        assertTrue(score >= 80, "expected pass-mark-or-above, got $score")
    }

    @Test
    fun mid_score_for_moderate_coverage_and_accuracy() {
        // 60/100 covered, 75% accuracy, no mocks
        val score = Readiness.score(
            progress(total = 100, unique = 60, practiced = 80, correct = 60)
        )
        assertTrue(score in 60..78, "expected mid-range readiness, got $score")
    }

    @Test
    fun zone_green_at_or_above_pass_mark() {
        assertEquals(ReadinessZone.Green, Readiness.zone(80, passMark = 80))
        assertEquals(ReadinessZone.Green, Readiness.zone(95, passMark = 80))
    }

    @Test
    fun zone_amber_within_20_points_of_pass_mark() {
        assertEquals(ReadinessZone.Amber, Readiness.zone(79, passMark = 80))
        assertEquals(ReadinessZone.Amber, Readiness.zone(60, passMark = 80))
    }

    @Test
    fun zone_red_when_more_than_20_below_pass_mark() {
        assertEquals(ReadinessZone.Red, Readiness.zone(59, passMark = 80))
        assertEquals(ReadinessZone.Red, Readiness.zone(0, passMark = 80))
    }

    @Test
    fun points_to_pass_clamps_at_zero_when_already_passing() {
        assertEquals(0, Readiness.pointsToPass(85, passMark = 80))
        assertEquals(0, Readiness.pointsToPass(80, passMark = 80))
        assertEquals(8, Readiness.pointsToPass(72, passMark = 80))
    }

    @Test
    fun honors_custom_pass_mark_from_question_set() {
        // Some states use 75% — ensure the helper threads it through
        assertEquals(ReadinessZone.Green, Readiness.zone(75, passMark = 75))
        assertEquals(ReadinessZone.Amber, Readiness.zone(60, passMark = 75))
        assertEquals(15, Readiness.pointsToPass(60, passMark = 75))
    }
}
