package com.sujalkumar.knockme.data.repository

import android.content.Intent
import com.sujalkumar.knockme.data.model.SignInResult
import com.sujalkumar.knockme.data.model.UserData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getSignedInUser(): UserData?
    suspend fun googleSignIn(intent: Intent): SignInResult
    suspend fun signOut()
}
