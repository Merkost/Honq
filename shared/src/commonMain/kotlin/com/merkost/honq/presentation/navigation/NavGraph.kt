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
import com.merkost.honq.presentation.screens.practice.PracticeScreen
import com.merkost.honq.presentation.screens.results.ResultsScreen
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
                onNavigateToMockTest = { navController.navigate(Screen.MockTest.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
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
