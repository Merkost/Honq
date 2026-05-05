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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.ProBadge
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

/**
 * Unified list-card for the home's secondary practice modes. One surface with hairline
 * dividers — like an iOS grouped-table section. Replaces the cramped 3-up tile grid.
 */
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
