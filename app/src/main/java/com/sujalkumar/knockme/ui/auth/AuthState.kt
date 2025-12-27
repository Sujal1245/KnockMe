package com.sujalkumar.knockme.ui.auth

import com.sujalkumar.knockme.domain.model.AuthError

data class AuthState(
    val isCheckingSession: Boolean = false,
    val isSigningIn: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: AuthError? = null
)
