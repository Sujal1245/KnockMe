package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.ui.model.AlertOwner
import com.sujalkumar.knockme.ui.model.DisplayableKnockAlert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    authRepository: AuthRepository,
    private val knockAlertRepository: KnockAlertRepository,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = null))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val ownersMap = MutableStateFlow<Map<String, AlertOwner?>>(emptyMap())
    private val observedOwnerIds = mutableSetOf<String>()

    private val user = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val alertsFlow = user
        .flatMapLatest { currentUser ->
            if (currentUser == null) {
                flowOf(emptyList())
            } else {
                knockAlertRepository.observeKnockAlerts()
                    .catch { emit(emptyList()) }
            }
        }

    init {
        combine(user, alertsFlow, ownersMap) { user, alerts, owners ->
            println(owners)

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
                        owner = owners[alert.ownerId]
                    )
                }
            )
        }
            .catch { /* ignore transient Firestore permission errors during auth warm-up */ }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)

        uiState
            .map {
                it.feedKnockAlerts.map { displayableKnockAlert ->
                    displayableKnockAlert.alert.ownerId
                }.distinct()
            }
            .onEach { ownerIds ->
                if (user.value == null) return@onEach

                ownerIds
                    .filterNot { observedOwnerIds.contains(it) }
                    .forEach { ownerId ->
                        observedOwnerIds.add(ownerId)
                        otherUsersRepository.observeUser(ownerId)
                            .onEach { owner ->
                                ownersMap.update { current ->
                                    current + (
                                        ownerId to owner?.let {
                                            AlertOwner(
                                                displayName = it.displayName,
                                                photoUrl = it.photoUrl
                                            )
                                        }
                                    )
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