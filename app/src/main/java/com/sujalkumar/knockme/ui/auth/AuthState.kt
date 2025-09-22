package com.sujalkumar.knockme.ui.auth

import android.content.Intent
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.api.ApiException

sealed interface AuthState {
    data object Idle : AuthState
    data class EmailSignInIntentReady(val intent: Intent) : AuthState
    data class GoogleSignInIntentReady(val intent: Intent) : AuthState
    data object ProcessingSignIn : AuthState // Generic state for when we are actively processing
    data object AuthenticationSuccess : AuthState
    data class EmailSignInError(val result: FirebaseAuthUIAuthenticationResult) : AuthState
    data class GoogleSignInError(val apiException: ApiException?) : AuthState
    data class FirebaseAuthenticationError(val exception: Exception) : AuthState
    data object SigningOut : AuthState
    data object SignOutSuccess : AuthState
    data object SignOutError : AuthState // Can be made more specific if needed (e.g., FirebaseSignOutError)
}