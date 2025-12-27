package com.sujalkumar.knockme.data.mapper

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.sujalkumar.knockme.domain.model.AuthError
import java.io.IOException

fun Exception.toAuthError(): AuthError =
    when (this) {
        is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
        is FirebaseAuthUserCollisionException -> AuthError.Unauthorized
        is IOException -> AuthError.Network
        else -> AuthError.Unknown
    }