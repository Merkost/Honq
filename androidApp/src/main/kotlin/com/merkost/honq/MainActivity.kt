package com.merkost.honq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.merkost.honq.data.di.databaseModule
import com.merkost.honq.di.sharedModules
import com.merkost.honq.integrity.integrityModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KoinApplication(
                application = {
                    androidContext(this@MainActivity)
                    modules(sharedModules() + databaseModule + integrityModule)
                }
            ) {
                App()
            }
        }
    }
}
