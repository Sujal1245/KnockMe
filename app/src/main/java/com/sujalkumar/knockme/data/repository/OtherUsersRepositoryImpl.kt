package com.sujalkumar.knockme.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sujalkumar.knockme.data.mapper.toFirestoreUser
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.data.model.FirestoreUser
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    override suspend fun upsertCurrentUser(user: User) {
        firestore.collection("users")
            .document(user.uid)
            .set(user.toFirestoreUser(), SetOptions.merge())
            .await()
    }

    private fun fetchAndCacheUser(userId: String) {
        externalScope.launch {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (!document.exists()) return@launch

                val user = document
                    .toObject(FirestoreUser::class.java)
                    ?.toUser()

                if (user != null) {
                    userCache.update { current ->
                        current + (userId to user)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}