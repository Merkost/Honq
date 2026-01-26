package com.merkost.honq

import android.app.Application
import com.merkost.honq.core.analytics.initAmplitudeContext
import com.merkost.honq.data.local.initDataStore

class HonqApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initDataStore(this)
        initAmplitudeContext(this)
    }
}
