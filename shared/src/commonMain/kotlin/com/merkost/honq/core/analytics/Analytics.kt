package com.merkost.honq.core.analytics

interface Analytics {
    fun track(event: AnalyticsEvent)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
}
