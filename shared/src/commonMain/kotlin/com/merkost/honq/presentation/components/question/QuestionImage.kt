package com.merkost.honq.presentation.components.question

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_image_broken
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuestionImage(
    url: String,
    contentDescription: String? = "Question image",
    modifier: Modifier = Modifier
) {
    var showFullscreen by remember { mutableStateOf(false) }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { showFullscreen = true },
        contentScale = ContentScale.Fit,
        loading = {
            ImagePlaceholder(showLoading = true)
        },
        error = {
            ImagePlaceholder(showLoading = false)
        }
    )

    if (showFullscreen) {
        FullscreenImageDialog(
            url = url,
            onDismiss = { showFullscreen = false }
        )
    }
}

@Composable
private fun FullscreenImageDialog(
    url: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                }
        ) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = "Fullscreen image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholder(
    showLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(
                color = colors.imagePlaceholder,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = colors.loadingIndicator,
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_image_broken),
                contentDescription = "Failed to load image",
                modifier = Modifier.size(48.dp),
                tint = colors.imagePlaceholderIcon
            )
        }
    }
}
