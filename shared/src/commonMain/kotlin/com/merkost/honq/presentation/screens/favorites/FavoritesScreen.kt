package com.merkost.honq.presentation.screens.favorites

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestion: (String) -> Unit
) {
    val container = koinViewModel<FavoritesContainer>()

    val state by container.store.subscribe { action ->
        when (action) {
            FavoritesAction.NavigateBack -> onNavigateBack()
        }
    }

    FavoritesContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateToQuestion = onNavigateToQuestion
    )
}

@Composable
private fun FavoritesContent(
    state: FavoritesState,
    onIntent: (FavoritesIntent) -> Unit,
    onNavigateToQuestion: (String) -> Unit
) {
    HonqScaffold(
        title = "Favorites",
        onNavigateBack = { onIntent(FavoritesIntent.NavigateBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = state.isLoading,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                },
                contentKey = { it }
            ) { isLoading ->
                if (isLoading) {
                    FullscreenLoading()
                } else {
                    FavoritesList(
                        favorites = state.favorites,
                        onToggleFavorite = { questionId ->
                            onIntent(FavoritesIntent.ToggleFavorite(questionId))
                        },
                        onNavigateToQuestion = onNavigateToQuestion
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Question>,
    onToggleFavorite: (String) -> Unit,
    onNavigateToQuestion: (String) -> Unit
) {
    val colors = HonqTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HonqSizing.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        if (favorites.isEmpty()) {
            HonqCard {
                Text(
                    text = "No favorites yet",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Text(
                    text = "Save questions during practice or mock tests to review them here.",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            favorites.forEach { question ->
                FavoriteQuestionCard(
                    question = question,
                    onToggleFavorite = { onToggleFavorite(question.id) },
                    onClick = { onNavigateToQuestion(question.id) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteQuestionCard(
    question: Question,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard(onClick = onClick) {
        Text(
            text = question.text,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
                FavoriteMetaPill(text = "#${question.id}")
                val categoryLabel = question.categoryName.ifBlank {
                    question.categoryId.formatCategoryId()
                }
                FavoriteMetaPill(text = categoryLabel)
                FavoriteMetaPill(text = question.stateId.uppercase())
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = "Remove from favorites",
                    tint = colors.primary
                )
            }
        }
    }
}

private fun String.formatCategoryId(): String =
    replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

@Composable
private fun FavoriteMetaPill(text: String) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(colors.surfaceVariant)
            .padding(horizontal = HonqSpacing.sm, vertical = HonqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            fontSize = 12.sp
        )
    }
}
