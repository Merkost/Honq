package com.merkost.honq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.merkost.honq.presentation.screens.home.HomeScreen
import com.merkost.honq.presentation.screens.mocktest.MockTestScreen
import com.merkost.honq.presentation.screens.practice.PracticeScreen
import com.merkost.honq.presentation.screens.results.ResultsScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPractice = { navController.navigate(Screen.Practice.route) },
                onNavigateToMockTest = { navController.navigate(Screen.MockTest.route) }
            )
        }

        composable(Screen.Practice.route) {
            PracticeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MockTest.route) {
            MockTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResults = { score, total ->
                    navController.navigate(Screen.Results.createRoute(score, total)) {
                        popUpTo(Screen.MockTest.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.savedStateHandle.get<Int>("score") ?: 0
            val total = backStackEntry.savedStateHandle.get<Int>("total") ?: 0
            ResultsScreen(
                score = score,
                total = total,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.MockTest.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
    }
}
