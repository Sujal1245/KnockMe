package com.sujalkumar.knockme.ui.addalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant

class AddKnockAlertViewModel(
    private val knockAlertRepository: KnockAlertRepository,
    private val userDetailsRepository: UserDetailsRepository
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
            // Get current user from UserDetailsRepository
            val currentUser = userDetailsRepository.user.first()

            if (currentUser == null) {
                _uiState.value = AddKnockAlertUiState.Error("User not logged in.")
                return@launch
            }

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
                ownerId = currentUser.uid,
                content = _alertContent.value,
                targetTime = targetTime,
                knockedByUserIds = emptyList()
            )

            val result = knockAlertRepository.addKnockAlert(knockAlert)
            _uiState.value = result.fold(
                onSuccess = { AddKnockAlertUiState.Success },
                onFailure = { AddKnockAlertUiState.Error(it.message ?: "Failed to add alert.") }
            )
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
