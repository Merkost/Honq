package com.merkost.honq.core.analytics

import com.amplitude.kmp.Amplitude
import com.amplitude.kmp.events.Identify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class AmplitudeAnalytics(
    private val amplitude: Amplitude
) : Analytics {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun track(event: AnalyticsEvent) {
        scope.launch { amplitude.track(event.name, event.properties) }
    }

    override fun setUserId(userId: String?) {
        scope.launch { amplitude.setUserId(userId) }
    }

    override fun setUserProperty(name: String, value: String) {
        scope.launch {
            val identify = Identify().set(name, value)
            amplitude.identify(identify)
        }
    }
}
