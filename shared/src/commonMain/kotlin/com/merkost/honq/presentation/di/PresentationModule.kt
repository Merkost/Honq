package com.merkost.honq.presentation.di

import com.merkost.honq.presentation.screens.home.HomeContainer
import com.merkost.honq.presentation.screens.favorites.FavoritesContainer
import com.merkost.honq.presentation.screens.favorites.FavoriteQuestionContainer
import com.merkost.honq.presentation.screens.mocktest.MockTestContainer
import com.merkost.honq.presentation.screens.onboarding.OnboardingContainer
import com.merkost.honq.presentation.screens.categories.CategorySelectionContainer
import com.merkost.honq.presentation.screens.practice.PracticeContainer
import com.merkost.honq.presentation.screens.review.ReviewIncorrectContainer
import com.merkost.honq.presentation.screens.search.SearchContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

val presentationModule = module {
    factory { params -> OnboardingContainer(get(), get(), get(), get(), get(), get(), params.get<CoroutineScope>()) }
    factory { HomeContainer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { params ->
        PracticeContainer(
            params.getOrNull<String>(0),
            params.getOrNull<String>(1),
            get(),
            get(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { params -> CategorySelectionContainer(get(), get(), params.get<CoroutineScope>()) }
    factory { params -> MockTestContainer(get(), get(), get(), get(), get(), get(), params.get<CoroutineScope>()) }
    factory { params -> FavoritesContainer(get(), get(), get(), get(), params.get<CoroutineScope>()) }
    factory { params ->
        FavoriteQuestionContainer(
            params.get<String>(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { params -> ReviewIncorrectContainer(get(), get(), get(), get(), params.get<CoroutineScope>()) }
    factory { params -> SearchContainer(get(), get(), get(), get(), params.get<CoroutineScope>()) }
}
