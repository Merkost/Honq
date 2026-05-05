package com.merkost.honq.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.domain.model.State
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.LicenseTypeIcon
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_honq_logo
import honq.shared.generated.resources.onboarding_app_name
import honq.shared.generated.resources.onboarding_car_license
import honq.shared.generated.resources.onboarding_coming_soon
import honq.shared.generated.resources.onboarding_continue
import honq.shared.generated.resources.onboarding_get_started
import honq.shared.generated.resources.onboarding_resources_only
import honq.shared.generated.resources.onboarding_rider_license
import honq.shared.generated.resources.onboarding_select_license_subtitle
import honq.shared.generated.resources.onboarding_select_license_title
import honq.shared.generated.resources.onboarding_select_state_subtitle
import honq.shared.generated.resources.onboarding_select_state_title
import honq.shared.generated.resources.onboarding_start_learning
import honq.shared.generated.resources.onboarding_subtitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

private const val WELCOME_STAGGER_DELAY = 80L
private const val LIST_STAGGER_DELAY = 60L
private const val SLIDE_UP_PX = 40f

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<OnboardingContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            OnboardingAction.NavigateToHome -> onNavigateToHome()
        }
    }

    OnboardingContent(
        state = state,
        onIntent = container.store::intent
    )
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit
) {
    val colors = HonqTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = {
                fadeIn(tween(HonqMotion.durationMedium)).togetherWith(
                    fadeOut(tween(HonqMotion.durationMedium))
                )
            }
        ) { isLoading ->
            if (isLoading) {
                BrandedSplash()
            } else {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        val enterOffset = if (isForward) 1 else -1
                        val exitOffset = if (isForward) -1 else 1

                        (slideInHorizontally(
                            animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                            initialOffsetX = { fullWidth -> fullWidth * enterOffset }
                        ) + fadeIn(
                            animationSpec = tween(HonqMotion.durationMedium)
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                                targetOffsetX = { fullWidth -> fullWidth * exitOffset }
                            ) + fadeOut(
                                animationSpec = tween(HonqMotion.durationShort)
                            )
                        )
                    }
                ) { step ->
                    when (step) {
                        OnboardingStep.Welcome -> WelcomeStep(
                            onGetStarted = { onIntent(OnboardingIntent.GetStarted) }
                        )
                        OnboardingStep.StateSelection -> StateSelectionStep(
                            states = state.states,
                            selectedStateId = state.selectedStateId,
                            onSelectState = { onIntent(OnboardingIntent.SelectState(it)) },
                            onContinue = { onIntent(OnboardingIntent.ConfirmStateSelection) },
                            onBack = { onIntent(OnboardingIntent.GoBack) },
                            canContinue = state.canProceedFromStateSelection
                        )
                        OnboardingStep.LicenseTypeSelection -> LicenseTypeSelectionStep(
                            licenseTypes = state.licenseTypes,
                            selectedTypeId = state.selectedLicenseTypeId,
                            onSelectType = { onIntent(OnboardingIntent.SelectLicenseType(it)) },
                            onComplete = { onIntent(OnboardingIntent.CompleteOnboarding) },
                            onBack = { onIntent(OnboardingIntent.GoBack) },
                            canComplete = state.canProceedFromLicenseTypeSelection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandedSplash() {
    val colors = HonqTheme.colors
    val logoAnim = remember { Animatable(0f) }
    val titleAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAnim.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = HonqMotion.durationEnter,
                easing = HonqMotion.easingEmphasizedDecelerate
            )
        )
        titleAnim.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = HonqMotion.durationEnter,
                easing = HonqMotion.easingEmphasizedDecelerate
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_honq_logo),
                contentDescription = "Honq Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(logoAnim.value)
                    .scale(0.8f + 0.2f * logoAnim.value)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            Text(
                text = stringResource(Res.string.onboarding_app_name),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp),
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier
                    .alpha(titleAnim.value)
                    .offset { IntOffset(0, ((1f - titleAnim.value) * SLIDE_UP_PX).toInt()) }
            )
        }
    }
}

