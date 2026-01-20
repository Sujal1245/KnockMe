package com.sujalkumar.knockme.ui.addalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.usecase.AddKnockAlertUseCase
import com.sujalkumar.knockme.ui.common.UiText
import com.sujalkumar.knockme.ui.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class AddKnockAlertViewModel(
    private val addKnockAlertUseCase: AddKnockAlertUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddKnockAlertUiState())
    val uiState: StateFlow<AddKnockAlertUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<AddKnockAlertUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    fun onAlertContentChanged(content: String) {
        _uiState.update {
            it.copy(alertContent = content)
        }
    }

    fun onTargetTimeChanged(timeMillis: Long) {
        _uiState.update {
            it.copy(targetTime = Instant.fromEpochMilliseconds(timeMillis))
        }
    }

    fun addKnockAlert() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.alertContent.isBlank()) {
                _uiEvents.send(
                    AddKnockAlertUiEvent.ShowSnackbar(
                        UiText.StringResource(R.string.error_empty_alert_content)
                    )
                )
                return@launch
            }

            val targetTime = currentState.targetTime
            if (targetTime == null || targetTime.toEpochMilliseconds() <= System.currentTimeMillis()) {
                _uiEvents.send(
                    AddKnockAlertUiEvent.ShowSnackbar(
                        UiText.StringResource(R.string.error_invalid_target_time)
                    )
                )
                return@launch
            }

            _uiState.update {
                it.copy(isLoading = true)
            }

            val knockAlert = KnockAlert(
                id = "",
                ownerId = "",
                content = currentState.alertContent,
                createdAt = Instant.fromEpochMilliseconds(0),
                targetTime = targetTime,
                knockedByUserIds = emptyList()
            )

            when (val result = addKnockAlertUseCase(knockAlert)) {
                is KnockAlertResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    _uiEvents.send(AddKnockAlertUiEvent.AlertAdded)
                }

                is KnockAlertResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    _uiEvents.send(
                        AddKnockAlertUiEvent.ShowSnackbar(
                            result.error.toUiText()
                        )
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update {
            AddKnockAlertUiState()
        }
    }
}
