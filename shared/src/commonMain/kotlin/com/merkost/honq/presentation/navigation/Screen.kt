package com.merkost.honq.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Practice : Screen("practice")
    data object MockTest : Screen("mocktest")
    data object Results : Screen("results/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "results/$score/$total"
    }
}
