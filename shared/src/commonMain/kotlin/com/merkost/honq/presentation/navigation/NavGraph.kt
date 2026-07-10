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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.data.local.OnboardingPreferences
import com.merkost.honq.domain.model.CategoryScore
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.presentation.screens.about.AboutScreen
import com.merkost.honq.presentation.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import com.merkost.honq.presentation.screens.favorites.FavoriteQuestionScreen
import com.merkost.honq.presentation.screens.favorites.FavoritesScreen
import com.merkost.honq.presentation.screens.home.HomeScreen
import com.merkost.honq.presentation.screens.mocktest.MockTestScreen
import com.merkost.honq.presentation.screens.categories.CategorySelectionScreen
import com.merkost.honq.presentation.screens.practice.PracticeScreen
import com.merkost.honq.presentation.screens.results.ResultsScreen
import com.merkost.honq.presentation.screens.review.ReviewIncorrectScreen
import com.merkost.honq.presentation.screens.mocktestview.MockTestReviewScreen
import com.merkost.honq.presentation.screens.search.SearchScreen
import com.merkost.honq.presentation.screens.statistics.StatisticsScreen
import com.merkost.honq.presentation.screens.unanswered.UnansweredQuestionsScreen
import com.merkost.honq.presentation.screens.weakest.WeakestQuestionsScreen
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.util.requestInAppReview

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
    val premiumManager = koinInject<PremiumManager>()
    val coroutineScope = rememberCoroutineScope()
    val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.collectAsState()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            premiumManager.syncPremiumStatus()
        }
        onPauseOrDispose { }
    }

    val startDestination: Screen = when (isOnboardingCompleted.value) {
        true -> Screen.Home
        false -> Screen.Onboarding
        null -> Screen.Onboarding
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val screenName = destination.route
                ?.substringAfterLast(".")
                ?.substringBefore("/")
                ?.substringBefore("?")
                ?.lowercase()
                ?: return@OnDestinationChangedListener
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
        modifier = Modifier.background(HonqTheme.colors.background),
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() }
    ) {
        composable<Screen.Onboarding>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { defaultExitTransition() },
            popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            LaunchedEffect(Unit) {
                requestInAppReview("PRACTICE_MILESTONE")
            }
            HomeScreen(
                onNavigateToPractice = { navController.navigate(Screen.CategorySelection) },
                onNavigateToRandomPractice = { navController.navigate(Screen.Practice) },
                onNavigateToSmartPractice = { navController.navigate(Screen.SmartPractice) },
                onNavigateToMockTest = { navController.navigate(Screen.MockTest) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites) },
                onNavigateToSearch = { navController.navigate(Screen.Search) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics) },
                onNavigateToAbout = { navController.navigate(Screen.About) }
            )
        }

        composable<Screen.About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Favorites> {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.FavoriteQuestion(questionId))
                }
            )
        }

        composable<Screen.FavoriteQuestion> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.FavoriteQuestion>()
            FavoriteQuestionScreen(
                questionId = route.questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Practice> {
            PracticeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.SmartPractice> {
            PracticeScreen(
                smartMode = true,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.CategorySelection> {
            CategorySelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPractice = { categoryId, categoryName ->
                    if (categoryId != null && categoryName != null) {
                        navController.navigate(Screen.PracticeByCategory(categoryId, categoryName)) {
                            popUpTo<Screen.CategorySelection> { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Practice) {
                            popUpTo<Screen.CategorySelection> { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Screen.PracticeByCategory> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PracticeByCategory>()
            PracticeScreen(
                categoryId = route.categoryId,
                categoryName = route.categoryName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.MockTest> {
            MockTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResults = { score, total, hasIncorrect, passPercentage, categoryBreakdown ->
                    val breakdownJson = Json.encodeToString(categoryBreakdown)
                    navController.navigate(Screen.Results(score, total, hasIncorrect, passPercentage, breakdownJson)) {
                        popUpTo<Screen.MockTest> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Results>(
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
            val route = backStackEntry.toRoute<Screen.Results>()
            val passed = route.total > 0 && ((route.score.toFloat() / route.total) * 100).toInt() >= route.passPercentage
            if (passed) {
                LaunchedEffect(Unit) {
                    requestInAppReview("MOCK_TEST_PASSED")
                }
            }
            val categoryBreakdown = try {
                Json.decodeFromString<List<CategoryScore>>(route.categoryBreakdownJson)
            } catch (_: Exception) {
                emptyList()
            }
            ResultsScreen(
                score = route.score,
                total = route.total,
                passPercentage = route.passPercentage,
                categoryBreakdown = categoryBreakdown,
                hasIncorrect = route.hasIncorrect,
                onNavigateHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
                onRetry = {
                    if (premiumManager.isPremium.value || premiumManager.freeTrialMockTestsRemaining.value > 0) {
                        navController.navigate(Screen.MockTest) {
                            popUpTo<Screen.Home>()
                        }
                    } else {
                        navController.navigate(Screen.Home) {
                            popUpTo<Screen.Home> { inclusive = true }
                        }
                    }
                },
                onReviewIncorrect = {
                    navController.navigate(Screen.ReviewIncorrect)
                }
            )
        }

        composable<Screen.ReviewIncorrect> {
            ReviewIncorrectScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.SearchQuestion(questionId))
                }
            )
        }

        composable<Screen.SearchQuestion> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.SearchQuestion>()
            FavoriteQuestionScreen(
                questionId = route.questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Statistics> {
            LaunchedEffect(Unit) {
                requestInAppReview("STATISTICS_VIEWED")
            }
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWeakestQuestions = { navController.navigate(Screen.WeakestQuestions) },
                onNavigateToUnansweredQuestions = { navController.navigate(Screen.UnansweredQuestions) },
                onNavigateToMockTestReview = { mockTestResultId ->
                    navController.navigate(Screen.MockTestReview(mockTestResultId))
                }
            )
        }

        composable<Screen.MockTestReview> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.MockTestReview>()
            MockTestReviewScreen(
                mockTestResultId = route.mockTestResultId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.WeakestQuestions> {
            WeakestQuestionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.WeakestQuestion(questionId))
                }
            )
        }

        composable<Screen.WeakestQuestion> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.WeakestQuestion>()
            FavoriteQuestionScreen(
                questionId = route.questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.UnansweredQuestions> {
            UnansweredQuestionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestion = { questionId ->
                    navController.navigate(Screen.UnansweredQuestion(questionId))
                }
            )
        }

        composable<Screen.UnansweredQuestion> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.UnansweredQuestion>()
            FavoriteQuestionScreen(
                questionId = route.questionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
