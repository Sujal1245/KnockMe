package com.sujalkumar.knockme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sujalkumar.knockme.navigation.NavigationRoot
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import com.sujalkumar.knockme.ui.theme.KnockMeTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel by viewModel()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.authState.value.isCheckingSession
        }

        setContent {
            KnockMeTheme {
                enableEdgeToEdge()
                NavigationRoot()
            }
        }
    }
}