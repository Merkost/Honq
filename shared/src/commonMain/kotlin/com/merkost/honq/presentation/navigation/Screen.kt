package com.merkost.honq.presentation.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object About : Screen("about")
    data object Favorites : Screen("favorites")
    data object FavoriteQuestion : Screen("favorites/question/{questionId}") {
        fun createRoute(questionId: String) = "favorites/question/$questionId"
    }
    data object Practice : Screen("practice")
    data object CategorySelection : Screen("categories")
    data object PracticeByCategory : Screen("practice/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: String, categoryName: String): String {
            val encodedName = URLEncoder.encode(categoryName, "UTF-8")
            return "practice/$categoryId/$encodedName"
        }
    }
    data object MockTest : Screen("mocktest")
    data object Results : Screen("results/{score}/{total}/{hasIncorrect}") {
        fun createRoute(score: Int, total: Int, hasIncorrect: Boolean) = "results/$score/$total/$hasIncorrect"
    }
    data object ReviewIncorrect : Screen("review_incorrect")
    data object Search : Screen("search")
    data object SearchQuestion : Screen("search/question/{questionId}") {
        fun createRoute(questionId: String) = "search/question/$questionId"
    }
}
