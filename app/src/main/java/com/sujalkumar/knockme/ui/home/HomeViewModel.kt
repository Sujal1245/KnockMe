package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted

class HomeViewModel(
    userDetailsRepository: UserDetailsRepository,
    private val knockAlertRepository: KnockAlertRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(user = null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _oneTimeEvent = Channel<HomeOneTimeEvent>()
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()

    private val user = userDetailsRepository.user.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null,
    )

    init {
        val activeAlertsFlow: Flow<List<KnockAlert>> = knockAlertRepository.getActiveKnockAlerts()

        combine(user, activeAlertsFlow) { user, activeAlerts ->
            val (myKnockAlerts, feedKnockAlertsSource) = if (user != null) {
                activeAlerts.partition { it.ownerId == user.uid }
            } else {
                emptyList<KnockAlert>() to activeAlerts
            }

            val feedKnockAlerts = feedKnockAlertsSource.map { alert ->
                val owner = otherUsersRepository.getUserById(alert.ownerId)
                val hasKnocked = user?.uid in alert.knockedByUserIds
                DisplayableKnockAlert(
                    alert = alert,
                    ownerDisplayName = owner?.displayName,
                    hasKnocked = hasKnocked
                )
            }

            HomeUiState(
                user = user,
                myKnockAlerts = myKnockAlerts,
                feedKnockAlerts = feedKnockAlerts
            )
        }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }


    fun knockOnAlert(alertId: String) {
        viewModelScope.launch {
            val currentUser = user.value
            if (currentUser == null) {
                _oneTimeEvent.send(HomeOneTimeEvent.UserNotLoggedIn)
                return@launch
            }

            val currentState = _uiState.value
            val currentFeed = currentState.feedKnockAlerts
            val alertIndex = currentFeed.indexOfFirst { it.alert.id == alertId }

            if (alertIndex == -1) return@launch

            val displayableAlert = currentFeed[alertIndex]

            if (displayableAlert.alert.ownerId == currentUser.uid) return@launch

            // Optimistic update
            val updatedFeed = currentFeed.toMutableList().apply {
                this[alertIndex] = displayableAlert.copy(hasKnocked = true)
            }
            _uiState.value = currentState.copy(feedKnockAlerts = updatedFeed)

            val result = knockAlertRepository.knockOnAlert(
                alertId = alertId,
                userId = currentUser.uid
            )

            result.onFailure {
                val revertedFeed = currentFeed.toMutableList().apply {
                    this[alertIndex] = displayableAlert
                }
                _uiState.value = currentState.copy(feedKnockAlerts = revertedFeed)
            }
        }
    }

    fun onLogoutRequested() {
        viewModelScope.launch {
            _oneTimeEvent.send(HomeOneTimeEvent.LoggedOut)
        }
    }
}

sealed interface HomeOneTimeEvent {
    data object UserNotLoggedIn : HomeOneTimeEvent
    data object LoggedOut : HomeOneTimeEvent
}