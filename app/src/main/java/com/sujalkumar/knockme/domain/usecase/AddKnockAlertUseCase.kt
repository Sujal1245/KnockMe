package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import kotlinx.coroutines.flow.first

class AddKnockAlertUseCase(
    private val authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository
) {
    suspend operator fun invoke(alert: KnockAlert): KnockAlertResult {
        val user = authRepository.currentUser.first()
            ?: return KnockAlertResult.Failure(KnockAlertError.NotAuthenticated)

        return knockAlertRepository.addKnockAlert(alert, user.uid)
    }
}
