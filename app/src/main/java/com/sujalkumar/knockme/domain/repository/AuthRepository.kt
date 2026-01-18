package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signOut()
}
