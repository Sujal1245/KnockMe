package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val userDetailsRepository: UserDetailsRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        userDetailsRepository.clearUserDetails()
    }
}