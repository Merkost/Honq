package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.merkost.honq.presentation.theme.HonqColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HonqScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = HonqColors.Background,
        topBar = {
            if (title != null || onNavigateBack != null) {
                CenterAlignedTopAppBar(
                    title = { title?.let { Text(it) } },
                    navigationIcon = {
                        onNavigateBack?.let { onBack ->
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = HonqColors.Background,
                        titleContentColor = HonqColors.TextPrimary,
                        navigationIconContentColor = HonqColors.TextPrimary
                    )
                )
            }
        },
        content = content
    )
}
