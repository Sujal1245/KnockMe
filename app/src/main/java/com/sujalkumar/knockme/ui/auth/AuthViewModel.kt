package com.sujalkumar.knockme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(
        AuthState(isCheckingSession = true)
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    // Persist public user profile to Firestore (/users/{uid})
                    otherUsersRepository.upsertCurrentUser(user)
                    // Persist locally
                    userDetailsRepository.setUserDetails(user)
                }

                _authState.update {
                    it.copy(
                        isCheckingSession = false,
                        isSignedIn = user != null
                    )
                }
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

            when (val result = authRepository.signInWithGoogle()) {
                is AuthResult.Success -> {
                    _authState.update {
                        it.copy(
                            isSigningIn = false,
                            error = null
                        )
                    }
                }

                is AuthResult.Failure -> {
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

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
