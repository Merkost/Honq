package com.merkost.honq.data.di

import com.merkost.honq.data.local.DataStoreInAppReviewPreferences
import com.merkost.honq.data.local.DataStoreOnboardingPreferences
import com.merkost.honq.data.local.DataStorePremiumPreferences
import com.merkost.honq.data.local.DataStoreSyncPreferences
import com.merkost.honq.data.local.DataStoreThemePreferences
import com.merkost.honq.data.local.InAppReviewPreferences
import com.merkost.honq.data.local.InMemoryQuestionSetSelectionRepository
import com.merkost.honq.data.local.InMemoryStateSelectionRepository
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.data.local.PremiumPreferences
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.local.ThemePreferences
import com.merkost.honq.data.local.createDataStore
import com.merkost.honq.data.premium.RevenueCatPremiumManager
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.remote.api.AppConfigApi
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.api.SupabaseConfig
import com.merkost.honq.data.repository.DataSyncManager
import com.merkost.honq.data.repository.FavoritesRepositoryImpl
import com.merkost.honq.data.repository.InMemoryReviewRepository
import com.merkost.honq.data.repository.ProgressRepositoryImpl
import com.merkost.honq.data.repository.QuestionRepositoryImpl
import com.merkost.honq.domain.repository.FavoritesRepository
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import com.merkost.honq.domain.repository.ReviewRepository
import com.merkost.honq.domain.repository.StateSelectionRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

val dataModule = module {
    single { createDataStore() }
    single<SyncPreferences> { DataStoreSyncPreferences(get()) }
    single<OnboardingPreferences> { DataStoreOnboardingPreferences(get()) }
    single<ThemePreferences> { DataStoreThemePreferences(get()) }
    single<InAppReviewPreferences> { DataStoreInAppReviewPreferences(get()) }
    single<PremiumPreferences> { DataStorePremiumPreferences(get()) }
    single<PremiumManager> { RevenueCatPremiumManager(get()) }
    single<StateSelectionRepository> { InMemoryStateSelectionRepository() }
    single<QuestionSetSelectionRepository> { InMemoryQuestionSetSelectionRepository() }

    single<SupabaseClient> { SupabaseConfig.createClient() }

    // Bundled-content seeding (replacing Supabase sync over the next several commits)
    single {
        com.merkost.honq.data.local.seed.StateResourcesProvider(
            json = get(),
            readBundle = {
                @OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
                honq.shared.generated.resources.Res.readBytes("files/content/v1/state_resources.json")
            },
        )
    }
    single {
        com.merkost.honq.data.local.seed.BundledContentLoader(
            localDataSource = get(),
            stateResourcesProvider = get(),
            syncPreferences = get(),
            json = get(),
            dispatchers = get(),
        )
    }

    single { QuestionApi(get()) }
    single { AppConfigApi(get()) }
    single { DataSyncManager(get(), get()) }
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
        QuestionRepositoryImpl(get(), get(), get(), get(), get(), get())
    }
    single<ProgressRepository> { ProgressRepositoryImpl(get(), get(), get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get()) }
    single<ReviewRepository> { InMemoryReviewRepository() }
}
