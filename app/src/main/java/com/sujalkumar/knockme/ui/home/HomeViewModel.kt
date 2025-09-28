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
    private val userDetailsRepository: UserDetailsRepository,
    private val knockAlertRepository: KnockAlertRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val userFlow: Flow<AppUser?> = userDetailsRepository.user
        val activeAlertsFlow: Flow<List<KnockAlert>> = knockAlertRepository.getActiveKnockAlerts()

        combine(userFlow, activeAlertsFlow) { user, activeAlerts ->
            val myKnockAlerts = mutableListOf<KnockAlert>()
            val feedKnockAlerts = mutableListOf<DisplayableKnockAlert>()

            if (user != null) {
                activeAlerts.forEach { alert ->
                    if (alert.ownerId == user.uid) {
                        myKnockAlerts.add(alert)
                    } else {
                        // For now, ownerDisplayName is null. This can be enhanced later
                        // if a mechanism to fetch other users' display names is available.
                        feedKnockAlerts.add(DisplayableKnockAlert(alert = alert, ownerDisplayName = null))
                    }
                }
            } else {
                // If no user is logged in, all alerts are treated as feed alerts.
                // And none are owned by the "current" (non-existent) user.
                activeAlerts.forEach {
                    feedKnockAlerts.add(DisplayableKnockAlert(alert = it, ownerDisplayName = null))
                }
            }
            HomeUiState.Success(user = user, myKnockAlerts = myKnockAlerts, feedKnockAlerts = feedKnockAlerts)
        }
        .catch { e -> 
            _uiState.value = HomeUiState.Error(e.message ?: "An error occurred loading home data") 
        }
        .onEach { successState -> // This should correctly assign any state, including the new Success state
            _uiState.value = successState
        }
        .launchIn(viewModelScope)
    }

    fun knockOnAlert(alertId: String) {
        viewModelScope.launch {
            val currentUser = userDetailsRepository.user.first()
            if (currentUser == null) {
                println("User not logged in, cannot knock.")
                // Optionally update UI: _uiState.value = HomeUiState.Error("You must be logged in to knock")
                return@launch
            }

            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                // The alert being knocked should be in feedKnockAlerts and not owned by the current user.
                val alertToKnock = currentState.feedKnockAlerts.find { it.alert.id == alertId }?.alert

                if (alertToKnock == null) {
                    println("Alert with ID $alertId not found in feed or user attempted to knock on own alert.")
                    // Optionally update UI: _uiState.value = HomeUiState.Error("Alert not found or action not allowed.")
                    return@launch
                }

                // Safeguard: Ensure the user is not knocking their own alert, even if it ended up in feedKnockAlerts.
                if (alertToKnock.ownerId == currentUser.uid) {
                    println("User ${currentUser.uid} cannot knock on their own alert $alertId (safeguard check)." )
                    // Optionally update UI: _uiState.value = HomeUiState.Error("You cannot knock on your own alert.")
                    return@launch
                }

                val result = knockAlertRepository.knockOnAlert(alertId = alertId, userId = currentUser.uid)
                result.onFailure {
                    println("Failed to knock on alert $alertId: ${it.message}")
                    // Example: _uiState.value = HomeUiState.Error("Knock failed: ${it.message}")
                }
                // On success, Firestore listener in getActiveKnockAlerts should update the list if knockedByUids changes behavior
                // This will trigger the combine block again and refresh myKnockAlerts and feedKnockAlerts.
            } else {
                println("Cannot knock: HomeUiState is not Success. Current state: $currentState")
            }
        }
    }
}

sealed interface HomeUiState {
    data class Success(
        val user: AppUser?,
        val myKnockAlerts: List<KnockAlert> = emptyList(),
        val feedKnockAlerts: List<DisplayableKnockAlert> = emptyList()
    ) : HomeUiState
    data class Error(val message: String = "An error occurred.") : HomeUiState 
    data object Loading : HomeUiState
}
