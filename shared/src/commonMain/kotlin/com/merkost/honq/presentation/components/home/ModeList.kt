package com.merkost.honq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.ProBadge
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun ModeList(
    modifier: Modifier = Modifier,
    rows: List<ModeListEntry>
) {
    val colors = HonqTheme.colors
    val shape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.5f), shape)
    ) {
        rows.forEachIndexed { index, entry ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = HonqSizing.cardPadding + ICON_BADGE_SIZE + HonqSpacing.md),
                    thickness = 0.5.dp,
                    color = colors.border.copy(alpha = 0.6f)
                )
            }
            ModeListRow(entry = entry)
        }
    }
}

data class ModeListEntry(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val onClick: () -> Unit,
    val trailingBadgeText: String? = null,
    val trailingBadgeColor: Color? = null,
    val showProBadge: Boolean = false
)

@Composable
private fun ModeListRow(entry: ModeListEntry) {
    val colors = HonqTheme.colors
    val contentAlpha = if (entry.enabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .clickable(enabled = entry.enabled, onClick = entry.onClick)
            .padding(
                horizontal = HonqSizing.cardPadding,
                vertical = HonqSpacing.md
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(ICON_BADGE_SIZE)
                .clip(RoundedCornerShape(11.dp))
                .background(entry.tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = entry.tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = colors.textPrimary
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = entry.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.textMuted
            )
        }

        when {
            entry.trailingBadgeText != null -> {
                val badgeFg = entry.trailingBadgeColor ?: colors.primary
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(badgeFg.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entry.trailingBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = badgeFg
                    )
                }
            }
            entry.showProBadge -> ProBadge()
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier
                .size(20.dp)
                .alpha(0.5f)
        )
    }
}

private val ICON_BADGE_SIZE = 38.dp

private val SmartTint = Color(0xFF5E5CE6)

@Composable
private fun previewRows(isPremium: Boolean = false): List<ModeListEntry> {
    val colors = HonqTheme.colors
    return listOf(
        ModeListEntry(
            icon = Icons.AutoMirrored.Rounded.Assignment,
            tint = colors.primary,
            title = "Mock test",
            subtitle = "Timed exam · 25 questions",
            enabled = true,
            onClick = {},
            trailingBadgeText = if (!isPremium) "2 FREE" else null,
            trailingBadgeColor = colors.warning
        ),
        ModeListEntry(
            icon = Icons.Rounded.Psychology,
            tint = SmartTint,
            title = "Smart drill",
            subtitle = "Adaptive — focuses your weak spots",
            enabled = true,
            onClick = {},
            showProBadge = !isPremium
        ),
        ModeListEntry(
            icon = Icons.Rounded.Bookmark,
            tint = colors.warning,
            title = "Saved questions",
            subtitle = "12 questions bookmarked",
            enabled = true,
            onClick = {}
        )
    )
}

@Preview
@Composable
private fun ModeListFreePreview() {
    HonqPreviewTheme {
        ModeList(rows = previewRows(isPremium = false))
    }
}

@Preview
@Composable
private fun ModeListPremiumPreview() {
    HonqPreviewTheme {
        ModeList(rows = previewRows(isPremium = true))
    }
}

@Preview
@Composable
private fun ModeListFreeLightPreview() {
    HonqPreviewTheme(darkTheme = false) {
        ModeList(rows = previewRows(isPremium = false))
    }
}

@Preview
@Composable
private fun ModeListAllDisabledPreview() {
    val colors = HonqTheme.colors
    HonqPreviewTheme {
        ModeList(
            rows = listOf(
                ModeListEntry(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    tint = colors.primary,
                    title = "Mock test",
                    subtitle = "Loading content…",
                    enabled = false,
                    onClick = {}
                ),
                ModeListEntry(
                    icon = Icons.Rounded.Psychology,
                    tint = SmartTint,
                    title = "Smart drill",
                    subtitle = "Loading content…",
                    enabled = false,
                    onClick = {}
                ),
                ModeListEntry(
                    icon = Icons.Rounded.Bookmark,
                    tint = colors.warning,
                    title = "Saved questions",
                    subtitle = "Loading content…",
                    enabled = false,
                    onClick = {}
                )
            )
        )
    }
}

@Preview
@Composable
private fun ModeListNoFavoritesPreview() {
    val colors = HonqTheme.colors
    HonqPreviewTheme {
        ModeList(
            rows = listOf(
                ModeListEntry(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    tint = colors.primary,
                    title = "Mock test",
                    subtitle = "Timed exam · 25 questions",
                    enabled = true,
                    onClick = {}
                ),
                ModeListEntry(
                    icon = Icons.Rounded.Psychology,
                    tint = SmartTint,
                    title = "Smart drill",
                    subtitle = "Adaptive — focuses your weak spots",
                    enabled = true,
                    onClick = {},
                    showProBadge = true
                ),
                ModeListEntry(
                    icon = Icons.Rounded.Bookmark,
                    tint = colors.warning,
                    title = "Saved questions",
                    subtitle = "Bookmark questions to revisit them",
                    enabled = true,
                    onClick = {}
                )
            )
        )
    }
}

