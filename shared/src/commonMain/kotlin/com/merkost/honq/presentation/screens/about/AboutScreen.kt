package com.merkost.honq.presentation.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.data.local.FontScale
import com.merkost.honq.data.local.ThemeMode
import com.merkost.honq.data.local.ThemePreferences
import com.merkost.honq.domain.premium.PremiumManager
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.presentation.components.base.AnimatedSegmentedSelector
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqTheme
import androidx.compose.ui.tooling.preview.Preview
import com.merkost.honq.presentation.util.openAppStore
import com.merkost.honq.presentation.util.openUrl
import com.merkost.honq.presentation.util.sendEmail
import com.merkost.honq.presentation.util.appVersion
import com.merkost.honq.presentation.util.shareText
import com.merkost.honq.presentation.util.storeUrl
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_honq_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private const val CONTACT_EMAIL = "merkostdev+honq@gmail.com"
private val SHARE_TEXT
    get() = "Check out Honq - the best way to prepare for your Australian driver's license test!\n$storeUrl"
private const val PRIVACY_URL = "https://merkost.github.io/Honq/privacy.html"
private const val TERMS_URL = "https://merkost.github.io/Honq/terms.html"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val colors = HonqTheme.colors
    val progressRepository: ProgressRepository = koinInject()
    val themePreferences: ThemePreferences = koinInject()
    val premiumManager: PremiumManager = koinInject()
    val analytics: Analytics = koinInject()
    val themeMode by themePreferences.themeMode.collectAsState()
    val fontScale by themePreferences.fontScale.collectAsState()
    val isPremium by premiumManager.isPremium.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var showCustomerCenter by remember { mutableStateOf(false) }

    HonqScaffold(
        title = "About",
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = HonqSizing.screenPadding)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_honq_logo),
                    contentDescription = "Honq Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(HonqSpacing.lg))

                Text(
                    text = "Honq",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )

            }

            Spacer(modifier = Modifier.height(HonqSpacing.md))

            Text(
                text = "Prepare for your Australian driving test.\nNo ads, ever.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = HonqSizing.screenPadding)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            // Action pills - horizontal row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                ActionPill(
                    icon = Icons.Rounded.Star,
                    label = "Rate",
                    onClick = { openAppStore() },
                    modifier = Modifier.weight(1f)
                )
                ActionPill(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    onClick = { shareText(SHARE_TEXT, "Share Honq") },
                    modifier = Modifier.weight(1f)
                )
                ActionPill(
                    icon = Icons.Rounded.Email,
                    label = "Contact",
                    onClick = { sendEmail(CONTACT_EMAIL, "Honq Feedback") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding)
                    .clip(RoundedCornerShape(HonqSizing.cornerRadius))
                    .background(colors.surface)
                    .padding(HonqSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
                ThemeModeSelector(
                    selected = themeMode,
                    onSelect = { themePreferences.setThemeMode(it) }
                )

                Spacer(modifier = Modifier.height(HonqSpacing.md))

                Text(
                    text = "Font Size",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
                FontScaleSelector(
                    selected = fontScale,
                    onSelect = { themePreferences.setFontScale(it) }
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            HonqProSection(
                isPremium = isPremium,
                isRestoring = isRestoring,
                restoreMessage = restoreMessage,
                onRestorePurchase = {
                    isRestoring = true
                    restoreMessage = null
                    analytics.track(AnalyticsEvent.RestoreStarted("about"))
                    coroutineScope.launch {
                        premiumManager.restorePurchase()
                            .onSuccess { restored ->
                                isRestoring = false
                                restoreMessage = if (restored) "Purchase restored!" else "No previous purchase found"
                                analytics.track(AnalyticsEvent.RestoreCompleted(restored, "about"))
                            }
                            .onFailure { e ->
                                isRestoring = false
                                restoreMessage = e.message ?: "Restore failed"
                                analytics.track(
                                    AnalyticsEvent.RestoreFailed(e.message ?: "unknown", "about")
                                )
                            }
                    }
                },
                onManageSubscription = { showCustomerCenter = true }
            )

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding)
                    .clip(RoundedCornerShape(HonqSizing.cornerRadius))
                    .background(colors.surface)
            ) {
                LegalLink(
                    title = "Privacy Policy",
                    onClick = { openUrl(PRIVACY_URL) }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = HonqSpacing.md)
                        .height(0.5.dp)
                        .background(colors.border.copy(alpha = 0.5f))
                )
                LegalLink(
                    title = "Terms of Service",
                    onClick = { openUrl(TERMS_URL) }
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Text(
                text = "This app is not affiliated with any Australian state or territory transport authority. " +
                        "Content is for practice purposes only. Always refer to your state's official handbook.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding)
                    .clip(RoundedCornerShape(HonqSizing.cornerRadius))
                    .background(colors.surface)
                    .padding(HonqSpacing.md)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding)
                    .clip(RoundedCornerShape(HonqSizing.cornerRadius))
                    .background(colors.incorrectSurface.copy(alpha = 0.3f))
                    .clickable { showResetDialog = true }
                    .padding(HonqSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = null,
                        tint = colors.incorrect.copy(alpha = 0.8f),
                        modifier = Modifier.size(HonqSizing.iconSize18)
                    )
                    Spacer(modifier = Modifier.width(HonqSpacing.sm))
                    Text(
                        text = "Reset All Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.incorrect.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
            ) {
                Text(
                    text = "Made with care in Sydney",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted.copy(alpha = 0.6f)
                )
                Text(
                    text = "\uD83C\uDDE6\uD83C\uDDFA",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xs))

            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = {
                Text(
                    text = "Reset Progress?",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("This will clear all your practice history and mock test results. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            progressRepository.clearAllProgress()
                            showResetDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Reset",
                        color = colors.incorrect,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textSecondary
                    )
                }
            }
        )
    }

    if (showCustomerCenter) {
        CustomerCenter(
            onDismiss = { showCustomerCenter = false }
        )
    }
}

