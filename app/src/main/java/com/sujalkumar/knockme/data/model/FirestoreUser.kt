package com.sujalkumar.knockme.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class FirestoreUser(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,

    @ServerTimestamp
    val createdAt: Timestamp? = null
)
