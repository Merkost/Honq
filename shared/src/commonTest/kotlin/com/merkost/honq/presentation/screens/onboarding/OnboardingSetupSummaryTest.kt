package com.merkost.honq.presentation.screens.onboarding

import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnboardingSetupSummaryTest {
    @Test
    fun summary_is_hidden_until_both_choices_exist() {
        assertNull(createSetupSummary(null, null))
        assertNull(createSetupSummary(sampleState(), null))
        assertNull(createSetupSummary(null, sampleLicenseType()))
    }

    @Test
    fun summary_contains_state_and_license_labels() {
        assertEquals(
            SetupSummary("New South Wales", "NSW", "Car", "C"),
            createSetupSummary(sampleState(), sampleLicenseType()),
        )
    }
}

private fun sampleState() = State("nsw", "New South Wales", "NSW")
private fun sampleLicenseType() = LicenseType("car", "Car", "C")
