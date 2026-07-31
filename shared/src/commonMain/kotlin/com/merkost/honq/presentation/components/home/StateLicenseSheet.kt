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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import honq.shared.generated.resources.home_selection_updating
import honq.shared.generated.resources.home_setup_subtitle
import honq.shared.generated.resources.home_setup_title
import honq.shared.generated.resources.home_state
import honq.shared.generated.resources.home_take_practice_test
import honq.shared.generated.resources.home_no_questions_available
import honq.shared.generated.resources.home_license_not_available
import honq.shared.generated.resources.home_available_licenses
import honq.shared.generated.resources.home_unavailable_licenses
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HonqSizing.screenPadding)
                .padding(bottom = HonqSpacing.xl)
        ) {
            Text(
                text = stringResource(Res.string.home_setup_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            Text(
                text = stringResource(Res.string.home_setup_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            Text(
                text = stringResource(Res.string.home_state),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            state.states.chunked(3).forEach { stateRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    stateRow.forEach { stateOption ->
                        SelectableChip(
                            modifier = Modifier.weight(1f),
                            text = stateOption.shortName,
                            contentDescription = stateOption.name,
                            selected = stateOption.id == state.selectedStateId,
                            enabled = stateOption.isActive,
                            onClick = { onSelectState(stateOption.id) }
                        )
                    }
                    repeat(3 - stateRow.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
            }

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.home_license_type),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    fontWeight = FontWeight.Bold
                )
                if (state.isSyncing && state.questionSets.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(HonqSizing.iconSize16),
                        strokeWidth = 2.dp,
                        color = colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            val licenseChoices = state.licenseTypes.map { type ->
                val hasQuestionSet = state.questionSets.any { it.licenseTypeId == type.id }
                type to licenseChoiceAvailability(
                    isSyncing = state.isSyncing,
                    questionSetCount = state.questionSets.size,
                    hasQuestionSet = hasQuestionSet,
                )
            }
            val allUpdating = licenseChoices.isNotEmpty() && licenseChoices.all {
                (_, availability) -> availability == LicenseChoiceAvailability.Updating
            }
            Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
                if (allUpdating) {
                    licenseChoices.forEach { (type, availability) ->
                        LicenseTypeChoiceRow(
                            typeName = type.name,
                            shortName = type.shortName,
                            typeId = LicenseTypeId.fromId(type.id),
                            selected = type.id == state.selectedLicenseTypeId,
                            availability = availability,
                            onClick = { onSelectLicenseType(type.id) },
                        )
                    }
                } else {
                    val availableChoices = licenseChoices.filter { (_, availability) ->
                        availability == LicenseChoiceAvailability.Available
                    }
                    val unavailableChoices = licenseChoices.filter { (_, availability) ->
                        availability == LicenseChoiceAvailability.Unavailable
                    }

                    if (availableChoices.isNotEmpty()) {
                        LicenseChoiceGroupLabel(text = stringResource(Res.string.home_available_licenses))
                        availableChoices.forEach { (type, availability) ->
                            LicenseTypeChoiceRow(
                                typeName = type.name,
                                shortName = type.shortName,
                                typeId = LicenseTypeId.fromId(type.id),
                                selected = type.id == state.selectedLicenseTypeId,
                                availability = availability,
                                onClick = { onSelectLicenseType(type.id) },
                            )
                        }
                    }
                    if (unavailableChoices.isNotEmpty()) {
                        LicenseChoiceGroupLabel(text = stringResource(Res.string.home_unavailable_licenses))
                        unavailableChoices.forEach { (type, availability) ->
                            LicenseTypeChoiceRow(
                                typeName = type.name,
                                shortName = type.shortName,
                                typeId = LicenseTypeId.fromId(type.id),
                                selected = type.id == state.selectedLicenseTypeId,
                                availability = availability,
                                onClick = { onSelectLicenseType(type.id) },
                            )
                        }
                    }
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
    modifier: Modifier = Modifier,
    text: String,
    contentDescription: String = text,
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
        modifier = modifier
            .alpha(contentAlpha)
            .sizeIn(minHeight = HonqSizing.minTapTarget)
            .semantics { this.contentDescription = contentDescription }
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(backgroundColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
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

@Composable
private fun LicenseChoiceGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = HonqTheme.colors.textMuted,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun LicenseTypeChoiceRow(
    typeName: String,
    shortName: String,
    typeId: LicenseTypeId?,
    selected: Boolean,
    availability: LicenseChoiceAvailability,
    onClick: () -> Unit,
) {
    val colors = HonqTheme.colors
    val shape = RoundedCornerShape(HonqSizing.cornerRadiusSmall)
    val enabled = availability == LicenseChoiceAvailability.Available
    val isUpdating = availability == LicenseChoiceAvailability.Updating
    val tint = when {
        !enabled -> colors.textMuted
        selected -> colors.primary
        else -> colors.textSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.55f)
            .clip(shape)
            .background(if (selected) colors.primarySurface else colors.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.border,
                shape = shape
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .sizeIn(minHeight = 68.dp)
            .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                .background(if (selected) colors.primarySurface else colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            LicenseTypeIcon(
                typeId = typeId,
                tint = tint
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) colors.primary else colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when {
                    isUpdating -> stringResource(Res.string.home_selection_updating)
                    !enabled -> stringResource(Res.string.home_license_not_available)
                    else -> shortName.uppercase()
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUpdating || !enabled) colors.textMuted else tint
            )
        }

        ChoiceIndicator(selected = selected)
    }
}

@Composable
private fun ChoiceIndicator(selected: Boolean) {
    val colors = HonqTheme.colors
    if (selected) {
        Box(
            modifier = Modifier
                .size(HonqSizing.checkmarkSize)
                .background(colors.primary, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(HonqSizing.iconSize16)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(HonqSizing.checkmarkSize)
                .border(1.dp, colors.border, androidx.compose.foundation.shape.CircleShape)
        )
    }
}
