package com.merkost.honq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.data.di.databaseModule
import com.merkost.honq.di.sharedModules
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            if (HonqApplication.isTampered) {
                TamperedAppScreen(onExit = { finishAffinity() })
            } else {
                KoinApplication(
                    application = {
                        androidContext(this@MainActivity)
                        modules(sharedModules() + databaseModule)
                    }
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
private fun TamperedAppScreen(onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "App Verification Failed",
            color = Color(0xFFFF453A),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This copy of Honq could not be verified.\n\n" +
                    "Please download the official app from Google Play Store to ensure you have a secure, unmodified version.",
            color = Color(0xFFF5F5F7),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD60A),
                contentColor = Color(0xFF1C1C1E)
            )
        ) {
            Text(
                text = "Exit",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
