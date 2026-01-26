package com.merkost.honq.core.analytics

import com.amplitude.core.Amplitude

expect fun createAmplitude(apiKey: String): Amplitude
