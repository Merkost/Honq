package com.merkost.honq.core.analytics

import com.amplitude.kmp.Amplitude
import com.amplitude.kmp.Identify

class AmplitudeAnalytics(
    private val amplitude: Amplitude
) : Analytics {

    override fun track(event: AnalyticsEvent) {
        amplitude.track(event.name, event.properties)
    }

    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String) {
        val identify = Identify().set(name, value)
        amplitude.identify(identify)
    }
}
