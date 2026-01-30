package com.merkost.honq.core.analytics

import com.amplitude.kmp.Amplitude

expect fun createAmplitude(apiKey: String): Amplitude
