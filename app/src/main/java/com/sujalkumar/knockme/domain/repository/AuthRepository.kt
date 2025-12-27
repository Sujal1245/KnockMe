package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.SignInResult
import com.sujalkumar.knockme.domain.model.User

interface AuthRepository {
    fun getSignedInUser(): User?
    suspend fun googleSignIn(): SignInResult
    suspend fun signOut()
}
