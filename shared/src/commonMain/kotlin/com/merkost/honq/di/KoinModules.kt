package com.merkost.honq.di

import com.merkost.honq.core.di.coreModule
import com.merkost.honq.data.di.dataModule
import com.merkost.honq.domain.di.domainModule
import com.merkost.honq.presentation.di.presentationModule
import org.koin.core.module.Module

fun sharedModules(): List<Module> = listOf(
    coreModule,
    dataModule,
    domainModule,
    presentationModule
)
