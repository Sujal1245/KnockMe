package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserDetailsRepository {
    val user: Flow<User?>

    suspend fun setUserDetails(user: User)

    suspend fun clearUserDetails()
}