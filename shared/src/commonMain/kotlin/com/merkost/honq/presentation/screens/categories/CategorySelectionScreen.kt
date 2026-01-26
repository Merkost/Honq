package com.merkost.honq.presentation.screens.categories

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.Category
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun CategorySelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (categoryId: String?, categoryName: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<CategorySelectionContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            CategorySelectionAction.NavigateBack -> onNavigateBack()
            is CategorySelectionAction.NavigateToPractice -> onNavigateToPractice(action.categoryId, action.categoryName)
        }
    }

    CategorySelectionContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun CategorySelectionContent(
    state: CategorySelectionState,
    onIntent: (CategorySelectionIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqScaffold(
        title = "Practice by Category",
        onNavigateBack = onNavigateBack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.loadingIndicator
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = colors.incorrect,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.categories.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No categories available",
                            color = colors.textSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(HonqSizing.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
                    ) {
                        item {
                            AllCategoriesCard(
                                onClick = { onIntent(CategorySelectionIntent.SelectAllCategories) }
                            )
                            Spacer(modifier = Modifier.height(HonqSpacing.md))
                            Text(
                                text = "Or choose a specific topic",
                                color = colors.textMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = HonqSpacing.xs)
                            )
                            Spacer(modifier = Modifier.height(HonqSpacing.sm))
                        }

                        items(state.categories) { category ->
                            CategoryCard(
                                category = category,
                                onClick = { onIntent(CategorySelectionIntent.SelectCategory(category.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllCategoriesCard(
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
                        .background(colors.primarySurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "All Categories",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Practice questions from all topics",
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors
    val iconName = category.iconName.ifEmpty { "default" }

    HonqCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md),
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(iconName = iconName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (category.description.isNotEmpty()) {
                        Text(
                            text = category.description,
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CategoryIcon(iconName: String) {
    val colors = HonqTheme.colors

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getCategoryEmoji(iconName),
            fontSize = 24.sp
        )
    }
}

private fun getCategoryEmoji(iconName: String): String {
    return when (iconName.lowercase()) {
        "road_rules", "rules" -> "\uD83D\uDEE3\uFE0F"
        "signs", "traffic_signs" -> "\uD83D\uDEA7"
        "safety", "road_safety" -> "\u26A0\uFE0F"
        "alcohol", "alcohol_fatigue", "fatigue" -> "\uD83C\uDF7A"
        "intersections", "roundabouts" -> "\uD83D\uDD04"
        "speed", "speed_limits" -> "\uD83C\uDFCE\uFE0F"
        "parking" -> "\uD83C\uDD7F\uFE0F"
        "hazards", "hazard_perception" -> "\u26A0\uFE0F"
        "vehicles", "vehicle_control" -> "\uD83D\uDE97"
        "pedestrians", "sharing_road" -> "\uD83D\uDEB6"
        "emergencies", "emergency" -> "\uD83D\uDEA8"
        "night", "night_driving" -> "\uD83C\uDF19"
        "weather", "conditions" -> "\uD83C\uDF27\uFE0F"
        "licensing", "license" -> "\uD83D\uDCCB"
        else -> "\uD83D\uDCD6"
    }
}
