package com.sujalkumar.knockme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.SignInResult
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userDetailsRepository: UserDetailsRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(
        AuthState(isCheckingSession = true)
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSignedInUser()
    }

    private fun checkSignedInUser() {
        viewModelScope.launch {
            val user = authRepository.getSignedInUser()
            _authState.update {
                it.copy(
                    isCheckingSession = false,
                    isSignedIn = user != null,
                    error = null
                )
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.update {
                it.copy(
                    isSigningIn = true,
                    error = null
                )
            }

            when (val result = authRepository.googleSignIn()) {
                is SignInResult.Success -> {
                    handleSuccessfulSignIn(result.user)
                }

                is SignInResult.Failure -> {
                    _authState.update {
                        it.copy(
                            isSigningIn = false,
                            isSignedIn = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleSuccessfulSignIn(user: User) {
        userDetailsRepository.setUserDetails(user)
        _authState.update {
            it.copy(
                isSigningIn = false,
                isSignedIn = true,
                error = null
            )
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.update {
                AuthState(isSignedIn = false)
            }
        }
    }
}
