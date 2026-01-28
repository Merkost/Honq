package com.merkost.honq.presentation.di

import com.merkost.honq.presentation.screens.categories.CategorySelectionContainer
import com.merkost.honq.presentation.screens.favorites.FavoriteQuestionContainer
import com.merkost.honq.presentation.screens.favorites.FavoritesContainer
import com.merkost.honq.presentation.screens.home.HomeContainer
import com.merkost.honq.presentation.screens.mocktest.MockTestContainer
import com.merkost.honq.presentation.screens.mocktestview.MockTestReviewContainer
import com.merkost.honq.presentation.screens.onboarding.OnboardingContainer
import com.merkost.honq.presentation.screens.practice.PracticeContainer
import com.merkost.honq.presentation.screens.review.ReviewIncorrectContainer
import com.merkost.honq.presentation.screens.search.SearchContainer
import com.merkost.honq.presentation.screens.statistics.StatisticsContainer
import com.merkost.honq.presentation.screens.unanswered.UnansweredQuestionsContainer
import com.merkost.honq.presentation.screens.weakest.WeakestQuestionsContainer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

val presentationModule = module {
    factory { params ->
        OnboardingContainer(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory {
        HomeContainer(
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
    factory { params ->
        PracticeContainer(
            params.getOrNull<String>(),
            params.getOrNull<String>(),
            get(),
            get(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { params ->
        CategorySelectionContainer(
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { params ->
        MockTestContainer(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { params -> FavoritesContainer(get(), get(), get()) }
    factory { params ->
        FavoriteQuestionContainer(
            params.get<String>(),
            get(),
            get(),
            get(),
        )
    }
    factory { params ->
        ReviewIncorrectContainer(
            get(),
            get(),
            get(),
            get(),
            params.get<CoroutineScope>()
        )
    }
    factory { _ -> SearchContainer(get(), get(), get(), get()) }
    factory { _ -> StatisticsContainer(get(), get()) }
    factory { params -> WeakestQuestionsContainer(get(), get(), params.get<CoroutineScope>()) }
    factory { params -> UnansweredQuestionsContainer(get(), get(), params.get<CoroutineScope>()) }
    factory { params ->
        MockTestReviewContainer(
            get(),
            get(),
            get(),
            get(),
            params.get<Long>(),
            params.get<CoroutineScope>()
        )
    }
}
