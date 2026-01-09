package com.sujalkumar.knockme.ui.addalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertError
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class AddKnockAlertViewModel(
    private val knockAlertRepository: KnockAlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddKnockAlertUiState())
    val uiState: StateFlow<AddKnockAlertUiState> = _uiState.asStateFlow()

    fun onAlertContentChanged(content: String) {
        _uiState.value = _uiState.value.copy(
            alertContent = content,
            errorMessage = null
        )
    }

    fun onTargetTimeChanged(timeMillis: Long) {
        _uiState.value = _uiState.value.copy(
            targetTime = Instant.fromEpochMilliseconds(timeMillis),
            errorMessage = null
        )
    }

    fun addKnockAlert() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.alertContent.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Alert content cannot be empty."
                )
                return@launch
            }

            val targetTime = currentState.targetTime
            if (targetTime == null || targetTime.toEpochMilliseconds() <= System.currentTimeMillis()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Target time must be in the future."
                )
                return@launch
            }

            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            val knockAlert = KnockAlert(
                id = "",
                ownerId = "",
                content = currentState.alertContent,
                targetTime = targetTime,
                knockedByUserIds = emptyList()
            )

            when (val result = knockAlertRepository.addKnockAlert(knockAlert)) {
                is KnockAlertResult.Success -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }

                is KnockAlertResult.Failure -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = when (result.error) {
                            KnockAlertError.NotAuthenticated ->
                                "You must be signed in to create an alert."
                            KnockAlertError.Network ->
                                "Network error. Please try again."
                            else ->
                                "Failed to add alert."
                        }
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AddKnockAlertUiState()
    }
}
