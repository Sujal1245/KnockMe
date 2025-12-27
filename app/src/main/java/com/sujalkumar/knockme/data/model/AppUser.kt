package com.sujalkumar.knockme.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val uid: String,
    val displayName: String? = null,
    val photoUrl: String? = null
)
