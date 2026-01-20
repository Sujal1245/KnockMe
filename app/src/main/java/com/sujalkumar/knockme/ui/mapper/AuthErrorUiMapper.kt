package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.AuthError
import com.sujalkumar.knockme.ui.common.UiText

fun AuthError.toUiText(): UiText =
    when (this) {
        AuthError.Network ->
            UiText.StringResource(resId = R.string.internet_connection_error)

        AuthError.InvalidCredentials ->
            UiText.StringResource(resId = R.string.invalid_credentials_error)

        AuthError.UserCancelled ->
            UiText.StringResource(resId = R.string.sign_in_cancelled_error)

        AuthError.Unauthorized ->
            UiText.StringResource(resId = R.string.unauthorized_error)

        AuthError.Unknown ->
            UiText.StringResource(resId = R.string.unknown_error)
    }
