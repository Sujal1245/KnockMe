package com.sujalkumar.knockme.data.datasource

import androidx.datastore.core.DataStore
import com.sujalkumar.knockme.data.model.AppUser
import kotlinx.coroutines.flow.Flow

class UserDataSource(
    private val appUserDataStore: DataStore<AppUser?>
) {
    val appUser: Flow<AppUser?> = appUserDataStore.data

    suspend fun storeAppUser(user: AppUser) {
        appUserDataStore.updateData {
            user
        }
    }

    suspend fun clearAppUser() {
        appUserDataStore.updateData {
            null
        }
    }
}