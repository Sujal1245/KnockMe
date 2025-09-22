package com.sujalkumar.knockme.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import com.sujalkumar.knockme.ui.auth.AuthState
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract(),
    ) { result ->
        viewModel.processSignInResult(result, result.resultCode)
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.SignInIntentReady -> {
                signInLauncher.launch(state.intent)
                viewModel.resetAuthState() 
            }
            is AuthState.AuthenticationSuccess -> { // Updated state for navigation
                onNavigateToHome()
                viewModel.resetAuthState() 
            }
            is AuthState.SignInError -> {
                val errorMessage = state.result.idpResponse?.error?.message ?: "Sign-in failed"
                snackbarHostState.showSnackbar(message = errorMessage)
                viewModel.resetAuthState()
            }
            else -> { /* Other states handled by UI changes directly or are intermediate */ }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (authState) {
                AuthState.SigningIn, AuthState.AuthenticationSuccess -> { // Show progress indicator also when checking auth state or on success before navigation
                    CircularProgressIndicator()
                }
                AuthState.Idle, is AuthState.SignInIntentReady, is AuthState.SignInError, AuthState.SigningOut, AuthState.SignOutError, AuthState.SignOutSuccess -> {
                    // Show sign-in UI for Idle, after errors, or after successful sign out (which transitions to Idle)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "KnockMe",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Welcome! Please sign in to continue.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.createSignInIntent() }) {
                            Text("Sign In / Register")
                        }
                    }
                }
            }
        }
    }
}
