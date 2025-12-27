package com.sujalkumar.knockme.domain.model

data class User(
    val uid: String,
    val displayName: String? = null,
    val photoUrl: String? = null
)
