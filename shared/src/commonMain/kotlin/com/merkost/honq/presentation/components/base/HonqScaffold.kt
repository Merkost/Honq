package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_honq_logo
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HonqScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    showLogo: Boolean = false,
    centered: Boolean = true,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val titleSlot: @Composable () -> Unit = {
        if (showLogo) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic_honq_logo),
                    contentDescription = "Honq Logo",
                    modifier = Modifier.size(HonqSizing.iconSizeLarge)
                )
                title?.let {
                    Spacer(modifier = Modifier.width(HonqSpacing.sm))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            title?.let { Text(it) }
        }
    }
    val navIconSlot: @Composable () -> Unit = {
        onNavigateBack?.let { onBack ->
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HonqTheme.colors.background,
        bottomBar = bottomBar,
        topBar = {
            if (title != null || onNavigateBack != null || showLogo) {
                if (centered) {
                    CenterAlignedTopAppBar(
                        title = titleSlot,
                        navigationIcon = navIconSlot,
                        actions = actions,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = HonqTheme.colors.background,
                            titleContentColor = HonqTheme.colors.textPrimary,
                            navigationIconContentColor = HonqTheme.colors.textPrimary
                        )
                    )
                } else {
                    TopAppBar(
                        title = titleSlot,
                        navigationIcon = navIconSlot,
                        actions = actions,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = HonqTheme.colors.background,
                            titleContentColor = HonqTheme.colors.textPrimary,
                            navigationIconContentColor = HonqTheme.colors.textPrimary
                        )
                    )
                }
            }
        },
        content = content
    )
}
