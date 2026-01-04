package com.sujalkumar.knockme.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sujalkumar.knockme.data.mapper.toFirestoreUser
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.data.model.FirestoreUser
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class OtherUsersRepositoryImpl(
    private val firestore: FirebaseFirestore
) : OtherUsersRepository {

    private val userCache = MutableStateFlow<Map<String, User>>(emptyMap())
    private val inFlightFetches = mutableSetOf<String>()

    override fun observeUser(userId: String): Flow<User?> {
        return userCache
            .map { it[userId] }
            .onStart {
                if (!userCache.value.containsKey(userId) &&
                    inFlightFetches.add(userId)
                ) {
                    try {
                        fetchAndCacheUser(userId)
                    } finally {
                        inFlightFetches.remove(userId)
                    }
                }
            }
    }

    override suspend fun upsertCurrentUser(user: User) {
        firestore.collection("users")
            .document(user.uid)
            .set(user.toFirestoreUser(), SetOptions.merge())
            .await()
    }

    private suspend fun fetchAndCacheUser(userId: String) {
        val document = firestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        if (!document.exists()) return

        val user = document
            .toObject(FirestoreUser::class.java)
            ?.toUser()

        if (user != null) {
            userCache.update { current ->
                current + (userId to user)
            }
        }
    }
}