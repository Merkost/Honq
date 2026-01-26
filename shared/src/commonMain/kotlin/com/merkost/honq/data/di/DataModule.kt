package com.merkost.honq.data.di

import com.merkost.honq.data.local.DataStoreOnboardingPreferences
import com.merkost.honq.data.local.InMemoryQuestionSetSelectionRepository
import com.merkost.honq.data.local.InMemoryStateSelectionRepository
import com.merkost.honq.data.local.InMemorySyncPreferences
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.local.createDataStore
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.api.SupabaseConfig
import com.merkost.honq.data.repository.FavoritesRepositoryImpl
import com.merkost.honq.data.repository.ProgressRepositoryImpl
import com.merkost.honq.data.repository.QuestionRepositoryImpl
import com.merkost.honq.domain.repository.FavoritesRepository
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import com.merkost.honq.domain.repository.StateSelectionRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

val dataModule = module {
    single { createDataStore() }
    single<SyncPreferences> { InMemorySyncPreferences() }
    single<OnboardingPreferences> { DataStoreOnboardingPreferences(get()) }
    single<StateSelectionRepository> { InMemoryStateSelectionRepository() }
    single<QuestionSetSelectionRepository> { InMemoryQuestionSetSelectionRepository() }

    single<SupabaseClient> { SupabaseConfig.createClient() }
    single { QuestionApi(get()) }
    single {
        QuestionLocalDataSource(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single<QuestionRepository> {
        QuestionRepositoryImpl(get(), get(), get(), get(), get())
    }
    single<ProgressRepository> { ProgressRepositoryImpl(get(), get(), get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get()) }
}
