package com.sujalkumar.knockme.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class OtherUsersRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val externalScope: CoroutineScope
) : OtherUsersRepository {

    private val userCache = MutableStateFlow<Map<String, User>>(emptyMap())

    override fun observeUser(userId: String): Flow<User?> {
        return userCache
            .map { it[userId] }
            .onStart {
                if (!userCache.value.containsKey(userId)) {
                    fetchAndCacheUser(userId)
                }
            }
    }

    private fun fetchAndCacheUser(userId: String) {
        // Fire-and-forget fetch; cache drives UI updates
        externalScope.launch {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val user = document.toObject(AppUser::class.java)?.toUser()

                if (user != null) {
                    userCache.update { current ->
                        current + (userId to user)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // ignore – best effort cache
            }
        }
    }
}