package com.sujalkumar.knockme.data.model

import com.google.firebase.auth.FirebaseUser

data class SignInResult(
    val data: FirebaseUser?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)
