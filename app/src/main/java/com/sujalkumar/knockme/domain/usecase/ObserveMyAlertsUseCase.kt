package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMyAlertsUseCase(
    private val authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository
) {
    operator fun invoke(): Flow<List<KnockAlert>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                knockAlertRepository.observeAlertsByOwner(user.uid)
            }
        }
}
