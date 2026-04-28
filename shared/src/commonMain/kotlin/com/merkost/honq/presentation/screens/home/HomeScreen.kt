package com.merkost.honq.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.StateResource
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.presentation.components.base.FullscreenError
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.base.LicenseTypeIcon
import com.merkost.honq.presentation.components.base.ProBadge
import com.merkost.honq.presentation.screens.paywall.PurchaseSuccessScreen
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.util.openUrl
import honq.shared.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.dsl.intent

private const val STAGGER_DELAY = 60L
private const val SLIDE_UP_PX = 40f

@Composable
fun HomeScreen(
    onNavigateToPractice: () -> Unit,
    onNavigateToRandomPractice: () -> Unit,
    onNavigateToSmartPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val container = koinViewModel<HomeContainer>()
    val premiumManager: PremiumManager = koinInject()
    val isPremium by premiumManager.isPremium.collectAsState()
    val freeTestsRemaining by premiumManager.freeTrialMockTestsRemaining.collectAsState()

    val state by container.store.subscribe { action ->
        when (action) {
            HomeAction.NavigateToPractice -> onNavigateToPractice()
            HomeAction.NavigateToMockTest -> onNavigateToMockTest()
        }
    }

    var showPaywall by remember { mutableStateOf(false) }
    var showPurchaseSuccess by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun gatedNavigation(isPro: Boolean, navigate: () -> Unit) {
        if (isPro) {
            navigate()
        } else {
            pendingNavigation = navigate
            showPaywall = true
        }
    }

    HomeContent(
        state = state,
        isPremium = isPremium,
        freeTestsRemaining = freeTestsRemaining,
        onNavigateToPractice = {
            if (isPremium) onNavigateToPractice() else onNavigateToRandomPractice()
        },
        onNavigateToSmartPractice = {
            gatedNavigation(isPremium, onNavigateToSmartPractice)
        },
        onNavigateToMockTest = {
            if (isPremium || freeTestsRemaining > 0) {
                onNavigateToMockTest()
            } else {
                pendingNavigation = onNavigateToMockTest
                showPaywall = true
            }
        },
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToAbout = onNavigateToAbout,
        onSelectState = { stateId -> container.intent(HomeIntent.SelectState(stateId)) },
        onSelectLicenseType = { typeId -> container.intent(HomeIntent.SelectLicenseType(typeId)) },
        onRetry = { container.intent(HomeIntent.Retry) },
        onOpenExternalLink = { linkType, url ->
            container.intent(HomeIntent.OpenExternalLink(linkType, url))
        }
    )

    if (showPaywall) {
        Paywall(
            options = PaywallOptions(
                dismissRequest = {
                    showPaywall = false
                    pendingNavigation = null
                }
            ) {
                shouldDisplayDismissButton = true
                listener = object : PaywallListener {
                    override fun onPurchaseCompleted(
                        customerInfo: CustomerInfo,
                        storeTransaction: StoreTransaction
                    ) {
                        showPaywall = false
                        showPurchaseSuccess = true
                    }
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showPurchaseSuccess,
        enter = fadeIn(animationSpec = tween(HonqMotion.durationMedium)),
        exit = fadeOut(animationSpec = tween(HonqMotion.durationMedium))
    ) {
        PurchaseSuccessScreen(
            onContinue = {
                showPurchaseSuccess = false
                pendingNavigation?.invoke()
                pendingNavigation = null
            }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    isPremium: Boolean,
    freeTestsRemaining: Int,
    onNavigateToPractice: () -> Unit,
    onNavigateToSmartPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onSelectState: (String) -> Unit,
    onSelectLicenseType: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors

    val selectedState = state.states.firstOrNull { it.id == state.selectedStateId }
    val isExternalOnly = selectedState?.isExternalOnly == true

    HonqScaffold(
        title = stringResource(Res.string.app_name),
        showLogo = true,
        actions = {
            IconButton(onClick = onNavigateToSearch) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = colors.textPrimary
                )
            }
            IconButton(onClick = onNavigateToAbout) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(Res.string.home_about),
                    tint = colors.textPrimary
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isInitialLoading -> {
                    FullscreenLoading()
                }

                state.initialLoadError != null -> {
                    FullscreenError(
                        onRetry = onRetry,
                        errorDetail = state.initialLoadError
                    )
                }

                else -> {
                    val itemCount = if (isExternalOnly) 2 else {
                        5 + (if (state.stateResources.isNotEmpty()) 1 else 0)
                    }
                    val animProgress = remember { List(itemCount) { Animatable(0f) } }
                    LaunchedEffect(Unit) {
                        animProgress.forEachIndexed { index, anim ->
                            launch {
                                delay(index * STAGGER_DELAY)
                                anim.animateTo(
                                    1f,
                                    animationSpec = tween(
                                        durationMillis = HonqMotion.durationEnter,
                                        easing = HonqMotion.easingEmphasizedDecelerate
                                    )
                                )
                            }
                        }
                    }

                    fun Modifier.staggeredEntrance(index: Int): Modifier {
                        val progress = animProgress.getOrNull(index)?.value ?: 1f
                        return this
                            .alpha(progress)
                            .offset { IntOffset(0, ((1f - progress) * SLIDE_UP_PX).toInt()) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(HonqSizing.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
                    ) {
                        Box(modifier = Modifier.staggeredEntrance(0)) {
                            ConfigurationCard(
                                state,
                                onSelectState,
                                onSelectLicenseType,
                                onOpenExternalLink
                            )
                        }

                        if (isExternalOnly) {
                            Box(modifier = Modifier.staggeredEntrance(1)) {
                                ExternalResourcesCard(
                                    state = selectedState!!,
                                    onOpenExternalLink = onOpenExternalLink
                                )
                            }
                        } else {
                            Box(modifier = Modifier.staggeredEntrance(1)) {
                                HeroProgressCard(state = state)
                            }
                            Box(modifier = Modifier.staggeredEntrance(2)) {
                                PillGrid(
                                    state = state,
                                    isPremium = isPremium,
                                    freeTestsRemaining = freeTestsRemaining,
                                    onNavigateToPractice = onNavigateToPractice,
                                    onNavigateToMockTest = onNavigateToMockTest,
                                    onNavigateToSmartPractice = onNavigateToSmartPractice,
                                    onNavigateToFavorites = onNavigateToFavorites
                                )
                            }
                            Box(modifier = Modifier.staggeredEntrance(3)) {
                                CompactStatsRow(state, onNavigateToStatistics)
                            }

                            if (state.stateResources.isNotEmpty()) {
                                Box(modifier = Modifier.staggeredEntrance(4)) {
                                    OfficialResourcesCard(
                                        resources = state.stateResources,
                                        onOpenExternalLink = onOpenExternalLink
                                    )
                                }
                            }
                        }
                    }

                    SyncIndicator(
                        isSyncing = state.isSyncing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

// region Hero Progress Card

@Composable
private fun HeroProgressCard(state: HomeState) {
    val colors = HonqTheme.colors
    val progress = state.progress

    HonqCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
        ) {
            ProgressRing(
                progress = progress.completionProgress,
                modifier = Modifier.size(HonqSizing.heroRingSize)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    AnimatedContent(
                        targetState = progress.uniqueQuestionsAnswered,
                        transitionSpec = {
                            fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                        }
                    ) { answered ->
                        Text(
                            text = "$answered",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    Text(
                        text = stringResource(Res.string.home_questions_seen, progress.totalQuestions),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = HonqSpacing.xs)
                    )
                }
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
                ) {
                    AnimatedContent(
                        targetState = (progress.practiceAccuracy * 100).toInt(),
                        transitionSpec = {
                            fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                        }
                    ) { accuracy ->
                        Text(
                            text = "${accuracy}% accuracy",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (accuracy >= 75) colors.correct else if (accuracy >= 50) colors.primary else colors.textMuted
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        HonqProgressBar(progress = progress.completionProgress)
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)
    )
    val percentage = (animatedProgress * 100).toInt()
    val trackColor = colors.surfaceVariant
    val fillColor = colors.primary
    val textColor = colors.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val padding = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(padding, padding)

            // Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Fill
            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${percentage}%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// endregion

// region Pill Grid

private val PillGreen = Color(0xFF30D158)
private val PillAmber = Color(0xFFFFD60A)
private val PillPurple = Color(0xFF5E5CE6)
private val PillOrange = Color(0xFFFF9F0A)

@Composable
private fun PillGrid(
    state: HomeState,
    isPremium: Boolean,
    freeTestsRemaining: Int,
    onNavigateToPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit,
    onNavigateToSmartPractice: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val favoritesCount = state.favoriteQuestions.size
    val mockTestSubtitle = if (!isPremium && freeTestsRemaining > 0) {
        "$freeTestsRemaining free left"
    } else {
        "Timed exam"
    }

    Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
        ) {
            ActionPill(
                icon = Icons.Rounded.PlayArrow,
                tint = PillGreen,
                title = "Practice",
                subtitle = if (isPremium) "By category" else "Random",
                enabled = state.isReady,
                onClick = onNavigateToPractice,
                modifier = Modifier.weight(1f)
            )
            ActionPill(
                icon = Icons.AutoMirrored.Rounded.Assignment,
                tint = PillAmber,
                title = "Mock Test",
                subtitle = mockTestSubtitle,
                enabled = state.isReady,
                onClick = onNavigateToMockTest,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
        ) {
            ActionPill(
                icon = Icons.Rounded.Psychology,
                tint = PillPurple,
                title = "Smart",
                subtitle = "Weak spots",
                enabled = state.isReady,
                onClick = onNavigateToSmartPractice,
                showProBadge = !isPremium,
                modifier = Modifier.weight(1f)
            )
            ActionPill(
                icon = Icons.Rounded.Bookmark,
                tint = PillOrange,
                title = "Favorites",
                subtitle = "$favoritesCount saved",
                enabled = state.isReady,
                onClick = onNavigateToFavorites,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProBadge: Boolean = false
) {
    val colors = HonqTheme.colors
    val pillShape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)
    val contentAlpha = if (enabled) 1f else 0.5f

    Row(
        modifier = modifier
            .alpha(contentAlpha)
            .clip(pillShape)
            .background(colors.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = HonqSizing.pillPaddingHorizontal, vertical = HonqSizing.pillPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        IconBadge(icon = icon, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (showProBadge) {
                    ProBadge()
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(HonqSizing.iconBadgeSize)
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusXSmall))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(HonqSizing.iconSizeSmall)
        )
    }
}

// endregion

// region Compact Stats Row

@Composable
private fun CompactStatsRow(
    state: HomeState,
    onNavigateToStatistics: () -> Unit
) {
    val colors = HonqTheme.colors
    val progress = state.progress
    val accuracyPercent = (progress.practiceAccuracy * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        CompactStatCard(
            icon = Icons.Rounded.CheckCircle,
            tint = colors.correct,
            value = "${accuracyPercent}%",
            label = stringResource(Res.string.home_accuracy),
            onClick = onNavigateToStatistics,
            modifier = Modifier.weight(1f)
        )
        CompactStatCard(
            icon = Icons.Rounded.EmojiEvents,
            tint = colors.correct,
            value = "${progress.mockTestsPassed}",
            label = stringResource(Res.string.home_mock_tests),
            onClick = onNavigateToStatistics,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompactStatCard(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors
    val cardShape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)

    Row(
        modifier = modifier
            .clip(cardShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = HonqSizing.pillPaddingHorizontal, vertical = HonqSizing.pillPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(HonqSizing.iconBadgeSize)
                .clip(RoundedCornerShape(HonqSizing.cornerRadiusXSmall))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(HonqSizing.iconSize20)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted
            )
        }
    }
}

// endregion

// region Configuration Card

@Composable
private fun ConfigurationCard(
    state: HomeState,
    onSelectState: (String) -> Unit,
    onSelectLicenseType: (String) -> Unit,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors
    val selectedState = state.states.firstOrNull { it.id == state.selectedStateId }
    val selectedType = state.licenseTypes.firstOrNull { it.id == state.selectedLicenseTypeId }
    var isExpanded by remember { mutableStateOf(false) }
    val selectText = stringResource(Res.string.select)

    HonqCard(
        onClick = { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedState?.shortName ?: selectText,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "\u2022",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = selectedType?.name ?: selectText,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = if (isExpanded) stringResource(Res.string.done) else stringResource(Res.string.change),
                color = colors.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(HonqMotion.tweenMedium()) + expandVertically(HonqMotion.tweenMedium()),
            exit = fadeOut(HonqMotion.tweenShort()) + shrinkVertically(HonqMotion.tweenMedium())
        ) {
            Column {
                Spacer(modifier = Modifier.height(HonqSpacing.md))

                Text(
                    text = stringResource(Res.string.home_state),
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    state.states.forEach { stateOption ->
                        SelectableChip(
                            text = stateOption.shortName,
                            selected = stateOption.id == state.selectedStateId,
                            enabled = stateOption.isActive,
                            onClick = { onSelectState(stateOption.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(HonqSpacing.md))

                Text(
                    text = stringResource(Res.string.home_license_type),
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    state.licenseTypes.forEach { type ->
                        val hasQuestionSet = state.questionSets.any { it.licenseTypeId == type.id }
                        val isSelected = type.id == state.selectedLicenseTypeId
                        val tint = if (!hasQuestionSet) colors.textMuted
                        else if (isSelected) colors.primary
                        else colors.textSecondary
                        SelectableChip(
                            text = type.name,
                            selected = isSelected,
                            enabled = hasQuestionSet,
                            onClick = { onSelectLicenseType(type.id) },
                            icon = {
                                LicenseTypeIcon(
                                    typeId = LicenseTypeId.fromId(type.id),
                                    tint = tint,
                                    modifier = Modifier.size(HonqSizing.iconSize16)
                                )
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.selectedQuestionSet == null && !state.isSyncing,
                    enter = fadeIn(HonqMotion.tweenMedium()) + expandVertically(HonqMotion.tweenMedium()),
                    exit = fadeOut(HonqMotion.tweenShort()) + shrinkVertically(HonqMotion.tweenShort())
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(HonqSpacing.md))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                .padding(HonqSpacing.md),
                            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(HonqSizing.iconSize16)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
                                Text(
                                    text = "No in-app questions for this selection yet",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                val practiceUrl = selectedState?.externalPracticeUrl
                                if (!practiceUrl.isNullOrBlank()) {
                                    Text(
                                        text = "Try the official practice test instead",
                                        color = colors.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            onOpenExternalLink("practice_test", practiceUrl)
                                            openUrl(practiceUrl)
                                        }
                                    )
                                } else {
                                    Text(
                                        text = "Check the official resources for your state",
                                        color = colors.textMuted,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region Selectable Chip

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    val colors = HonqTheme.colors
    val backgroundColor = if (selected) colors.primarySurface else colors.surfaceVariant
    val borderColor = if (selected) colors.primary else colors.border
    val textColor =
        if (!enabled) colors.textMuted else if (selected) colors.primary else colors.textSecondary
    val contentAlpha = if (enabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .alpha(contentAlpha)
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)
            )
            .clickable(enabled = enabled && !selected, onClick = onClick)
            .padding(horizontal = HonqSpacing.sm, vertical = HonqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
    ) {
        if (icon != null) {
            icon()
        }
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

// endregion

// region Sync Indicator

@Composable
private fun SyncIndicator(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    AnimatedVisibility(
        visible = isSyncing,
        modifier = modifier,
        enter = fadeIn(HonqMotion.tweenMedium()),
        exit = fadeOut(HonqMotion.tweenMedium())
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = HonqSpacing.sm, horizontal = HonqSpacing.md)
                .shadow(2.dp, RoundedCornerShape(HonqSizing.cornerRadius))
                .background(
                    color = colors.primarySurface,
                    shape = RoundedCornerShape(HonqSizing.cornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = colors.primary,
                    shape = RoundedCornerShape(HonqSizing.cornerRadius)
                )
                .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.xs),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(HonqSizing.iconSizeXSmall),
                color = colors.primary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.padding(horizontal = HonqSpacing.xs))
            Text(
                text = stringResource(Res.string.syncing),
                color = colors.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// endregion

// region External / Official Resources

@Composable
private fun ExternalResourcesCard(
    state: com.merkost.honq.domain.model.State,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard {
        Text(
            text = stringResource(Res.string.home_state_official_resources, state.shortName),
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Text(
            text = stringResource(Res.string.home_state_provides_practice_test, state.name),
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(HonqSpacing.lg))

        state.externalPracticeUrl?.let { practiceUrl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                    .background(colors.primarySurface)
                    .clickable {
                        onOpenExternalLink("practice_test", practiceUrl)
                        openUrl(practiceUrl)
                    }
                    .padding(HonqSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(HonqSizing.iconSize20)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_take_practice_test),
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = extractDomain(practiceUrl),
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        state.handbookUrl?.let { handbookUrl ->
            Spacer(modifier = Modifier.height(HonqSpacing.sm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                    .background(colors.surfaceVariant)
                    .clickable {
                        onOpenExternalLink("handbook", handbookUrl)
                        openUrl(handbookUrl)
                    }
                    .padding(HonqSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(HonqSizing.iconSize20)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_official_handbook),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = extractDomain(handbookUrl),
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun extractDomain(url: String): String {
    return url
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .substringBefore("/")
}

@Composable
private fun OfficialResourcesCard(
    resources: List<StateResource>,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    HonqCard(
        onClick = { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.home_official_resources),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
            ) {
                Text(
                    text = stringResource(Res.string.home_links_count, resources.size),
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Rounded.KeyboardArrowUp
                    else
                        Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(Res.string.collapse) else stringResource(
                        Res.string.expand
                    ),
                    tint = colors.textMuted,
                    modifier = Modifier.size(HonqSizing.iconSize20)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(HonqMotion.tweenMedium()) + expandVertically(HonqMotion.tweenMedium()),
            exit = fadeOut(HonqMotion.tweenShort()) + shrinkVertically(HonqMotion.tweenMedium())
        ) {
            Column(
                modifier = Modifier.padding(top = HonqSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                resources.forEach { resource ->
                    ResourceItem(
                        resource = resource,
                        onOpenExternalLink = onOpenExternalLink
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceItem(
    resource: StateResource,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors

    val (icon, backgroundColor, iconTint) = when (resource.resourceType) {
        ResourceType.PRACTICE_TEST -> Triple(
            Icons.AutoMirrored.Rounded.OpenInNew,
            colors.primarySurface,
            colors.primary
        )

        ResourceType.PDF -> Triple(
            Icons.Rounded.PictureAsPdf,
            colors.surfaceVariant,
            colors.incorrect
        )

        ResourceType.HANDBOOK -> Triple(
            Icons.AutoMirrored.Rounded.MenuBook,
            colors.surfaceVariant,
            colors.textSecondary
        )

        ResourceType.OTHER -> Triple(
            Icons.AutoMirrored.Rounded.OpenInNew,
            colors.surfaceVariant,
            colors.textSecondary
        )
    }

    val linkType = resource.resourceType.name.lowercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(backgroundColor)
            .clickable {
                onOpenExternalLink(linkType, resource.url)
                openUrl(resource.url)
            }
            .padding(HonqSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(HonqSizing.iconSize20)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resource.title,
                color = colors.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = extractDomain(resource.url),
                color = colors.textMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = stringResource(Res.string.open),
            tint = colors.textMuted,
            modifier = Modifier.size(HonqSizing.iconSize16)
        )
    }
}

// endregion

// region Previews

// endregion
