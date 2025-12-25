package com.sujalkumar.knockme.data.repository

import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.data.model.SignInResult

interface AuthRepository {
    fun getSignedInUser(): FirebaseUser?
    suspend fun googleSignIn(): SignInResult
    suspend fun signOut()
}
