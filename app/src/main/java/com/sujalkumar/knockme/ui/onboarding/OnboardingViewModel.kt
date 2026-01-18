package com.sujalkumar.knockme.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, error = null) }

            when (val result = signInWithGoogleUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isSigningIn = false) }
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
