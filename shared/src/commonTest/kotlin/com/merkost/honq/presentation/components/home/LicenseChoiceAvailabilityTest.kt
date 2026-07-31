package com.merkost.honq.presentation.components.home

import kotlin.test.Test
import kotlin.test.assertEquals

class LicenseChoiceAvailabilityTest {
    @Test
    fun empty_question_sets_while_syncing_is_updating() {
        assertEquals(
            LicenseChoiceAvailability.Updating,
            licenseChoiceAvailability(true, questionSetCount = 0, hasQuestionSet = false),
        )
    }

    @Test
    fun matching_question_set_is_available_even_while_background_sync_runs() {
        assertEquals(
            LicenseChoiceAvailability.Available,
            licenseChoiceAvailability(true, questionSetCount = 2, hasQuestionSet = true),
        )
    }

    @Test
    fun missing_question_set_is_unavailable_after_loading() {
        assertEquals(
            LicenseChoiceAvailability.Unavailable,
            licenseChoiceAvailability(false, questionSetCount = 2, hasQuestionSet = false),
        )
    }
}
