package com.merkost.honq.presentation.components.home

internal enum class LicenseChoiceAvailability {
    Updating,
    Available,
    Unavailable,
}

internal fun licenseChoiceAvailability(
    isSyncing: Boolean,
    questionSetCount: Int,
    hasQuestionSet: Boolean,
): LicenseChoiceAvailability = when {
    isSyncing && questionSetCount == 0 -> LicenseChoiceAvailability.Updating
    hasQuestionSet -> LicenseChoiceAvailability.Available
    else -> LicenseChoiceAvailability.Unavailable
}
