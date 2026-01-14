package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> =
        authRepository.currentUser
}
