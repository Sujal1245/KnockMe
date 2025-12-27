package com.sujalkumar.knockme.domain.model

sealed class SignInResult {
    data class Success(
        val user: User
    ) : SignInResult()

    data class Failure(
        val error: AuthError
    ) : SignInResult()
}