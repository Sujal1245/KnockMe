package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.model.UserSummary

fun User.toUserSummary(): UserSummary {
    return UserSummary(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl
    )
}
