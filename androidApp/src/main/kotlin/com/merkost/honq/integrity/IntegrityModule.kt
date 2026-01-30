package com.merkost.honq.integrity

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val integrityModule = module {
    single { PlayIntegrityService(androidContext()) }
    single { IntegrityRepository(get(), get()) }
}
