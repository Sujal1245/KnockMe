package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.data.model.KnockAlert
import kotlinx.coroutines.flow.Flow

interface KnockAlertRepository {

    /**
     * Adds a new KnockAlert to Firestore.
     * @param alert The KnockAlert to be added.
     * @return Result indicating success or failure.
     */
    suspend fun addKnockAlert(alert: KnockAlert): Result<Unit>

    /**
     * Retrieves a flow of active KnockAlerts.
     * Active alerts are those whose targetTimestamp has passed and are ready to be shown.
     * @return Flow emitting a list of active KnockAlerts.
     */
    fun getActiveKnockAlerts(): Flow<List<KnockAlert>>

    /**
     * Allows a user to "knock" on an alert.
     * This should update the alert in Firestore, adding the user's ID to the knockedByUids list.
     * Implementations should ensure a user can only knock once.
     * @param alertId The ID of the KnockAlert.
     * @param userId The ID of the user performing the knock.
     * @return Result indicating success or failure.
     */
    suspend fun knockOnAlert(alertId: String, userId: String): Result<Unit>

    /**
     * Retrieves all KnockAlerts created by a specific user.
     * @param ownerId The UID of the user whose alerts are to be fetched.
     * @return Flow emitting a list of the user's KnockAlerts.
     */
    fun getUserKnockAlerts(ownerId: String): Flow<List<KnockAlert>>

    /**
     * Deletes a KnockAlert.
     * Typically, only the owner should be able to delete their alert.
     * @param alertId The ID of the KnockAlert to be deleted.
     * @return Result indicating success or failure.
     */
    suspend fun deleteKnockAlert(alertId: String): Result<Unit>
}