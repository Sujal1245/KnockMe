package com.sujalkumar.knockme.domain.repository

import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import kotlinx.coroutines.flow.Flow

interface KnockAlertRepository {

    fun observeKnockAlerts(): Flow<List<KnockAlert>>

    fun observeMyKnockAlerts(): Flow<List<KnockAlert>>

    suspend fun addKnockAlert(alert: KnockAlert): KnockAlertResult

    suspend fun knockOnAlert(alertId: String): KnockAlertResult

    suspend fun deleteKnockAlert(alertId: String): KnockAlertResult
}