package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.presentation.theme.HonqSizing

@Composable
fun LicenseTypeIcon(
    typeId: LicenseTypeId?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val icon = when (typeId) {
        LicenseTypeId.CAR -> Icons.Rounded.DirectionsCar
        LicenseTypeId.RIDER -> Icons.Rounded.TwoWheeler
        LicenseTypeId.RIDER_SPECIAL_MOBILITY_VEHICLE -> Icons.Rounded.TwoWheeler
        LicenseTypeId.HEAVY_RIGID -> Icons.Rounded.LocalShipping
        LicenseTypeId.HEAVY_COMBINATION -> Icons.Rounded.LocalShipping
        null -> Icons.Rounded.DirectionsCar
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(HonqSizing.iconSizeLarge)
    )
}
