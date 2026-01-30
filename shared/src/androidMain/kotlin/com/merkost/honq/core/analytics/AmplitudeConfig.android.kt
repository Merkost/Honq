package com.merkost.honq.core.analytics

import android.app.Application

private var applicationContext: Application? = null

fun initAmplitudeContext(application: Application) {
    applicationContext = application
}

actual val platformContext: Any? get() = applicationContext
