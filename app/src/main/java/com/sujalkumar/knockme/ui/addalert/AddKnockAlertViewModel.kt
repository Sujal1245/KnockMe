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

    private val _uiState = MutableStateFlow<AddKnockAlertUiState>(AddKnockAlertUiState.Idle)
    val uiState: StateFlow<AddKnockAlertUiState> = _uiState.asStateFlow()

    private val _alertContent = MutableStateFlow("")
    val alertContent: StateFlow<String> = _alertContent.asStateFlow()

    private val _targetTime = MutableStateFlow<Instant?>(null)
    val targetTime: StateFlow<Instant?> = _targetTime.asStateFlow()

    fun onAlertContentChanged(content: String) {
        _alertContent.value = content
    }

    fun onTargetTimeChanged(timeMillis: Long) {
        _targetTime.value = Instant.fromEpochMilliseconds(timeMillis)
    }

    fun addKnockAlert() {
        viewModelScope.launch {
            _uiState.value = AddKnockAlertUiState.Loading

            if (_alertContent.value.isBlank()) {
                _uiState.value = AddKnockAlertUiState.Error("Alert content cannot be empty.")
                return@launch
            }

            val targetTime = _targetTime.value
            if (targetTime == null || targetTime.toEpochMilliseconds() <= System.currentTimeMillis()) {
                _uiState.value = AddKnockAlertUiState.Error("Target time must be in the future.")
                return@launch
            }

            val knockAlert = KnockAlert(
                id = "",
                ownerId = "",
                content = _alertContent.value,
                targetTime = targetTime,
                knockedByUserIds = emptyList()
            )

            when (val result = knockAlertRepository.addKnockAlert(knockAlert)) {
                is KnockAlertResult.Success -> {
                    _uiState.value = AddKnockAlertUiState.Success
                }

                is KnockAlertResult.Failure -> {
                    _uiState.value = AddKnockAlertUiState.Error(
                        message = when (result.error) {
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
        _uiState.value = AddKnockAlertUiState.Idle
        _alertContent.value = ""
        _targetTime.value = null
    }
}

sealed interface AddKnockAlertUiState {
    object Idle : AddKnockAlertUiState
    object Loading : AddKnockAlertUiState
    object Success : AddKnockAlertUiState
    data class Error(val message: String) : AddKnockAlertUiState
}
