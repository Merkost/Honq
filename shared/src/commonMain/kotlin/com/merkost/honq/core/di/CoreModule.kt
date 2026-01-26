package com.merkost.honq.core.di

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.core.util.AppDispatchersImpl
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
}
