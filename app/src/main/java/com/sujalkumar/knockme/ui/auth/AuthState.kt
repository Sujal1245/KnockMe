package com.sujalkumar.knockme.ui.auth

import android.content.Intent
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

sealed interface AuthState {
    data object Idle : AuthState                      // Initial or reset state
    data object AuthenticationSuccess : AuthState     // User is authenticated (either initially or after sign-in)
    data object SigningIn : AuthState                 // Sign-in process is active
    data object SigningOut : AuthState                // Sign-out process is active
    data class SignInIntentReady(val intent: Intent) : AuthState // Intent for FirebaseUI is ready
    data class SignInError(val result: FirebaseAuthUIAuthenticationResult) : AuthState // Sign-in failed
    data object SignOutSuccess : AuthState            // User signed out successfully
    data object SignOutError : AuthState              // Sign-out failed
}