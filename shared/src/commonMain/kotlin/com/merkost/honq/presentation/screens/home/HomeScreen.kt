package com.merkost.honq.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.StateResource
import com.merkost.honq.domain.premium.PremiumManager
import com.merkost.honq.presentation.components.base.FullscreenError
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.home.LicensePlateChip
import com.merkost.honq.presentation.components.home.ModeList
import com.merkost.honq.presentation.components.home.ModeListEntry
import com.merkost.honq.presentation.components.home.PrimaryPracticeCta
import com.merkost.honq.presentation.components.home.ReadinessCard
import com.merkost.honq.presentation.components.home.SetupSyncFeedback
import com.merkost.honq.presentation.components.home.StateLicenseSheet
import com.merkost.honq.core.REVENUECAT_ENTITLEMENT_ID
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.presentation.screens.paywall.PurchaseSuccessScreen
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
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
private const val DEFAULT_PASS_PERCENTAGE = 80

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
    val analytics: Analytics = koinInject()
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
    var paywallTrigger by remember { mutableStateOf("unknown") }
    var restoreInProgress by remember { mutableStateOf(false) }
    var conversionHandled by remember { mutableStateOf(false) }

    fun openPaywall(trigger: String, navigate: () -> Unit) {
        pendingNavigation = navigate
        paywallTrigger = trigger
        restoreInProgress = false
        conversionHandled = false
        showPaywall = true
        analytics.track(AnalyticsEvent.PaywallShown(trigger))
    }

    fun gatedNavigation(isPro: Boolean, trigger: String, navigate: () -> Unit) {
        if (isPro) {
            navigate()
        } else {
            openPaywall(trigger, navigate)
        }
    }

    fun handleConversion(event: AnalyticsEvent) {
        if (conversionHandled) return
        conversionHandled = true
        analytics.track(event)
        showPaywall = false
        showPurchaseSuccess = true
    }

    LaunchedEffect(isPremium, showPaywall) {
        if (isPremium && showPaywall) {
            if (restoreInProgress) {
                restoreInProgress = false
                handleConversion(AnalyticsEvent.RestoreCompleted(restored = true, source = "paywall"))
            } else {
                handleConversion(AnalyticsEvent.PurchaseCompleted("", paywallTrigger))
            }
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
            gatedNavigation(isPremium, "smart_drill", onNavigateToSmartPractice)
        },
        onNavigateToMockTest = {
            if (isPremium || freeTestsRemaining > 0) {
                onNavigateToMockTest()
            } else {
                openPaywall("mock_test_limit", onNavigateToMockTest)
            }
        },
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToAbout = onNavigateToAbout,
        onSelectState = { stateId -> container.intent(HomeIntent.SelectState(stateId)) },
        onSelectLicenseType = { typeId -> container.intent(HomeIntent.SelectLicenseType(typeId)) },
        onRetry = { container.intent(HomeIntent.Retry) },
        onRetrySync = { container.requestRetrySync() },
        onOpenExternalLink = { linkType, url ->
            container.intent(HomeIntent.OpenExternalLink(linkType, url))
        }
    )

    if (showPaywall) {
        Paywall(
            options = PaywallOptions(
                dismissRequest = {
                    if (showPaywall) {
                        analytics.track(AnalyticsEvent.PaywallDismissed(paywallTrigger))
                        showPaywall = false
                        pendingNavigation = null
                    }
                }
            ) {
                shouldDisplayDismissButton = true
                listener = object : PaywallListener {
                    override fun onPurchaseStarted(rcPackage: Package) {
                        analytics.track(
                            AnalyticsEvent.PurchaseStarted(rcPackage.storeProduct.id, paywallTrigger)
                        )
                    }

                    override fun onPurchaseCompleted(
                        customerInfo: CustomerInfo,
                        storeTransaction: StoreTransaction
                    ) {
                        handleConversion(
                            AnalyticsEvent.PurchaseCompleted(
                                storeTransaction.productIds.firstOrNull().orEmpty(),
                                paywallTrigger
                            )
                        )
                    }

                    override fun onPurchaseCancelled() {
                        analytics.track(AnalyticsEvent.PurchaseCancelled(paywallTrigger))
                    }

                    override fun onPurchaseError(error: PurchasesError) {
                        analytics.track(AnalyticsEvent.PurchaseFailed(error.message, paywallTrigger))
                    }

                    override fun onRestoreStarted() {
                        restoreInProgress = true
                        analytics.track(AnalyticsEvent.RestoreStarted("paywall"))
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        restoreInProgress = false
                        val restored =
                            customerInfo.entitlements[REVENUECAT_ENTITLEMENT_ID]?.isActive == true
                        if (restored) {
                            handleConversion(AnalyticsEvent.RestoreCompleted(restored = true, source = "paywall"))
                        } else {
                            analytics.track(AnalyticsEvent.RestoreCompleted(restored = false, source = "paywall"))
                        }
                    }

                    override fun onRestoreError(error: PurchasesError) {
                        restoreInProgress = false
                        analytics.track(AnalyticsEvent.RestoreFailed(error.message, "paywall"))
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
    onRetrySync: () -> Unit,
    onOpenExternalLink: (linkType: String, url: String) -> Unit
) {
    val colors = HonqTheme.colors

    val selectedState = state.states.firstOrNull { it.id == state.selectedStateId }
    val selectedLicenseType = state.licenseTypes.firstOrNull { it.id == state.selectedLicenseTypeId }
    val isExternalOnly = selectedState?.isExternalOnly == true
    var showStateLicenseSheet by remember { mutableStateOf(false) }

    HonqScaffold(
        title = stringResource(Res.string.app_name),
        showLogo = true,
        centered = false,
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
                        4 + (if (state.stateResources.isNotEmpty()) 1 else 0)
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
                        if (state.syncError != null) {
                            SetupSyncFeedback(
                                onRetry = onRetrySync,
                                enabled = !state.isSyncing
                            )
                        }

                        if (selectedState != null) {
                            Box(modifier = Modifier.staggeredEntrance(0)) {
                                HomeContextChipRow(
                                    stateCode = selectedState.shortName,
                                    licenseTypeId = selectedLicenseType?.typeId,
                                    licenseCode = selectedLicenseType?.shortName.orEmpty(),
                                    licenseName = selectedLicenseType?.name.orEmpty(),
                                    onClick = { showStateLicenseSheet = true }
                                )
                            }
                        }

                        if (isExternalOnly) {
                            Box(modifier = Modifier.staggeredEntrance(1)) {
                                ExternalResourcesCard(
                                    state = selectedState,
                                    onOpenExternalLink = onOpenExternalLink
                                )
                            }
                        } else {
                            Box(modifier = Modifier.staggeredEntrance(1)) {
                                ReadinessCard(
                                    progress = state.progress,
                                    passMark = state.selectedQuestionSet
                                        ?.mockTestPassPercentage
                                        ?: DEFAULT_PASS_PERCENTAGE,
                                    onClick = onNavigateToStatistics
                                )
                            }
                            Box(modifier = Modifier.staggeredEntrance(2)) {
                                val hasProgress = state.progress.uniqueQuestionsAnswered > 0
                                PrimaryPracticeCta(
                                    eyebrow = if (hasProgress) "Continue" else "Start",
                                    title = if (hasProgress) "Practice questions" else "Start practising",
                                    subtitle = if (isPremium) "By category"
                                    else "Random selection",
                                    enabled = state.isReady,
                                    onClick = onNavigateToPractice
                                )
                            }
                            Box(modifier = Modifier.staggeredEntrance(3)) {
                                HomeModeList(
                                    state = state,
                                    isPremium = isPremium,
                                    freeTestsRemaining = freeTestsRemaining,
                                    onNavigateToMockTest = onNavigateToMockTest,
                                    onNavigateToSmartPractice = onNavigateToSmartPractice,
                                    onNavigateToFavorites = onNavigateToFavorites
                                )
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

    if (showStateLicenseSheet) {
        StateLicenseSheet(
            state = state,
            onSelectState = onSelectState,
            onSelectLicenseType = onSelectLicenseType,
            onOpenExternalLink = onOpenExternalLink,
            onDismiss = { showStateLicenseSheet = false }
        )
    }
}

@Composable
private fun HomeContextChipRow(
    stateCode: String,
    licenseTypeId: LicenseTypeId?,
    licenseCode: String,
    licenseName: String,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors
    val changeSetupLabel = stringResource(Res.string.home_change_setup)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                onClickLabel = changeSetupLabel,
                role = Role.Button,
                onClick = onClick
            )
            .sizeIn(minHeight = HonqSizing.minTapTarget)
            .padding(vertical = HonqSpacing.xs, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        LicensePlateChip(
            stateCode = stateCode,
            licenseTypeId = licenseTypeId,
            licenseCode = licenseCode
        )
        Text(
            text = licenseName,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = changeSetupLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = colors.textMuted,
            maxLines = 1,
            softWrap = false
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(HonqSizing.iconSize16)
        )
    }
}

@Composable
private fun HomeModeList(
    state: HomeState,
    isPremium: Boolean,
    freeTestsRemaining: Int,
    onNavigateToMockTest: () -> Unit,
    onNavigateToSmartPractice: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val colors = HonqTheme.colors
    val favoritesCount = state.favoriteQuestions.size
    val mockTrailingBadge = if (!isPremium && freeTestsRemaining > 0) {
        "$freeTestsRemaining FREE"
    } else null

    val rows = listOf(
        ModeListEntry(
            icon = Icons.AutoMirrored.Rounded.Assignment,
            tint = colors.primary,
            title = "Mock test",
            subtitle = "Timed exam · 25 questions",
            enabled = state.isReady,
            onClick = onNavigateToMockTest,
            trailingBadgeText = mockTrailingBadge,
            trailingBadgeColor = colors.warning
        ),
        ModeListEntry(
            icon = Icons.Rounded.Psychology,
            tint = SmartDrillTint,
            title = "Smart drill",
            subtitle = "Adaptive — focuses your weak spots",
            enabled = state.isReady,
            onClick = onNavigateToSmartPractice,
            showProBadge = !isPremium
        ),
        ModeListEntry(
            icon = Icons.Rounded.Bookmark,
            tint = colors.warning,
            title = "Saved questions",
            subtitle = "$favoritesCount question${if (favoritesCount == 1) "" else "s"} bookmarked",
            enabled = state.isReady,
            onClick = onNavigateToFavorites
        )
    )

    ModeList(rows = rows)
}

private val SmartDrillTint = Color(0xFF5E5CE6)

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
