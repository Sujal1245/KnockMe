package com.sujalkumar.knockme.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.AuthError
import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.usecase.SignInWithGoogleUseCase
import com.sujalkumar.knockme.ui.mapper.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class OnboardingViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = Channel<OnboardingUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true) }

            when (val result = signInWithGoogleUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isSigningIn = false) }
                    _uiEvents.send(OnboardingUiEvent.SignedIn)
                }

                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isSigningIn = false) }
                    _uiEvents.send(
                        OnboardingUiEvent.ShowSnackbar(
                            message = result.error.toUiMessage()
                        )
                    )
                }
            }
        }
    }
}