@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit
) {
    val colors = HonqTheme.colors
    val animProgress = remember { List(4) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        animProgress.forEachIndexed { index, anim ->
            launch {
                delay(index * WELCOME_STAGGER_DELAY)
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

    fun Modifier.staggered(index: Int): Modifier {
        val progress = animProgress.getOrNull(index)?.value ?: 1f
        return this
            .alpha(progress)
            .offset { IntOffset(0, ((1f - progress) * SLIDE_UP_PX).toInt()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HonqSizing.screenPadding)
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(Res.drawable.ic_honq_logo),
            contentDescription = "Honq Logo",
            modifier = Modifier
                .size(120.dp)
                .staggered(0)
        )

        Spacer(modifier = Modifier.height(HonqSpacing.lg))

        Text(
            text = stringResource(Res.string.onboarding_app_name),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp),
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            modifier = Modifier.staggered(1)
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.staggered(2)
        )

        Spacer(modifier = Modifier.weight(1f))

        HonqButton(
            text = stringResource(Res.string.onboarding_get_started),
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .staggered(3)
        )

        Spacer(modifier = Modifier.height(HonqSpacing.xl))
    }
}

@Composable
private fun StateSelectionStep(
    states: List<State>,
    selectedStateId: String?,
    onSelectState: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    canContinue: Boolean
) {
    val animProgress = remember(states.size) { List(states.size) { Animatable(0f) } }

    LaunchedEffect(states) {
        animProgress.forEachIndexed { index, anim ->
            launch {
                delay(index * LIST_STAGGER_DELAY)
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OnboardingHeader(
            title = stringResource(Res.string.onboarding_select_state_title),
            subtitle = stringResource(Res.string.onboarding_select_state_subtitle),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HonqSizing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
        ) {
            states.forEachIndexed { index, state ->
                val progress = animProgress.getOrNull(index)?.value ?: 1f
                StateSelectionCard(
                    state = state,
                    selected = state.id == selectedStateId,
                    onClick = { onSelectState(state.id) },
                    modifier = Modifier
                        .alpha(progress)
                        .offset { IntOffset(0, ((1f - progress) * SLIDE_UP_PX).toInt()) }
                )
            }
        }

        OnboardingFooter(
            buttonText = stringResource(Res.string.onboarding_continue),
            onClick = onContinue,
            enabled = canContinue
        )
    }
}

@Composable
private fun LicenseTypeSelectionStep(
    licenseTypes: List<LicenseType>,
    selectedTypeId: String?,
    onSelectType: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    canComplete: Boolean
) {
    val animProgress = remember(licenseTypes.size) { List(licenseTypes.size) { Animatable(0f) } }

    LaunchedEffect(licenseTypes) {
        animProgress.forEachIndexed { index, anim ->
            launch {
                delay(index * LIST_STAGGER_DELAY)
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OnboardingHeader(
            title = stringResource(Res.string.onboarding_select_license_title),
            subtitle = stringResource(Res.string.onboarding_select_license_subtitle),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HonqSizing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
        ) {
            licenseTypes.forEachIndexed { index, type ->
                val progress = animProgress.getOrNull(index)?.value ?: 1f
                LicenseTypeCard(
                    type = type,
                    selected = type.id == selectedTypeId,
                    onClick = { onSelectType(type.id) },
                    modifier = Modifier
                        .alpha(progress)
                        .offset { IntOffset(0, ((1f - progress) * SLIDE_UP_PX).toInt()) }
                )
            }
        }

        OnboardingFooter(
            buttonText = stringResource(Res.string.onboarding_start_learning),
            onClick = onComplete,
            enabled = canComplete
        )
    }
}

@Composable
private fun OnboardingHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    val colors = HonqTheme.colors

    Column(
        modifier = Modifier.padding(HonqSizing.screenPadding)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = HonqSpacing.md)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(HonqSpacing.xs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(HonqSpacing.lg))
    }
}

@Composable
private fun OnboardingFooter(
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .padding(HonqSizing.screenPadding)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HonqButton(
            text = buttonText,
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(HonqSpacing.md))
    }
}

@Composable
private fun StateSelectionCard(
    state: State,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors
    val enabled = state.isActive

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primarySurface else colors.surface,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.border,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textPrimary,
        animationSpec = tween(HonqMotion.durationShort)
    )

    val contentAlpha = if (enabled) 1f else 0.5f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(HonqSizing.cornerRadius)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(HonqSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                Text(
                    text = if (!enabled) stringResource(Res.string.onboarding_coming_soon, state.shortName) else state.shortName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                if (state.isExternalOnly) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(HonqSizing.progressBarHeightSmall))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = HonqSpacing.xs, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_resources_only),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        AnimatedCheckmark(selected = selected)
    }
}

@Composable
private fun LicenseTypeCard(
    type: LicenseType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primarySurface else colors.surface,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.border,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textPrimary,
        animationSpec = tween(HonqMotion.durationShort)
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textMuted,
        animationSpec = tween(HonqMotion.durationShort)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(HonqSizing.cornerRadius)
            )
            .clickable(onClick = onClick)
            .padding(HonqSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LicenseTypeIcon(
            typeId = type.typeId,
            tint = iconTint
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
            Text(
                text = getLicenseTypeDescription(type.typeId),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }

        AnimatedCheckmark(selected = selected)
    }
}

@Composable
private fun getLicenseTypeDescription(typeId: LicenseTypeId?): String = when (typeId) {
    LicenseTypeId.CAR -> stringResource(Res.string.onboarding_car_license)
    LicenseTypeId.RIDER,
    LicenseTypeId.RIDER_SPECIAL_MOBILITY_VEHICLE -> stringResource(Res.string.onboarding_rider_license)
    LicenseTypeId.LIGHT_RIGID,
    LicenseTypeId.MEDIUM_RIGID,
    LicenseTypeId.HEAVY_RIGID,
    LicenseTypeId.HEAVY_COMBINATION,
    LicenseTypeId.MULTI_COMBINATION -> ""
    null -> ""
}

@Composable
private fun AnimatedCheckmark(selected: Boolean) {
    val colors = HonqTheme.colors

    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            fadeIn(tween(HonqMotion.durationShort)).togetherWith(fadeOut(tween(HonqMotion.durationShort)))
        }
    ) { isSelected ->
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(HonqSizing.checkmarkSize)
                    .background(colors.primary, RoundedCornerShape(HonqSizing.cornerRadiusSmall)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = colors.onPrimary,
                    modifier = Modifier.size(HonqSizing.iconSize16)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(HonqSizing.checkmarkSize)
                    .border(1.dp, colors.border, RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            )
        }
    }
}

