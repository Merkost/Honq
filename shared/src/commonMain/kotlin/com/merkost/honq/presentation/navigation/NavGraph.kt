package com.merkost.honq.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.presentation.screens.about.AboutScreen
import com.merkost.honq.presentation.screens.onboarding.OnboardingScreen
import org.koin.compose.koinInject
import com.merkost.honq.presentation.screens.favorites.FavoriteQuestionScreen
import com.merkost.honq.presentation.screens.favorites.FavoritesScreen
import com.merkost.honq.presentation.screens.home.HomeScreen
import com.merkost.honq.presentation.screens.mocktest.MockTestScreen
import com.merkost.honq.presentation.screens.categories.CategorySelectionScreen
import com.merkost.honq.presentation.screens.practice.PracticeScreen
import com.merkost.honq.presentation.screens.results.ResultsScreen
import com.merkost.honq.presentation.screens.review.ReviewIncorrectScreen
import com.merkost.honq.presentation.screens.search.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqMotion

private val TRANSITION_DURATION = HonqMotion.durationMedium

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(TRANSITION_DURATION, easing = HonqMotion.easingStandard)
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(TRANSITION_DURATION, easing = HonqMotion.easingStandard)
    ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(TRANSITION_DURATION, easing = HonqMotion.easingStandard)
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(TRANSITION_DURATION, easing = HonqMotion.easingStandard)
    ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val onboardingPreferences = koinInject<OnboardingPreferences>()
    val analytics = koinInject<Analytics>()
    val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.collectAsState()

    val startDestination = when (isOnboardingCompleted.value) {
        true -> Screen.Home.route
        false -> Screen.Onboarding.route
        null -> Screen.Onboarding.route
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val screenName = destination.route?.substringBefore("/") ?: return@OnDestinationChangedListener
            analytics.track(AnalyticsEvent.ScreenViewed(screenName))
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.background(HonqColors.Background),
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() }
    ) {
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { defaultExitTransition() },
            popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            HomeScreen(
                onNavigateToPractice = { navController.navigate(Screen.Practice.route) },
                onNavigateToCategories = { navController.navigate(Screen.CategorySelection.route) },
                onNavigateToMockTest = { navController.navigate(Screen.MockTest.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.FavoriteQuestion.createRoute(questionId))
                }
            )
        }

        composable(
            route = Screen.FavoriteQuestion.route,
            arguments = listOf(
                navArgument("questionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionId = backStackEntry.savedStateHandle.get<String>("questionId").orEmpty()
            FavoriteQuestionScreen(
                questionId = questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Practice.route) {
            PracticeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CategorySelection.route) {
            CategorySelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPractice = { categoryId, categoryName ->
                    if (categoryId != null && categoryName != null) {
                        navController.navigate(Screen.PracticeByCategory.createRoute(categoryId, categoryName)) {
                            popUpTo(Screen.CategorySelection.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Practice.route) {
                            popUpTo(Screen.CategorySelection.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.PracticeByCategory.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val categoryName = backStackEntry.arguments?.getString("categoryName")?.let {
                URLDecoder.decode(it, "UTF-8")
            }
            PracticeScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MockTest.route) {
            MockTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResults = { score, total, hasIncorrect ->
                    navController.navigate(Screen.Results.createRoute(score, total, hasIncorrect)) {
                        popUpTo(Screen.MockTest.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("hasIncorrect") { type = NavType.BoolType }
            ),
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(TRANSITION_DURATION, easing = HonqMotion.easingStandard)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val hasIncorrect = backStackEntry.arguments?.getBoolean("hasIncorrect") ?: false
            ResultsScreen(
                score = score,
                total = total,
                hasIncorrect = hasIncorrect,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.MockTest.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onReviewIncorrect = {
                    navController.navigate(Screen.ReviewIncorrect.route)
                }
            )
        }

        composable(Screen.ReviewIncorrect.route) {
            ReviewIncorrectScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.SearchQuestion.createRoute(questionId))
                }
            )
        }

        composable(
            route = Screen.SearchQuestion.route,
            arguments = listOf(
                navArgument("questionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId").orEmpty()
            FavoriteQuestionScreen(
                questionId = questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
