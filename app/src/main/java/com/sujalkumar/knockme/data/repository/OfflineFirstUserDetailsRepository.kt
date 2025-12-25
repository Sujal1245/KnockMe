package com.sujalkumar.knockme.data.repository

import com.google.firebase.auth.FirebaseUser
import com.sujalkumar.knockme.data.datasource.UserDataSource
import com.sujalkumar.knockme.data.model.AppUser
import kotlinx.coroutines.flow.Flow

class UserDetailsRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserDetailsRepository {

    override val user: Flow<AppUser?> = userDataSource.appUser

    override suspend fun setUserDetails(user: FirebaseUser) {
        val appUser = AppUser(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl?.toString()
        )

        userDataSource.storeAppUser(appUser)
    }

    override suspend fun clearUserDetails() {
        userDataSource.clearAppUser()
    }
}
