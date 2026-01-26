package com.merkost.honq.core.analytics

import android.app.Application
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.Amplitude as AmplitudeCore

private var applicationContext: Application? = null

fun initAmplitudeContext(application: Application) {
    applicationContext = application
}

actual fun createAmplitude(apiKey: String): AmplitudeCore {
    val context = requireNotNull(applicationContext) {
        "Amplitude context not initialized. Call initAmplitudeContext() first."
    }
    return Amplitude(
        Configuration(
            apiKey = apiKey,
            context = context,
            trackingSessionEvents = true
        )
    )
}
