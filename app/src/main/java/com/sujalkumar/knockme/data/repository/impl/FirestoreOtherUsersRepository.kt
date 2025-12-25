package com.sujalkumar.knockme.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.repository.OtherUsersRepository
import kotlinx.coroutines.tasks.await

class FirestoreOtherUsersRepository(
    private val firestore: FirebaseFirestore
) : OtherUsersRepository {

    override suspend fun getUserById(userId: String): AppUser? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(AppUser::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
