package com.merkost.honq.presentation.di

import com.merkost.honq.presentation.screens.home.HomeContainer
import com.merkost.honq.presentation.screens.mocktest.MockTestContainer
import com.merkost.honq.presentation.screens.practice.PracticeContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

val presentationModule = module {
    factory { params -> HomeContainer(get(), get(), params.get<CoroutineScope>()) }
    factory { params -> PracticeContainer(get(), get(), params.get<CoroutineScope>()) }
    factory { params -> MockTestContainer(get(), get(), params.get<CoroutineScope>()) }
}
