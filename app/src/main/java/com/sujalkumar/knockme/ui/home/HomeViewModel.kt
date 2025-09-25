package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.model.KnockAlert
import com.sujalkumar.knockme.data.repository.KnockAlertRepository
import com.sujalkumar.knockme.data.repository.UserDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userDetailsRepository: UserDetailsRepository, // Changed to private val
    private val knockAlertRepository: KnockAlertRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val userFlow: Flow<AppUser?> = userDetailsRepository.user
        val activeAlertsFlow: Flow<List<KnockAlert>> = knockAlertRepository.getActiveKnockAlerts()

        combine(userFlow, activeAlertsFlow) { user, activeAlerts ->
            // Filter alerts: show all active alerts, but identify which are owned by the current user
            // For now, Success state just passes all active alerts.
            // UI can differentiate or we can refine state later if needed.
            HomeUiState.Success(user = user, knockAlerts = activeAlerts)
        }
        .catch { e -> 
            _uiState.value = HomeUiState.Error(e.message ?: "An error occurred loading home data") 
        }
        .onEach { successState ->
            _uiState.value = successState
        }
        .launchIn(viewModelScope)
    }

    fun knockOnAlert(alertId: String) {
        viewModelScope.launch {
            val currentUser = userDetailsRepository.user.first() // Get AppUser via UserDetailsRepository
            if (currentUser != null) {
                val result = knockAlertRepository.knockOnAlert(alertId = alertId, userId = currentUser.uid)
                result.onFailure {
                    // Optionally update UI with a message, e.g., via a temporary error Flow/State
                    println("Failed to knock on alert $alertId: ${it.message}")
                    // Example: _uiState.value = HomeUiState.Error("Knock failed: ${it.message}")
                }
                // On success, Firestore listener in getActiveKnockAlerts should update the list if knockedByUids changes behavior
            } else {
                println("User not logged in, cannot knock.")
                // Example: _uiState.value = HomeUiState.Error("You must be logged in to knock")
            }
        }
    }
}

sealed interface HomeUiState {
    data class Success(
        val user: AppUser?,
        val knockAlerts: List<KnockAlert> = emptyList()
    ) : HomeUiState
    data class Error(val message: String = "An error occurred.") : HomeUiState 
    data object Loading : HomeUiState
}