@Composable
private fun LegalLink(
    title: String,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm + 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(HonqSizing.iconSize18)
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    AnimatedSegmentedSelector(
        options = ThemeMode.entries,
        selected = selected,
        onSelect = onSelect,
        labelOf = { mode ->
            when (mode) {
                ThemeMode.SYSTEM -> "System"
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
            }
        },
        labelStyle = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun FontScaleSelector(
    selected: FontScale,
    onSelect: (FontScale) -> Unit
) {
    AnimatedSegmentedSelector(
        options = FontScale.entries,
        selected = selected,
        onSelect = onSelect,
        labelOf = { it.label },
        labelStyle = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(vertical = HonqSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.primary,
            modifier = Modifier.size(HonqSizing.iconSizeMedium)
        )
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun HonqProSection(
    isPremium: Boolean,
    isRestoring: Boolean,
    restoreMessage: String?,
    onRestorePurchase: () -> Unit,
    onManageSubscription: () -> Unit
) {
    val colors = HonqTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HonqSizing.screenPadding)
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm + 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(HonqSizing.iconSizeSmall)
            )
            Spacer(modifier = Modifier.width(HonqSpacing.sm))
            Text(
                text = "Honq Pro",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isPremium) "Active" else "Free",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isPremium) colors.correct else colors.textMuted
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HonqSpacing.md)
                .height(0.5.dp)
                .background(colors.border.copy(alpha = 0.5f))
        )

        if (!isPremium) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isRestoring, onClick = onRestorePurchase)
                    .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm + 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(HonqSizing.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(HonqSpacing.sm))
                Text(
                    text = if (isRestoring) "Restoring..." else "Restore Purchase",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(HonqSizing.iconSize18)
                )
            }

            if (restoreMessage != null) {
                Text(
                    text = restoreMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.xs)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onManageSubscription)
                    .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm + 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(HonqSizing.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(HonqSpacing.sm))
                Text(
                    text = "Manage Purchase",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(HonqSizing.iconSize18)
                )
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun HonqProSectionFreePreview() {
    HonqPreviewTheme {
        HonqProSection(
            isPremium = false,
            isRestoring = false,
            restoreMessage = null,
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

@Preview
@Composable
private fun HonqProSectionRestoringPreview() {
    HonqPreviewTheme {
        HonqProSection(
            isPremium = false,
            isRestoring = true,
            restoreMessage = null,
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

@Preview
@Composable
private fun HonqProSectionRestoreFailedPreview() {
    HonqPreviewTheme {
        HonqProSection(
            isPremium = false,
            isRestoring = false,
            restoreMessage = "No previous purchase found",
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

@Preview
@Composable
private fun HonqProSectionPremiumPreview() {
    HonqPreviewTheme {
        HonqProSection(
            isPremium = true,
            isRestoring = false,
            restoreMessage = null,
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

@Preview
@Composable
private fun HonqProSectionFreePreviewLight() {
    HonqPreviewTheme(darkTheme = false) {
        HonqProSection(
            isPremium = false,
            isRestoring = false,
            restoreMessage = null,
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

@Preview
@Composable
private fun HonqProSectionPremiumPreviewLight() {
    HonqPreviewTheme(darkTheme = false) {
        HonqProSection(
            isPremium = true,
            isRestoring = false,
            restoreMessage = null,
            onRestorePurchase = {},
            onManageSubscription = {}
        )
    }
}

// endregion

