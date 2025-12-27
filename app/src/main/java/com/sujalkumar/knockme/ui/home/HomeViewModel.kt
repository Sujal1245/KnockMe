package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class HomeViewModel(
    authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(user = null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val user = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    init {
        val alertsFlow = knockAlertRepository.observeKnockAlerts()

        combine(user, alertsFlow) { user, alerts ->
            val now = System.currentTimeMillis()

            val activeAlerts = alerts.filter {
                it.targetTime.toEpochMilliseconds() <= now
            }

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
            val currentFeed = _uiState.value.feedKnockAlerts
            val alertIndex = currentFeed.indexOfFirst { it.alert.id == alertId }
            if (alertIndex == -1) return@launch

            val displayableAlert = currentFeed[alertIndex]

            if (displayableAlert.hasKnocked) return@launch

            // Optimistic update
            val updatedFeed = currentFeed.toMutableList().apply {
                this[alertIndex] = displayableAlert.copy(hasKnocked = true)
            }
            _uiState.value = _uiState.value.copy(feedKnockAlerts = updatedFeed)

            when (knockAlertRepository.knockOnAlert(alertId)) {
                is KnockAlertResult.Success -> Unit
                is KnockAlertResult.Failure -> {
                    // revert optimistic update
                    val revertedFeed = currentFeed.toMutableList().apply {
                        this[alertIndex] = displayableAlert
                    }
                    _uiState.value = _uiState.value.copy(feedKnockAlerts = revertedFeed)
                }
            }
        }
    }

}