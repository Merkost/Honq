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
                                totalAnswered = state.totalAnswered,
                                totalQuestions = state.totalQuestions,
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
                            val progress = state.progressMap[category.id]
                            CategoryCard(
                                category = category,
                                answered = progress?.answeredQuestions ?: 0,
                                total = progress?.totalQuestions ?: 0,
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
    totalAnswered: Int,
    totalQuestions: Int,
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
                        text = if (totalQuestions > 0) "$totalAnswered / $totalQuestions seen" else "Practice questions from all topics",
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
    answered: Int,
    total: Int,
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
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md),
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(categoryId = category.id, iconName = category.iconName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (total > 0) "$answered / $total seen" else category.description,
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        maxLines = 2
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
private fun CategoryIcon(categoryId: String, iconName: String) {
    val colors = HonqTheme.colors

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getCategoryEmoji(categoryId, iconName),
            fontSize = 24.sp
        )
    }
}

private fun getCategoryEmoji(categoryId: String, iconName: String): String {
    val key = iconName.ifEmpty { categoryId }.lowercase()
    return when (key) {
        "alcohol_and_drugs", "alcohol", "alcohol_fatigue" -> "\uD83C\uDF7A"
        "bicycle_safety", "bicycle" -> "\uD83D\uDEB2"
        "fatigue_and_defensive_driving", "fatigue", "defensive_driving" -> "\uD83D\uDE34"
        "general_knowledge", "general" -> "\uD83D\uDCD6"
        "icac" -> "\u2696\uFE0F"
        "intersections", "roundabouts" -> "\uD83D\uDD04"
        "negligent_driving", "negligent" -> "\u26D4"
        "pedestrians", "sharing_road" -> "\uD83D\uDEB6"
        "rider_safety", "rider" -> "\uD83C\uDFCD\uFE0F"
        "road_users_hazards", "hazards", "hazard_perception" -> "\u26A0\uFE0F"
        "seat_belts_restraints", "seat_belts", "restraints" -> "\uD83D\uDD12"
        "speed_limits", "speed" -> "\uD83C\uDFCE\uFE0F"
        "traffic_lights_lanes", "traffic_lights", "lanes" -> "\uD83D\uDEA6"
        "traffic_signs", "signs" -> "\uD83D\uDEA7"
        "road_rules", "rules" -> "\uD83D\uDEE3\uFE0F"
        "safety", "road_safety" -> "\u26A0\uFE0F"
        "parking" -> "\uD83C\uDD7F\uFE0F"
        "vehicles", "vehicle_control" -> "\uD83D\uDE97"
        "emergencies", "emergency" -> "\uD83D\uDEA8"
        "night", "night_driving" -> "\uD83C\uDF19"
        "weather", "conditions" -> "\uD83C\uDF27\uFE0F"
        "licensing", "license" -> "\uD83D\uDCCB"
        else -> "\uD83D\uDCD6"
    }
}
