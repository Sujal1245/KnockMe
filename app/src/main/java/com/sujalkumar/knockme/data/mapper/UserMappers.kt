package com.sujalkumar.knockme.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.domain.model.User

fun AppUser.toUser(): User {
    return User(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl
    )
}

fun FirebaseUser.toUser(): User {
    return User(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}

fun User.toAppUser(): AppUser {
    return AppUser(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl
    )
}
