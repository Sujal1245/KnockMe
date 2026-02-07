package com.sujalkumar.knockme.data.mapper

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.model.FirestoreUser
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

fun FirestoreUser.toUser(): User {
    return User(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl,
        createdAtMillis = createdAt?.toDate()?.time
    )
}

fun User.toFirestoreUser(): FirestoreUser {
    return FirestoreUser(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl,
        createdAt = createdAtMillis?.let {
            Timestamp(
                it / 1000,
                ((it % 1000) * 1_000_000).toInt()
            )
        }
    )
}

fun User.toAppUser(): AppUser {
    return AppUser(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl
    )
}
