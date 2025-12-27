package com.sujalkumar.knockme.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.sujalkumar.knockme.data.mapper.toFirebaseKnockAlert
import com.sujalkumar.knockme.data.mapper.toKnockAlert
import com.sujalkumar.knockme.data.model.FirebaseKnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first

class KnockAlertRepositoryImpl(
    firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : KnockAlertRepository {

    private val knockAlertsCollection = firestore.collection("knock_alerts")

    override suspend fun addKnockAlert(alert: KnockAlert): KnockAlertResult {
        val currentUser = authRepository.currentUser.first()
            ?: return KnockAlertResult.Failure(KnockAlertError.NotAuthenticated)

        return try {
            val newAlertRef = knockAlertsCollection.document()
            val alertWithId = alert.copy(
                id = newAlertRef.id,
                ownerId = currentUser.uid
            )

            newAlertRef.set(alertWithId.toFirebaseKnockAlert()).await()
            KnockAlertResult.Success
        } catch (e: Exception) {
            KnockAlertResult.Failure(e.toKnockAlertError())
        }
    }

    override fun observeKnockAlerts(): Flow<List<KnockAlert>> = callbackFlow {
        val listener = knockAlertsCollection
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents
                    ?.mapNotNull { it.toObject<FirebaseKnockAlert>()?.toKnockAlert() }
                    .orEmpty()

                trySend(alerts).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun knockOnAlert(alertId: String): KnockAlertResult {
        val currentUser = authRepository.currentUser.first()
            ?: return KnockAlertResult.Failure(KnockAlertError.NotAuthenticated)

        return try {
            knockAlertsCollection.document(alertId)
                .update("knockedByUids", FieldValue.arrayUnion(currentUser.uid))
                .await()

            KnockAlertResult.Success
        } catch (e: Exception) {
            KnockAlertResult.Failure(e.toKnockAlertError())
        }
    }

    override fun observeMyKnockAlerts(): Flow<List<KnockAlert>> = callbackFlow {
        val currentUser = authRepository.currentUser.first()
            ?: run {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

        val listener = knockAlertsCollection
            .whereEqualTo("ownerId", currentUser.uid)
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents
                    ?.mapNotNull { it.toObject<FirebaseKnockAlert>()?.toKnockAlert() }
                    .orEmpty()

                trySend(alerts).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun deleteKnockAlert(alertId: String): KnockAlertResult {
        return try {
            knockAlertsCollection.document(alertId).delete().await()
            KnockAlertResult.Success
        } catch (e: Exception) {
            KnockAlertResult.Failure(e.toKnockAlertError())
        }
    }
}

private fun Exception.toKnockAlertError(): KnockAlertError =
    when (this) {
        is FirebaseFirestoreException -> KnockAlertError.Network
        else -> KnockAlertError.Unknown
    }
