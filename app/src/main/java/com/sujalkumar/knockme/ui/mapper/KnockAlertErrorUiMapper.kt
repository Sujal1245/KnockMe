package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.ui.common.UiText

fun KnockAlertError.toUiText(): UiText =
    when (this) {
        KnockAlertError.NotAuthenticated ->
            UiText.StringResource(resId = R.string.not_authenticated_error)

        KnockAlertError.PermissionDenied ->
            UiText.StringResource(resId = R.string.permission_denied_error)

        KnockAlertError.AlreadyKnocked ->
            UiText.StringResource(resId = R.string.already_knocked_error)

        KnockAlertError.NotFound ->
            UiText.StringResource(resId = R.string.alert_not_found_error)

        KnockAlertError.Network ->
            UiText.StringResource(resId = R.string.internet_connection_error)

        KnockAlertError.Unknown ->
            UiText.StringResource(resId = R.string.unknown_error)
    }
