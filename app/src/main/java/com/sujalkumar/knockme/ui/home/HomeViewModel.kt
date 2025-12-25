package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.common.Resource
import com.sujalkumar.knockme.data.model.KnockAlert
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    userDetailsRepository: UserDetailsRepository,
    private val knockAlertRepository: KnockAlertRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<Resource<HomeUiState>>(Resource.Loading)
    val uiState: StateFlow<Resource<HomeUiState>> = _uiState.asStateFlow()

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

            val deferredFeedKnockAlerts = feedKnockAlertsSource.map { alert ->
                viewModelScope.async { // Asynchronously fetch owner display name
                    val owner = otherUsersRepository.getUserById(alert.ownerId)
                    val hasKnocked = user?.uid in alert.knockedByUids
                    DisplayableKnockAlert(
                        alert = alert,
                        ownerDisplayName = owner?.displayName,
                        hasKnocked = hasKnocked
                    )
                }
            }

            HomeUiState(
                user = user,
                myKnockAlerts = myKnockAlerts,
                feedKnockAlerts = deferredFeedKnockAlerts.awaitAll() // Wait for all fetches to complete
            )
        }
            .catch { e ->
                _uiState.value = Resource.Error(e.message ?: "An error occurred loading home data", e)
            }
            .onEach { successState ->
                _uiState.value = Resource.Success(successState)
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
            if (currentState is Resource.Success) {
                val currentFeed = currentState.data.feedKnockAlerts
                val alertIndex = currentFeed.indexOfFirst { it.alert.id == alertId }

                if (alertIndex == -1) {
                    return@launch // Alert not found
                }

                val displayableAlert = currentFeed[alertIndex]

                if (displayableAlert.alert.ownerId == currentUser.uid) {
                    return@launch // User cannot knock on their own alert
                }

                // Optimistically update UI
                val updatedFeed = currentFeed.toMutableList().apply {
                    this[alertIndex] = displayableAlert.copy(hasKnocked = true)
                }
                _uiState.value = Resource.Success(currentState.data.copy(feedKnockAlerts = updatedFeed))

                // Perform the knock
                val result = knockAlertRepository.knockOnAlert(alertId = alertId, userId = currentUser.uid)

                // Revert UI on failure
                result.onFailure {
                    val revertedFeed = currentFeed.toMutableList().apply {
                        this[alertIndex] = displayableAlert
                    }
                    _uiState.value = Resource.Success(currentState.data.copy(feedKnockAlerts = revertedFeed))
                }
            }
        }
    }
}

sealed interface HomeOneTimeEvent {
    data object UserNotLoggedIn : HomeOneTimeEvent
}