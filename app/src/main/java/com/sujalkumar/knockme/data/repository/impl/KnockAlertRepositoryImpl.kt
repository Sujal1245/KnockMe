package com.sujalkumar.knockme.data.repository.impl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.sujalkumar.knockme.data.model.KnockAlert
import com.sujalkumar.knockme.data.repository.KnockAlertRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class KnockAlertRepositoryImpl(
    firestore: FirebaseFirestore
) : KnockAlertRepository {

    private val knockAlertsCollection = firestore.collection("knock_alerts")

    override suspend fun addKnockAlert(alert: KnockAlert): Result<Unit> {
        return try {
            // Generate a new document reference with an auto-generated ID
            val newAlertRef = knockAlertsCollection.document()
            // Create a new alert object with the generated ID set in its 'id' field
            val alertWithId = alert.copy(id = newAlertRef.id)
            // Set the data for the new document using the alert object that includes the ID
            newAlertRef.set(alertWithId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveKnockAlerts(): Flow<List<KnockAlert>> = callbackFlow {
        val currentTime = System.currentTimeMillis()
        val listenerRegistration = knockAlertsCollection
            .whereLessThanOrEqualTo("targetTimestamp", currentTime)
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val alerts = snapshot.documents.mapNotNull {
                        it.toObject<KnockAlert>()
                    }
                    trySend(alerts).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun knockOnAlert(alertId: String, userId: String): Result<Unit> {
        return try {
            knockAlertsCollection.document(alertId)
                .update("knockedByUids", FieldValue.arrayUnion(userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserKnockAlerts(ownerId: String): Flow<List<KnockAlert>> = callbackFlow {
        val listenerRegistration = knockAlertsCollection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val alerts = snapshot.documents.mapNotNull {
                        val knockAlert = it.toObject<KnockAlert>()
                        knockAlert?.copy(id = it.id)
                    }
                    trySend(alerts).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun deleteKnockAlert(alertId: String): Result<Unit> {
        return try {
            knockAlertsCollection.document(alertId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
