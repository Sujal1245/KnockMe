package com.sujalkumar.knockme.data.repository

import com.sujalkumar.knockme.data.model.AppUser

interface OtherUsersRepository {
    suspend fun getUserById(userId: String): AppUser?
}
