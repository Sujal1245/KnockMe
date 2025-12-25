package com.sujalkumar.knockme.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.domain.model.User

fun FirebaseUser.toUser(): User {
    return User(
        uid = uid,
        displayName = displayName?: "Anonymous",
        photoUrl = photoUrl?.toString()
    )
}
