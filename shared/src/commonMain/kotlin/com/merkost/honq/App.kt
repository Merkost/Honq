package com.merkost.honq

import androidx.compose.runtime.Composable
import com.merkost.honq.presentation.navigation.NavGraph
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.core.module.Module

@Composable
fun App() {
    HonqTheme {
        NavGraph()
    }
}
