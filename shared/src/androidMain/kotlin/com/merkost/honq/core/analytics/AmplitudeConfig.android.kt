package com.merkost.honq.core.analytics

import android.app.Application
import com.amplitude.kmp.Amplitude
import com.amplitude.kmp.AutocaptureOption
import com.amplitude.kmp.Configuration

private var applicationContext: Application? = null

fun initAmplitudeContext(application: Application) {
    applicationContext = application
}

actual fun createAmplitude(apiKey: String): Amplitude {
    val context = requireNotNull(applicationContext) {
        "Amplitude context not initialized. Call initAmplitudeContext() first."
    }
    val configuration = Configuration(
        apiKey = apiKey,
        androidContext = context
    ).apply {
        autocapture = setOf(AutocaptureOption.SESSIONS)
    }
    return Amplitude(configuration)
}
