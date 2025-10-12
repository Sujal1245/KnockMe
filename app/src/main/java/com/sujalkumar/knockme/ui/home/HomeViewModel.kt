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
                        val hasKnocked = user.uid in alert.knockedByUids
                        feedKnockAlerts.add(
                            DisplayableKnockAlert(
                                alert = alert,
                                ownerDisplayName = null, // This can be enhanced later
                                hasKnocked = hasKnocked
                            )
                        )
                    }
                }
            } else {
                activeAlerts.forEach {
                    feedKnockAlerts.add(
                        DisplayableKnockAlert(
                            alert = it,
                            ownerDisplayName = null, // This can be enhanced later
                            hasKnocked = false
                        )
                    )
                }
            }
            HomeUiState.Success(
                user = user,
                myKnockAlerts = myKnockAlerts,
                feedKnockAlerts = feedKnockAlerts
            )
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
            val currentUser = userDetailsRepository.user.first()
            if (currentUser == null) {
                _uiState.value = HomeUiState.Error("You must be logged in to knock.")
                return@launch
            }

            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                val alertIndex = currentState.feedKnockAlerts.indexOfFirst { it.alert.id == alertId }

                if (alertIndex == -1) {
                    _uiState.value = HomeUiState.Error("Alert not found.")
                    return@launch
                }

                val displayableAlert = currentState.feedKnockAlerts[alertIndex]

                if (displayableAlert.alert.ownerId == currentUser.uid) {
                    _uiState.value = HomeUiState.Error("You cannot knock on your own alert.")
                    return@launch
                }

                // Optimistically update UI
                val updatedFeedKnockAlerts = currentState.feedKnockAlerts.toMutableList()
                updatedFeedKnockAlerts[alertIndex] = displayableAlert.copy(hasKnocked = true)
                _uiState.value = currentState.copy(feedKnockAlerts = updatedFeedKnockAlerts)

                val result = knockAlertRepository.knockOnAlert(alertId = alertId, userId = currentUser.uid)
                result.onFailure {
                    // Revert UI change on failure
                    updatedFeedKnockAlerts[alertIndex] = displayableAlert
                    _uiState.value = currentState.copy(feedKnockAlerts = updatedFeedKnockAlerts)
                    _uiState.value = HomeUiState.Error("Knock failed: ${it.message}")
                }
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
