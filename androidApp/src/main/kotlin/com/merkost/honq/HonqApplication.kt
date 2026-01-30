package com.merkost.honq

import android.app.Application
import com.merkost.honq.core.analytics.initAmplitudeContext
import com.merkost.honq.core.logging.initCedar
import com.merkost.honq.data.local.initDataStore
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class HonqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        initCedar()

        initDataStore(this)
        initAmplitudeContext(this)
    }

}
