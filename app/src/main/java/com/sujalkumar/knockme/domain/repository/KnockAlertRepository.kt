package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import kotlinx.coroutines.flow.Flow

interface KnockAlertRepository {

    fun observeAllAlerts(): Flow<List<KnockAlert>>

    fun observeAlertsByOwner(userId: String): Flow<List<KnockAlert>>

    suspend fun addKnockAlert(
        alert: KnockAlert,
        userId: String
    ): KnockAlertResult

    suspend fun knockOnAlert(
        alertId: String,
        userId: String
    ): KnockAlertResult

    suspend fun deleteKnockAlert(alertId: String): KnockAlertResult
}
