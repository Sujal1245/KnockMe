package com.sujalkumar.knockme.domain.model

sealed interface KnockAlertResult {
    object Success : KnockAlertResult
    data class Failure(val error: KnockAlertError) : KnockAlertResult
}