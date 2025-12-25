package com.sujalkumar.knockme.ui.auth

data class AuthState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
