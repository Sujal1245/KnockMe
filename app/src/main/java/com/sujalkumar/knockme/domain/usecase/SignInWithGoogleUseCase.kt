package com.sujalkumar.knockme.domain.usecase

import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val otherUsersRepository: OtherUsersRepository
) {
    suspend operator fun invoke(): AuthResult {
        val result = authRepository.signInWithGoogle()

        if (result is AuthResult.Success) {
            val user = result.user
            userDetailsRepository.setUserDetails(user)
            otherUsersRepository.upsertCurrentUser(user)
        }

        return result
    }
}
