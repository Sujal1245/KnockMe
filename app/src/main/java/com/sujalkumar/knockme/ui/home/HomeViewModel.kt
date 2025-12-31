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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(
    authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val ownersMap = MutableStateFlow<Map<String, String?>>(emptyMap())

    private val user = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    init {
        val alertsFlow = knockAlertRepository.observeKnockAlerts()

        combine(user, alertsFlow, ownersMap) { user, alerts, owners ->
            val previousOwnerNames = _uiState.value.feedKnockAlerts
                .associateBy(
                    keySelector = { it.alert.ownerId },
                    valueTransform = { it.ownerDisplayName }
                )

            val now = System.currentTimeMillis()

            val activeAlerts = alerts.filter {
                it.targetTime.toEpochMilliseconds() <= now
            }

            val (myKnockAlerts, feedKnockAlertsSource) = if (user != null) {
                activeAlerts.partition { it.ownerId == user.uid }
            } else {
                emptyList<KnockAlert>() to activeAlerts
            }

            HomeUiState(
                user = user,
                myKnockAlerts = myKnockAlerts,
                feedKnockAlerts = feedKnockAlertsSource.map { alert ->
                    DisplayableKnockAlert(
                        alert = alert,
                        ownerDisplayName = owners[alert.ownerId]
                            ?: previousOwnerNames[alert.ownerId]
                    )
                }
            )
        }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)

        uiState
            .map {
                it.feedKnockAlerts.map { displayableKnockAlert ->
                    displayableKnockAlert.alert.ownerId
                }
                    .distinct()
            }
            .onEach { ownerIds ->
                ownerIds.forEach { ownerId ->
                    otherUsersRepository.observeUser(ownerId)
                        .onEach { owner ->
                            ownersMap.update { current ->
                                current + (ownerId to owner?.displayName)
                            }
                        }
                        .launchIn(viewModelScope)
                }
            }
            .launchIn(viewModelScope)
    }


    fun knockOnAlert(alertId: String) {
        viewModelScope.launch {
            val currentFeed = _uiState.value.feedKnockAlerts
            val alertIndex = currentFeed.indexOfFirst { it.alert.id == alertId }
            if (alertIndex == -1) return@launch

            val displayableAlert = currentFeed[alertIndex]

            if (displayableAlert.hasKnocked(_uiState.value.user?.uid)) return@launch

            // Optimistic update
            val updatedFeed = currentFeed.toMutableList().apply {
                val updatedAlert = displayableAlert.alert.copy(
                    knockedByUserIds = displayableAlert.alert.knockedByUserIds + _uiState.value.user!!.uid
                )
                this[alertIndex] = displayableAlert.copy(alert = updatedAlert)
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