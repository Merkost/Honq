package com.merkost.honq.presentation.components.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnimating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { isAnimating = false }
    )

    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            isAnimating = true
        }
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale)
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(150))).togetherWith(
                    scaleOut(targetScale = 0.6f) + fadeOut(animationSpec = tween(100))
                )
            }
        ) { favorite ->
            Icon(
                imageVector = if (favorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = if (favorite) "Remove from favorites" else "Save to favorites",
                tint = if (favorite) HonqTheme.colors.primary else HonqTheme.colors.textMuted
            )
        }
    }
}
