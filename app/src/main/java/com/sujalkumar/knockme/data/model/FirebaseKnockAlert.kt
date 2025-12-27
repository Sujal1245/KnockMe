package com.sujalkumar.knockme.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseKnockAlert(
    val id: String = "", // Firestore document ID
    val ownerId: String = "",
    val content: String = "",
    val targetTimestamp: Long = 0L, // UTC milliseconds
    val knockedByUids: List<String> = emptyList()
)