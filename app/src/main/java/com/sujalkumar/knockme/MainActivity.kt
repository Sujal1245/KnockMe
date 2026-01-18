package com.sujalkumar.knockme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sujalkumar.knockme.navigation.NavigationRoot
import androidx.lifecycle.lifecycleScope
import com.sujalkumar.knockme.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.sujalkumar.knockme.ui.theme.KnockMeTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val authRepository: AuthRepository by inject()

        var isCheckingSession = true

        lifecycleScope.launch {
            authRepository.currentUser.first()
            isCheckingSession = false
        }

        splashScreen.setKeepOnScreenCondition {
            isCheckingSession
        }

        setContent {
            KnockMeTheme {
                enableEdgeToEdge()
                NavigationRoot()
            }
        }
    }
}