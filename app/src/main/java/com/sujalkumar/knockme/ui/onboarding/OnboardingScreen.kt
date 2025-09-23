package com.sujalkumar.knockme.ui.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.ui.auth.AuthState
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val emailSignInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract(),
    ) { result ->
        viewModel.processEmailSignInResult(result, result.resultCode)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.processGoogleSignInResult(result.data)
        } else {
            if (result.data == null) {
                viewModel.resetAuthState()
            }
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.EmailSignInIntentReady -> {
                emailSignInLauncher.launch(state.intent)
                viewModel.resetAuthState()
            }

            is AuthState.GoogleSignInIntentReady -> {
                googleSignInLauncher.launch(state.intent)
                viewModel.resetAuthState()
            }

            is AuthState.AuthenticationSuccess -> {
                onNavigateToHome()
            }

            is AuthState.EmailSignInError -> {
                val errorMessage =
                    state.result.idpResponse?.error?.message ?: "Email sign-in failed"
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetAuthState()
            }

            is AuthState.GoogleSignInError -> {
                val errorMessage = state.apiException?.localizedMessage ?: "Google sign-in failed"
                snackbarHostState.showSnackbar(
                    message = "Google Sign-In Error: $errorMessage",
                    duration = SnackbarDuration.Long
                )
                viewModel.resetAuthState()
            }

            is AuthState.FirebaseAuthenticationError -> {
                val errorMessage =
                    state.exception.localizedMessage ?: "Authentication process failed"
                snackbarHostState.showSnackbar(
                    message = "Error: $errorMessage",
                    duration = SnackbarDuration.Long
                )
                viewModel.resetAuthState()
            }

            is AuthState.SignOutSuccess -> {
                viewModel.resetAuthState()
            }

            else -> { /* Idle, ProcessingSignIn, SigningOut handled by UI visibility changes */
            }
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

            AnimatedVisibility(
                visible = authState == AuthState.ProcessingSignIn || authState == AuthState.AuthenticationSuccess, // Corrected state
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }

            AnimatedVisibility(
                visible = authState == AuthState.Idle ||
                        authState is AuthState.EmailSignInIntentReady ||
                        authState is AuthState.GoogleSignInIntentReady ||
                        authState is AuthState.EmailSignInError ||
                        authState is AuthState.GoogleSignInError ||
                        authState is AuthState.FirebaseAuthenticationError ||
                        authState == AuthState.SigningOut ||
                        authState == AuthState.SignOutError ||
                        authState == AuthState.SignOutSuccess,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column {
                    Image(
                        painter = painterResource(R.drawable.knock_door),
                        contentDescription = "App Logo"
                    )
                    ElevatedCard(
                        modifier = modifier
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Welcome to KnockMe",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign in to connect and get started.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            OutlinedButton(
                                onClick = { viewModel.createGoogleSignInIntent() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.google),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Continue with Google")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.createEmailSignInIntent() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = "Email Icon",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Continue with Email")
                            }
                        }
                    }
                }
            }
        }
    }
}
