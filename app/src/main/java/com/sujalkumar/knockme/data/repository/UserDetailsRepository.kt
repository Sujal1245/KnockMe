package com.sujalkumar.knockme.data.repository

import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.data.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserDetailsRepository {
    val user: Flow<AppUser?>

    suspend fun setUserDetails(user: FirebaseUser)

    suspend fun clearUserDetails()
}
