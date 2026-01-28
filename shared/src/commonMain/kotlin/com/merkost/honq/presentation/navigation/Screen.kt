package com.merkost.honq.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Onboarding : Screen
    @Serializable data object Home : Screen
    @Serializable data object About : Screen
    @Serializable data object Favorites : Screen
    @Serializable data class FavoriteQuestion(val questionId: String) : Screen
    @Serializable data object Practice : Screen
    @Serializable data object CategorySelection : Screen
    @Serializable data class PracticeByCategory(val categoryId: String, val categoryName: String) : Screen
    @Serializable data object MockTest : Screen
    @Serializable data class Results(val score: Int, val total: Int, val hasIncorrect: Boolean) : Screen
    @Serializable data object ReviewIncorrect : Screen
    @Serializable data object Search : Screen
    @Serializable data class SearchQuestion(val questionId: String) : Screen
    @Serializable data object Statistics : Screen
    @Serializable data object WeakestQuestions : Screen
    @Serializable data class WeakestQuestion(val questionId: String) : Screen
    @Serializable data object UnansweredQuestions : Screen
    @Serializable data class UnansweredQuestion(val questionId: String) : Screen
    @Serializable data class MockTestReview(val mockTestResultId: Long) : Screen
}
