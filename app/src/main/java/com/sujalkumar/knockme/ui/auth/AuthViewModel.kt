package com.sujalkumar.knockme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val user = authRepository.getSignedInUser()
        if (user != null) {
            _authState.update { it.copy(isSignInSuccessful = true) }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            val result = authRepository.googleSignIn()

            result.data?.let {
                userDetailsRepository.setUserDetails(it)
            }

            _authState.update {
                it.copy(
                    isSignInSuccessful = result.data != null,
                    signInError = result.errorMessage
                )
            }


        }
    }

    fun resetState() {
        _authState.update { AuthState() }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.update { it.copy(isSignInSuccessful = false) }
        }
    }
}
