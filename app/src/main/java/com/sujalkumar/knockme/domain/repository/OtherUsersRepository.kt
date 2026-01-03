package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.User
import kotlinx.coroutines.flow.Flow

interface OtherUsersRepository {
    fun observeUser(userId: String): Flow<User?>
    suspend fun upsertCurrentUser(user: User)
}
