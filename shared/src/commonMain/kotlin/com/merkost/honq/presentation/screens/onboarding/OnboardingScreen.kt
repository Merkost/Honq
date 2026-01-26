package com.merkost.honq.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.domain.model.State
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqButton
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

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
        if (state.isLoading) {
            FullscreenLoading()
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

@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit
) {
    val colors = HonqTheme.colors

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
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(HonqSpacing.lg))

        Text(
            text = stringResource(Res.string.onboarding_app_name),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            fontSize = 18.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        HonqButton(
            text = stringResource(Res.string.onboarding_get_started),
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth()
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
            states.forEach { state ->
                StateSelectionCard(
                    state = state,
                    selected = state.id == selectedStateId,
                    onClick = { onSelectState(state.id) }
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
            licenseTypes.forEach { type ->
                LicenseTypeCard(
                    type = type,
                    selected = type.id == selectedTypeId,
                    onClick = { onSelectType(type.id) }
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
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(HonqSpacing.xs))

        Text(
            text = subtitle,
            fontSize = 16.sp,
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
    onClick: () -> Unit
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
        modifier = Modifier
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                Text(
                    text = state.name,
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
                if (state.isExternalOnly) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_resources_only),
                            fontSize = 10.sp,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                text = if (!enabled) stringResource(Res.string.onboarding_coming_soon, state.shortName) else state.shortName,
                fontSize = 14.sp,
                color = colors.textMuted
            )
        }

        AnimatedCheckmark(selected = selected)
    }
}

@Composable
private fun LicenseTypeCard(
    type: LicenseType,
    selected: Boolean,
    onClick: () -> Unit
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
        modifier = Modifier
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
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
            Text(
                text = getLicenseTypeDescription(type.typeId),
                fontSize = 14.sp,
                color = colors.textMuted
            )
        }

        AnimatedCheckmark(selected = selected)
    }
}

@Composable
private fun LicenseTypeIcon(
    typeId: LicenseTypeId?,
    tint: Color
) {
    val icon = when (typeId) {
        LicenseTypeId.CAR -> Icons.Rounded.DirectionsCar
        LicenseTypeId.RIDER -> Icons.Rounded.TwoWheeler
        null -> Icons.Rounded.DirectionsCar
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(32.dp)
    )
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
                    .size(24.dp)
                    .background(colors.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun getLicenseTypeDescription(typeId: LicenseTypeId?): String = when (typeId) {
    LicenseTypeId.CAR -> stringResource(Res.string.onboarding_car_license)
    LicenseTypeId.RIDER -> stringResource(Res.string.onboarding_rider_license)
    null -> ""
}
