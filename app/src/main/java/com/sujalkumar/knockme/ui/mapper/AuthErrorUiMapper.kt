package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.domain.model.AuthError

fun AuthError.toUiMessage(): String =
    when (this) {
        AuthError.Network ->
            "Please check your internet connection."

        AuthError.InvalidCredentials ->
            "Invalid credentials. Please try again."

        AuthError.UserCancelled ->
            "Sign-in cancelled."

        AuthError.Unauthorized ->
            "You are not authorized to sign in."

        AuthError.Unknown ->
            "Something went wrong. Please try again."
    }
