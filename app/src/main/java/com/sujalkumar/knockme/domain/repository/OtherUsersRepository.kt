package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.data.model.AppUser

interface OtherUsersRepository {
    suspend fun getUserById(userId: String): AppUser?
}