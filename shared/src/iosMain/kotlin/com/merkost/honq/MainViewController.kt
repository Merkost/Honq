package com.merkost.honq

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.honq.core.logging.initCedar
import com.merkost.honq.data.di.databaseModule
import com.merkost.honq.di.sharedModules
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.compose.KoinApplication
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    Firebase.initialize()
    initCedar()
    return ComposeUIViewController {
        KoinApplication(
            application = {
                modules(databaseModule + sharedModules())
            }
        ) {
            App()
        }
    }
}
