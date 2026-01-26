package com.merkost.honq.domain.di

import com.merkost.honq.domain.usecase.GetMockTestQuestionsUseCase
import com.merkost.honq.domain.usecase.GetRandomQuestionsUseCase
import com.merkost.honq.domain.usecase.GetUserProgressUseCase
import com.merkost.honq.domain.usecase.RecordAnswerUseCase
import com.merkost.honq.domain.usecase.SaveMockTestResultUseCase
import com.merkost.honq.domain.usecase.SyncQuestionsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetRandomQuestionsUseCase(get()) }
    factory { GetMockTestQuestionsUseCase(get()) }
    factory { RecordAnswerUseCase(get()) }
    factory { SaveMockTestResultUseCase(get()) }
    factory { GetUserProgressUseCase(get()) }
    factory { SyncQuestionsUseCase(get()) }
}
