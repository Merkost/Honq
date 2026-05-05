package com.merkost.honq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.LicenseTypeId
import com.merkost.honq.presentation.components.base.LicenseTypeIcon
import com.merkost.honq.presentation.screens.home.HomeState
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.util.openUrl
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.home_license_type
import honq.shared.generated.resources.home_state
import honq.shared.generated.resources.home_take_practice_test
import honq.shared.generated.resources.home_no_questions_available
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateLicenseSheet(
    state: HomeState,
    onSelectState: (String) -> Unit,
    onSelectLicenseType: (String) -> Unit,
    onOpenExternalLink: (linkType: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = HonqTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedState = state.states.firstOrNull { it.id == state.selectedStateId }
    val showNoContentHint = state.selectedQuestionSet == null && !state.isSyncing
        && state.selectedStateId != null && state.selectedLicenseTypeId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HonqSizing.screenPadding)
                .padding(bottom = HonqSpacing.xl)
        ) {
            Text(
                text = stringResource(Res.string.home_state),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
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
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
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
                    val tint = when {
                        !hasQuestionSet -> colors.textMuted
                        isSelected -> colors.primary
                        else -> colors.textSecondary
                    }
                    SelectableChip(
                        text = type.name,
                        selected = isSelected,
                        enabled = hasQuestionSet,
                        onClick = { onSelectLicenseType(type.id) },
                        icon = {
                            LicenseTypeIcon(
                                typeId = LicenseTypeId.fromId(type.id),
                                tint = tint,
                                modifier = Modifier.size(HonqSizing.iconSize16)
                            )
                        }
                    )
                }
            }

            if (showNoContentHint) {
                Spacer(modifier = Modifier.height(HonqSpacing.md))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(HonqSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(HonqSizing.iconSize16)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
                        Text(
                            text = stringResource(Res.string.home_no_questions_available),
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        val practiceUrl = selectedState?.externalPracticeUrl
                        if (!practiceUrl.isNullOrBlank()) {
                            Text(
                                text = stringResource(Res.string.home_take_practice_test),
                                color = colors.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    onOpenExternalLink("practice_test", practiceUrl)
                                    openUrl(practiceUrl)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectableChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    val colors = HonqTheme.colors
    val backgroundColor = if (selected) colors.primarySurface else colors.surfaceVariant
    val borderColor = if (selected) colors.primary else colors.border
    val textColor = when {
        !enabled -> colors.textMuted
        selected -> colors.primary
        else -> colors.textSecondary
    }
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
        if (icon != null) icon()
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
