package com.merkost.honq.core.analytics

import com.amplitude.core.Amplitude
import com.amplitude.core.Configuration

actual fun createAmplitude(apiKey: String): Amplitude {
    return Amplitude(
        Configuration(
            apiKey = apiKey,
            trackingSessionEvents = true
        )
    )
}
