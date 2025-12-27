package com.sujalkumar.knockme.domain.model

sealed class AuthError {
    object Network : AuthError()
    object InvalidCredentials : AuthError()
    object UserCancelled : AuthError()
    object Unauthorized : AuthError()
    object Unknown : AuthError()
}
