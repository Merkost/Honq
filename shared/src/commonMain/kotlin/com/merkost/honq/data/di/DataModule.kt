package com.merkost.honq.data.di

import com.merkost.honq.data.local.SampleDataSeeder
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.api.SupabaseConfig
import com.merkost.honq.data.repository.FakeQuestionRepository
import com.merkost.honq.data.repository.ProgressRepositoryImpl
import com.merkost.honq.data.repository.QuestionRepositoryImpl
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionRepository
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseConfig.createClient() }
    single { QuestionApi(get()) }
    single { QuestionLocalDataSource(get(), get(), get(), get()) }
    single { SampleDataSeeder(get(), get()) }
    // Use FakeQuestionRepository for testing, swap to QuestionRepositoryImpl for production
    single<QuestionRepository> { FakeQuestionRepository() }
    // single<QuestionRepository> { QuestionRepositoryImpl(get(), get(), get(), get(), get()) }
    single<ProgressRepository> { ProgressRepositoryImpl(get(), get()) }
}
