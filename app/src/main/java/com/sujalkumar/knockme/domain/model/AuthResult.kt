package com.sujalkumar.knockme.domain.model

sealed interface AuthResult {
    data class Success(
        val user: User
    ) : AuthResult

    data class Failure(
        val error: AuthError
    ) : AuthResult
}