package com.sujalkumar.knockme.data.repository

import com.sujalkumar.knockme.data.datasource.UserDataSource
import com.sujalkumar.knockme.data.mapper.toAppUser
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDetailsRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserDetailsRepository {

    override val user: Flow<User?> =
        userDataSource.appUser.map { it?.toUser() }

    override suspend fun setUserDetails(user: User) {
        userDataSource.storeAppUser(user.toAppUser())
    }

    override suspend fun clearUserDetails() {
        userDataSource.clearAppUser()
    }
}
