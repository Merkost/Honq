package com.merkost.honq.core.analytics

import com.amplitude.kmp.Amplitude
import com.amplitude.kmp.AutocaptureOption
import com.amplitude.kmp.Configuration

actual fun createAmplitude(apiKey: String): Amplitude {
    val configuration = Configuration(
        apiKey = apiKey
    ).apply {
        autocapture = setOf(AutocaptureOption.SESSIONS)
    }
    return Amplitude(configuration)
}
