package com.merkost.honq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.presentation.theme.HonqTheme

/**
 * Two paired badges: the original Australia-style dual-panel licence plate (blue state
 * code + white licence letter) and a separate amber tile with the vehicle silhouette.
 * Splitting the icon out of the plate keeps the plate looking authentic while making the
 * vehicle type visually obvious — the letter alone was unreadable at small sizes.
 *
 * Plate corner radius is small (4dp) so the curve never crosses the text bounds, which
 * was the source of the previous "clipping" issue.
 */
@Composable
fun LicensePlateChip(
    stateCode: String,
    licenseTypeId: LicenseTypeId?,
    licenseCode: String,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors
    val plateShape = RoundedCornerShape(PLATE_CORNER)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Blue + white plate — the look you liked, restored.
        Row(
            modifier = Modifier
                .height(PLATE_HEIGHT)
                .clip(plateShape)
                .background(Color.White)
                .border(width = 1.dp, color = LicensePlateInk, shape = plateShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(PLATE_HEIGHT)
                    .background(LicensePlateBlue)
                    .padding(horizontal = PLATE_PANEL_PADDING_H),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stateCode.uppercase(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                )
            }
            Box(
                modifier = Modifier.padding(horizontal = PLATE_PANEL_PADDING_H),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = licenseCode.uppercase(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = LicensePlateInk
                    )
                )
            }
        }

        // Vehicle silhouette — no container, just the icon. Lets the plate be the
        // visual "object" and the vehicle icon read as a clean accent.
        Icon(
            imageVector = vehicleIconFor(licenseTypeId),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(VEHICLE_ICON_SIZE)
        )
    }
}

private fun vehicleIconFor(typeId: LicenseTypeId?) = when (typeId) {
    LicenseTypeId.CAR -> Icons.Rounded.DirectionsCar
    LicenseTypeId.RIDER,
    LicenseTypeId.RIDER_SPECIAL_MOBILITY_VEHICLE -> Icons.Rounded.TwoWheeler
    LicenseTypeId.LIGHT_RIGID,
    LicenseTypeId.MEDIUM_RIGID,
    LicenseTypeId.HEAVY_RIGID,
    LicenseTypeId.HEAVY_COMBINATION,
    LicenseTypeId.MULTI_COMBINATION -> Icons.Rounded.LocalShipping
    null -> Icons.Rounded.DirectionsCar
}

private val LicensePlateBlue = Color(0xFF0A84FF)
private val LicensePlateInk = Color(0xFF1A1A1F)

private val PLATE_HEIGHT = 32.dp
private val PLATE_CORNER = 4.dp
private val PLATE_PANEL_PADDING_H = 11.dp

private val VEHICLE_ICON_SIZE = 28.dp
