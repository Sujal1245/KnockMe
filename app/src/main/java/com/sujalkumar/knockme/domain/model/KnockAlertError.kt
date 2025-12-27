package com.sujalkumar.knockme.domain.model

sealed interface KnockAlertError {
    object NotAuthenticated : KnockAlertError
    object PermissionDenied : KnockAlertError
    object AlreadyKnocked : KnockAlertError
    object NotFound : KnockAlertError
    object Network : KnockAlertError
    object Unknown : KnockAlertError
}