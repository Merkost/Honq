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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.BuildKonfig
import com.merkost.honq.domain.repository.ProgressRepository
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.util.openAppStore
import com.merkost.honq.presentation.util.openUrl
import com.merkost.honq.presentation.util.sendEmail
import com.merkost.honq.presentation.util.shareText
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_honq_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private const val CONTACT_EMAIL = "merkostdev+honq@gmail.com"
private const val SHARE_TEXT = "Check out Honq - the best way to prepare for your Australian driver's license test!"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val colors = HonqTheme.colors
    val progressRepository: ProgressRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

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

            // Hero section with app identity
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(HonqSpacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    Text(
                        text = "Made with care in Sydney",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "\uD83C\uDDE6\uD83C\uDDFA",
                        fontSize = 16.sp
                    )
                }
            }

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

            // Resources section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HonqSizing.screenPadding)
            ) {
                Text(
                    text = "OFFICIAL RESOURCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = HonqSpacing.sm, bottom = HonqSpacing.sm)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HonqSizing.cornerRadius))
                        .background(colors.surface)
                ) {
                    ResourceLink(
                        state = "NSW",
                        title = "Road Users Handbook",
                        onClick = {
                            openUrl("https://www.nsw.gov.au/driving-boating-and-transport/roads-safety-and-rules/road-users-handbook")
                        }
                    )
                    Divider()
                    ResourceLink(
                        state = "VIC",
                        title = "Road to Solo Driving",
                        onClick = {
                            openUrl("https://www.vicroads.vic.gov.au/licences/your-licence/getting-your-licence/road-to-solo-driving-handbook")
                        }
                    )
                    Divider()
                    ResourceLink(
                        state = "QLD",
                        title = "Your Keys to Driving",
                        onClick = {
                            openUrl("https://www.qld.gov.au/transport/licensing/getting/handbook")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            // Reset progress - subtle destructive action
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(HonqSpacing.sm))
                    Text(
                        text = "Reset All Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.incorrect.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            // Version footer
            Text(
                text = "Version ${BuildKonfig.APP_VERSION}",
                fontSize = 12.sp,
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
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun ResourceLink(
    state: String,
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
        // State badge with fixed width for alignment
        Box(
            modifier = Modifier
                .width(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.primarySurface)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }

        Spacer(modifier = Modifier.width(HonqSpacing.md))

        Text(
            text = title,
            fontSize = 14.sp,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)
            .height(0.5.dp)
            .background(HonqTheme.colors.border.copy(alpha = 0.5f))
    )
}
