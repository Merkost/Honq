package com.merkost.honq.review

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val reviewModule = module {
    single { InAppReviewService(androidContext()) }
}
