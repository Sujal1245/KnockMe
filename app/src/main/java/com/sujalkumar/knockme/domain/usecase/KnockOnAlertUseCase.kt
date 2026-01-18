package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository

class KnockOnAlertUseCase(
    private val authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository
) {

    suspend operator fun invoke(alertId: String): KnockAlertResult {
        val currentUser = authRepository.currentUser.value
            ?: return KnockAlertResult.Failure(KnockAlertError.NotAuthenticated)

        return knockAlertRepository.knockOnAlert(
            alertId = alertId,
            userId = currentUser.uid
        )
    }
}
