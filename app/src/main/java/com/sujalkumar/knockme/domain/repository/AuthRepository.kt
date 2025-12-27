package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signOut()
}
