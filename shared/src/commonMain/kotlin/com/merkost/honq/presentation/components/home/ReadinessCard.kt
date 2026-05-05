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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.presentation.screens.home.Readiness
import com.merkost.honq.presentation.screens.home.ReadinessZone
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.home_readiness_caption
import honq.shared.generated.resources.home_readiness_passed
import honq.shared.generated.resources.home_readiness_points_to_pass
import honq.shared.generated.resources.home_readiness_status_keep_going
import honq.shared.generated.resources.home_readiness_status_on_track
import honq.shared.generated.resources.home_readiness_status_ready
import honq.shared.generated.resources.home_readiness_title
import org.jetbrains.compose.resources.stringResource

/**
 * Hero card with a horizontal layout — the score number occupies the left column as the
 * headline, the speedometer arc sits on the right as a compact instrument. Cuts the card
 * height roughly in half compared to the stacked layout while keeping the speedometer
 * metaphor recognizable.
 */
@Composable
fun ReadinessCard(
    progress: UserProgress,
    passMark: Int,
    stateCode: String?,
    licenseTypeId: LicenseTypeId?,
    licenseCode: String,
    onContextClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = HonqTheme.colors
    val score = Readiness.score(progress)
    val zone = Readiness.zone(score, passMark)
    val pointsToPass = Readiness.pointsToPass(score, passMark)
    val accuracyPercent = (progress.practiceAccuracy * 100f).toInt()

    val cardGradient = Brush.radialGradient(
        colors = listOf(
            colors.primary.copy(alpha = 0.10f),
            colors.surface
        ),
        radius = HERO_GRADIENT_RADIUS
    )
    val numberBrush = Brush.verticalGradient(
        colors = listOf(
            colors.textPrimary,
            colors.textPrimary.copy(alpha = 0.55f)
        )
    )
    val shape = RoundedCornerShape(HonqSizing.cornerRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = cardGradient)
            .border(1.dp, colors.primary.copy(alpha = 0.12f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(HonqSizing.cardPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (stateCode != null) {
                ReadinessContextChip(
                    stateCode = stateCode,
                    licenseTypeId = licenseTypeId,
                    licenseCode = licenseCode,
                    onClick = onContextClick
                )
            } else {
                Text(
                    text = stringResource(Res.string.home_readiness_title),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = colors.textMuted
                )
            }
            StatusPill(zone = zone)
        }

        Spacer(Modifier.height(HonqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
        ) {
            // Score readout — flexes to fill remaining width left of the gauge.
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = score.toString(),
                    style = TextStyle(
                        brush = numberBrush,
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp,
                        letterSpacing = (-2.5).sp,
                        lineHeight = 64.sp
                    ),
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = colors.textMuted,
                    modifier = Modifier.alignByBaseline()
                )
            }

            ReadinessGauge(
                score = score,
                passMark = passMark,
                modifier = Modifier.width(GAUGE_WIDTH)
            )
        }

        Spacer(Modifier.height(HonqSpacing.md))

        Text(
            text = if (pointsToPass == 0) stringResource(Res.string.home_readiness_passed)
            else stringResource(Res.string.home_readiness_points_to_pass, pointsToPass),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = colors.textPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = stringResource(
                Res.string.home_readiness_caption,
                progress.uniqueQuestionsAnswered,
                accuracyPercent
            ),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private const val HERO_GRADIENT_RADIUS = 1200f
private val GAUGE_WIDTH = 132.dp

@Composable
private fun ReadinessContextChip(
    stateCode: String,
    licenseTypeId: LicenseTypeId?,
    licenseCode: String,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors
    Row(
        // Rectangle clip on the *click ripple* only — the previous CircleShape pill clip
        // was cropping the plate's corners, which is the "circle shape clipping" issue.
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LicensePlateChip(
            stateCode = stateCode,
            licenseTypeId = licenseTypeId,
            licenseCode = licenseCode
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StatusPill(zone: ReadinessZone) {
    val colors = HonqTheme.colors
    val (bg, fg, label) = when (zone) {
        ReadinessZone.Green -> Triple(
            colors.correctSurface,
            colors.correct,
            stringResource(Res.string.home_readiness_status_ready)
        )
        ReadinessZone.Amber -> Triple(
            colors.primarySurface,
            colors.primary,
            stringResource(Res.string.home_readiness_status_on_track)
        )
        ReadinessZone.Red -> Triple(
            colors.incorrectSurface,
            colors.incorrect,
            stringResource(Res.string.home_readiness_status_keep_going)
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 10.5.sp
            ),
            color = fg
        )
    }
}
