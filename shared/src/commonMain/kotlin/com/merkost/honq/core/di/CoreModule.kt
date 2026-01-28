package com.merkost.honq.core.di

import com.merkost.honq.BuildKonfig
import com.merkost.honq.core.analytics.AmplitudeAnalytics
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.createAmplitude
import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.AppDispatchersImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreModule = module {
    single<AppDispatchers> { AppDispatchersImpl() }
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    single { createAmplitude(BuildKonfig.AMPLITUDE_API_KEY) }
    single<Analytics> { AmplitudeAnalytics(get()) }
    single { Firebase.crashlytics }
}
