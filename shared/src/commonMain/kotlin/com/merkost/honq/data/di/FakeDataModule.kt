package com.merkost.honq.data.di

import com.merkost.honq.data.local.InMemoryOnboardingPreferences
import com.merkost.honq.data.local.InMemoryQuestionSetSelectionRepository
import com.merkost.honq.data.local.InMemoryStateSelectionRepository
import com.merkost.honq.data.local.InMemorySyncPreferences
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.remote.api.AppConfigApi
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.api.SupabaseConfig
import com.merkost.honq.data.repository.DataSyncManager
import com.merkost.honq.data.repository.FakeFavoritesRepository
import com.merkost.honq.data.repository.FakeProgressRepository
import com.merkost.honq.data.repository.FakeQuestionRepository
import com.merkost.honq.data.repository.InMemoryReviewRepository
import com.merkost.honq.domain.repository.FavoritesRepository
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import com.merkost.honq.domain.repository.ReviewRepository
import com.merkost.honq.domain.repository.StateSelectionRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module

/**
 * A fake data module with pre-populated mock data for screenshots and previews.
 *
 * Replace [dataModule] with [fakeDataModule] in KoinModules.kt to use:
 * ```
 * fun sharedModules(): List<Module> = listOf(
 *     coreModule,
 *     fakeDataModule,  // <-- swap here
 *     domainModule,
 *     presentationModule
 * )
 * ```
 */
val fakeDataModule = module {
    single<SyncPreferences> {
        InMemorySyncPreferences().apply {
            CoroutineScope(Dispatchers.Main).launch {
                setInitialSyncCompleted(true)
                setLocalDataVersion(1)
            }
        }
    }
    single<OnboardingPreferences> {
        InMemoryOnboardingPreferences().apply {
            setOnboardingCompleted(true)
            setSelectedStateId("nsw")
            setSelectedLicenseTypeId("car")
        }
    }
    single<StateSelectionRepository> { InMemoryStateSelectionRepository() }
    single<QuestionSetSelectionRepository> {
        InMemoryQuestionSetSelectionRepository().apply {
            setSelectedQuestionSetId("nsw_car")
        }
    }

    single<SupabaseClient> { SupabaseConfig.createClient() }
    single { QuestionApi(get()) }
    single { AppConfigApi(get()) }
    single { DataSyncManager(get(), get()) }

    single<QuestionRepository> { FakeQuestionRepository() }
    single<ProgressRepository> {
        val fakeQuestionRepo = get<QuestionRepository>() as FakeQuestionRepository
        FakeProgressRepository(questionsProvider = { fakeQuestionRepo.sampleQuestions })
    }
    single<FavoritesRepository> {
        val fakeQuestionRepo = get<QuestionRepository>() as FakeQuestionRepository
        FakeFavoritesRepository(questionsProvider = { fakeQuestionRepo.sampleQuestions })
    }
    single<ReviewRepository> { InMemoryReviewRepository() }
}
