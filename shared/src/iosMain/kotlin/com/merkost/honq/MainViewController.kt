package com.merkost.honq

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.honq.data.di.databaseModule
import com.merkost.honq.di.sharedModules
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        application = {
            modules(sharedModules() + databaseModule)
        }
    ) {
        App()
    }
}
