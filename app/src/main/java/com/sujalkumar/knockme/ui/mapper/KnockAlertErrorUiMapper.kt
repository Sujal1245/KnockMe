package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.domain.model.KnockAlertError

fun KnockAlertError.toUiMessage(): String =
    when (this) {
        KnockAlertError.NotAuthenticated ->
            "You must be signed in to knock."

        KnockAlertError.PermissionDenied ->
            "You don’t have permission to knock on this alert."

        KnockAlertError.AlreadyKnocked ->
            "You’ve already knocked on this alert."

        KnockAlertError.NotFound ->
            "This alert no longer exists."

        KnockAlertError.Network ->
            "Network error. Please try again."

        KnockAlertError.Unknown ->
            "Something went wrong. Please try again."
    }
