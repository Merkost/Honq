package com.merkost.honq.domain.di

import com.merkost.honq.domain.usecase.GetCategoriesUseCase
import com.merkost.honq.domain.usecase.GetCategoryProgressUseCase
import com.merkost.honq.domain.usecase.GetMockTestReviewUseCase
import com.merkost.honq.domain.usecase.ReviewEligibilityManager
import com.merkost.honq.domain.usecase.GetStatisticsUseCase
import com.merkost.honq.domain.usecase.GetUnansweredQuestionsUseCase
import com.merkost.honq.domain.usecase.GetSmartPracticeQuestionsUseCase
import com.merkost.honq.domain.usecase.GetWeakestQuestionsUseCase
import com.merkost.honq.domain.usecase.SearchQuestionsUseCase
import com.merkost.honq.domain.usecase.GetLicenseTypesUseCase
import com.merkost.honq.domain.usecase.GetMockTestQuestionsUseCase
import com.merkost.honq.domain.usecase.GetQuestionSetsByStateUseCase
import com.merkost.honq.domain.usecase.GetRandomQuestionsUseCase
import com.merkost.honq.domain.usecase.GetQuestionByIdUseCase
import com.merkost.honq.domain.usecase.GetStateResourcesUseCase
import com.merkost.honq.domain.usecase.GetStatesUseCase
import com.merkost.honq.domain.usecase.ObserveSelectedStateUseCase
import com.merkost.honq.domain.usecase.ObserveSelectedQuestionSetUseCase
import com.merkost.honq.domain.usecase.GetUserProgressUseCase
import com.merkost.honq.domain.usecase.HasIncorrectAnswersUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionIdsUseCase
import com.merkost.honq.domain.usecase.ObserveFavoriteQuestionsUseCase
import com.merkost.honq.domain.usecase.ObserveIncorrectAnswersUseCase
import com.merkost.honq.domain.usecase.RecordAnswerUseCase
import com.merkost.honq.domain.usecase.SaveIncorrectAnswersUseCase
import com.merkost.honq.domain.usecase.SaveMockTestResultUseCase
import com.merkost.honq.domain.usecase.SetSelectedQuestionSetUseCase
import com.merkost.honq.domain.usecase.SetSelectedStateUseCase
import com.merkost.honq.domain.usecase.ToggleFavoriteQuestionUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetRandomQuestionsUseCase(get(), get()) }
    factory { GetMockTestQuestionsUseCase(get(), get()) }
    factory { GetQuestionByIdUseCase(get()) }
    factory { RecordAnswerUseCase(get()) }
    factory { SaveMockTestResultUseCase(get()) }
    factory { GetUserProgressUseCase(get()) }
    factory { GetStatesUseCase(get()) }
    factory { GetStateResourcesUseCase(get()) }
    factory { GetLicenseTypesUseCase(get()) }
    factory { GetQuestionSetsByStateUseCase(get()) }
    factory { ObserveSelectedStateUseCase(get()) }
    factory { ObserveSelectedQuestionSetUseCase(get()) }
    factory { SetSelectedQuestionSetUseCase(get()) }
    factory { SetSelectedStateUseCase(get(), get(), get()) }
    factory { ObserveFavoriteQuestionsUseCase(get()) }
    factory { ObserveFavoriteQuestionIdsUseCase(get()) }
    factory { ToggleFavoriteQuestionUseCase(get()) }
    factory { SaveIncorrectAnswersUseCase(get()) }
    factory { ObserveIncorrectAnswersUseCase(get()) }
    factory { HasIncorrectAnswersUseCase(get()) }
    factory { GetCategoriesUseCase(get(), get()) }
    factory { GetCategoryProgressUseCase(get(), get()) }
    factory { SearchQuestionsUseCase(get(), get()) }
    factory { GetStatisticsUseCase(get()) }
    factory { GetWeakestQuestionsUseCase(get()) }
    factory { GetUnansweredQuestionsUseCase(get()) }
    factory { GetMockTestReviewUseCase(get()) }
    factory { GetSmartPracticeQuestionsUseCase(get()) }
    single { ReviewEligibilityManager(get(), get()) }
}
