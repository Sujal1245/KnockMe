package com.sujalkumar.knockme.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.sujalkumar.knockme.data.mapper.toFirebaseKnockAlert
import com.sujalkumar.knockme.data.mapper.toKnockAlert
import com.sujalkumar.knockme.data.model.FirebaseKnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class KnockAlertRepositoryImpl(
    firestore: FirebaseFirestore
) : KnockAlertRepository {

    private val knockAlertsCollection = firestore.collection("knock_alerts")

    override suspend fun addKnockAlert(
        alert: KnockAlert,
        userId: String
    ): KnockAlertResult {
        return try {
            val newAlertRef = knockAlertsCollection.document()
            val alertWithId = alert.copy(
                id = newAlertRef.id,
                ownerId = userId,
                createdAt = Clock.System.now()
            )
            newAlertRef.set(alertWithId.toFirebaseKnockAlert()).await()
            KnockAlertResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            KnockAlertResult.Failure(e.toKnockAlertError())
        }
    }

    override fun observeAllAlerts(): Flow<List<KnockAlert>> =
        knockAlertsCollection
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject<FirebaseKnockAlert>()?.toKnockAlert() }
            }
            .distinctUntilChanged()

    override suspend fun knockOnAlert(
        alertId: String,
        userId: String
    ): KnockAlertResult {
        return try {
            knockAlertsCollection.document(alertId)
                .update("knockedByUids", FieldValue.arrayUnion(userId))
                .await()
            KnockAlertResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            KnockAlertResult.Failure(e.toKnockAlertError())
        }
    }

    override fun observeAlertsByOwner(userId: String): Flow<List<KnockAlert>> =
        knockAlertsCollection
            .whereEqualTo("ownerId", userId)
            .orderBy("targetTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject<FirebaseKnockAlert>()?.toKnockAlert() }
            }
            .distinctUntilChanged()

    override suspend fun deleteKnockAlert(alertId: String): KnockAlertResult {
        return try {
            knockAlertsCollection.document(alertId).delete().await()
            KnockAlertResult.Success
        } catch (e: CancellationException) {
            throw e
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
