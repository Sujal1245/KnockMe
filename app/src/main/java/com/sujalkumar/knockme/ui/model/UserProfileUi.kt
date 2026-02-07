package com.sujalkumar.knockme.ui.model

data class UserProfileUi(
    val uid: String,
    val displayName: String?,
    val photoUrl: String?,
    val bio: String?,
    val joinedText: String,
    val isCurrentUser: Boolean
)
