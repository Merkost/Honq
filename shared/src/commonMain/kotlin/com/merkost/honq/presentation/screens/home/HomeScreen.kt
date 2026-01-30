package com.merkost.honq.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.LicenseTypeIcon
import com.merkost.honq.presentation.components.base.FullscreenError
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.StateResource
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.util.openUrl
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.dsl.intent

private const val STAGGER_DELAY = 60L
private const val SLIDE_UP_PX = 40f

@Composable
fun HomeScreen(
    onNavigateToPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val container = koinViewModel<HomeContainer>()
    val state by container.store.subscribe { action ->
        when (action) {
            HomeAction.NavigateToPractice -> onNavigateToPractice()
            HomeAction.NavigateToMockTest -> onNavigateToMockTest()
        }
    }

    HomeContent(
        state = state,
        onNavigateToPractice = onNavigateToPractice,
        onNavigateToMockTest = onNavigateToMockTest,
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
}

@Composable
private fun HomeContent(
    state: HomeState,
    onNavigateToPractice: () -> Unit,
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
        }
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
                    val selectedState = state.states.firstOrNull { it.id == state.selectedStateId }
                    val isExternalOnly = selectedState?.isExternalOnly == true

                    val itemCount = if (isExternalOnly) 2 else {
                        4 + (if (state.stateResources.isNotEmpty()) 1 else 0) + 1 // cards + bottom bar
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

                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
                        ) {
                            Box(modifier = Modifier.staggeredEntrance(0)) {
                                ConfigurationCard(state, onSelectState, onSelectLicenseType)
                            }

                            if (isExternalOnly && selectedState != null) {
                                Box(modifier = Modifier.staggeredEntrance(1)) {
                                    ExternalResourcesCard(
                                        state = selectedState,
                                        onOpenExternalLink = onOpenExternalLink
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.staggeredEntrance(1)) {
                                    QuestionBankCard(state)
                                }
                                Box(modifier = Modifier.staggeredEntrance(2)) {
                                    FavoritesCard(state, onNavigateToFavorites)
                                }
                                Box(modifier = Modifier.staggeredEntrance(3)) {
                                    StatsRow(state, onNavigateToStatistics)
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

                        if (!isExternalOnly) {
                            val bottomBarIndex = itemCount - 1
                            Box(modifier = Modifier.staggeredEntrance(bottomBarIndex)) {
                                BottomActionBarVertical {
                                    HonqButton(
                                        text = stringResource(Res.string.home_start_practice),
                                        onClick = onNavigateToPractice,
                                        enabled = state.isReady
                                    )
                                    HonqButton(
                                        text = stringResource(Res.string.home_take_mock_test),
                                        onClick = onNavigateToMockTest,
                                        variant = HonqButtonVariant.Secondary,
                                        enabled = state.isReady
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

@Composable
private fun ConfigurationCard(
    state: HomeState,
    onSelectState: (String) -> Unit,
    onSelectLicenseType: (String) -> Unit
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "•",
                    color = colors.textMuted,
                    fontSize = 16.sp
                )
                Text(
                    text = selectedType?.name ?: selectText,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = if (isExpanded) stringResource(Res.string.done) else stringResource(Res.string.change),
                color = colors.primary,
                fontSize = 12.sp,
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
                    fontSize = 12.sp
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
                    fontSize = 12.sp
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
                                    modifier = Modifier.size(16.dp)
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
                        Spacer(modifier = Modifier.height(HonqSpacing.sm))
                        Text(
                            text = stringResource(Res.string.home_no_questions_available),
                            color = colors.incorrect,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

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
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

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
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(
                    color = colors.primarySurface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = colors.primary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.xs),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = colors.primary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.padding(horizontal = HonqSpacing.xs))
            Text(
                text = stringResource(Res.string.syncing),
                color = colors.primary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun QuestionBankCard(state: HomeState) {
    val colors = HonqTheme.colors
    val progress = state.progress

    HonqCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.home_question_bank),
                color = colors.textSecondary,
                fontSize = 14.sp
            )
            AnimatedContent(
                targetState = progress.totalQuestions,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                }
            ) { total ->
                Text(
                    text = stringResource(Res.string.home_questions_count, total),
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
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
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            Text(
                text = stringResource(Res.string.home_questions_seen, progress.totalQuestions),
                fontSize = 16.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        HonqProgressBar(progress = progress.completionProgress)
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        Text(
            text = stringResource(Res.string.home_percent_complete, (progress.completionProgress * 100).toInt()),
            color = colors.textMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FavoritesCard(
    state: HomeState,
    onNavigateToFavorites: () -> Unit
) {
    val colors = HonqTheme.colors
    val favoritesCount = state.favoriteQuestions.size

    HonqCard(onClick = onNavigateToFavorites) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.home_favorites),
                color = colors.textSecondary,
                fontSize = 14.sp
            )
            Text(
                text = stringResource(Res.string.view_all),
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(HonqSpacing.sm))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
        ) {
            AnimatedContent(
                targetState = favoritesCount,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                }
            ) { count ->
                Text(
                    text = "$count",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            Text(
                text = if (favoritesCount == 1) stringResource(Res.string.home_saved_question) else stringResource(Res.string.home_saved_questions),
                fontSize = 16.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(
    state: HomeState,
    onNavigateToStatistics: () -> Unit
) {
    val colors = HonqTheme.colors
    val progress = state.progress

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        HonqCard(
            modifier = Modifier.weight(1f),
            onClick = onNavigateToStatistics
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.home_accuracy),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Stats",
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            AnimatedContent(
                targetState = (progress.practiceAccuracy * 100).toInt(),
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                }
            ) { percentage ->
                Text(
                    text = "$percentage%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (percentage >= 75) colors.correct else if (percentage >= 50) colors.primary else colors.incorrect
                )
            }
            Text(
                text = "${progress.correctAnswers}/${progress.totalPracticed}",
                color = colors.textMuted,
                fontSize = 11.sp
            )
        }

        HonqCard(
            modifier = Modifier.weight(1f),
            onClick = onNavigateToStatistics
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.home_mock_tests),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Stats",
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            AnimatedContent(
                targetState = progress.mockTestsPassed,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                }
            ) { passed ->
                Text(
                    text = "$passed",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.correct
                )
            }
            Text(
                text = stringResource(Res.string.home_tests_taken, progress.mockTestsTaken),
                color = colors.textMuted,
                fontSize = 11.sp
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
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Text(
            text = stringResource(Res.string.home_state_provides_practice_test, state.name),
            color = colors.textPrimary,
            fontSize = 14.sp
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
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_take_practice_test),
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = extractDomain(practiceUrl),
                        color = colors.textMuted,
                        fontSize = 11.sp
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
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_official_handbook),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = extractDomain(handbookUrl),
                        color = colors.textMuted,
                        fontSize = 11.sp
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
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
            ) {
                Text(
                    text = stringResource(Res.string.home_links_count, resources.size),
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Rounded.KeyboardArrowUp
                    else
                        Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(Res.string.collapse) else stringResource(Res.string.expand),
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
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
            Icons.Rounded.MenuBook,
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
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resource.title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = extractDomain(resource.url),
                color = colors.textMuted,
                fontSize = 11.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = stringResource(Res.string.open),
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
