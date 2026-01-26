package com.merkost.honq.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object About : Screen("about")
    data object Favorites : Screen("favorites")
    data object FavoriteQuestion : Screen("favorites/question/{questionId}") {
        fun createRoute(questionId: String) = "favorites/question/$questionId"
    }
    data object Practice : Screen("practice")
    data object MockTest : Screen("mocktest")
    data object Results : Screen("results/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "results/$score/$total"
    }
}
