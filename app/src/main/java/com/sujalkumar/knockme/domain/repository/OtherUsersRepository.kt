package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.User

interface OtherUsersRepository {
    suspend fun getUserById(userId: String): User?
}