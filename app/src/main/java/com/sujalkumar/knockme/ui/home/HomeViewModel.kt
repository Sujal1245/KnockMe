package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.usecase.KnockOnAlertUseCase
import com.sujalkumar.knockme.domain.usecase.ObserveFeedAlertsUseCase
import com.sujalkumar.knockme.domain.usecase.ObserveMyAlertsUseCase
import com.sujalkumar.knockme.domain.usecase.SignOutUseCase
import com.sujalkumar.knockme.ui.model.AlertOwner
import com.sujalkumar.knockme.ui.model.DisplayableKnockAlert
import com.sujalkumar.knockme.ui.model.MyKnockAlertUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    authRepository: AuthRepository,
    private val signOutUseCase: SignOutUseCase,
    observeFeedAlertsUseCase: ObserveFeedAlertsUseCase,
    observeMyAlertsUseCase: ObserveMyAlertsUseCase,
    private val knockOnAlertUseCase: KnockOnAlertUseCase,
    private val otherUsersRepository: OtherUsersRepository
) : ViewModel() {

    private val loadingState = MutableStateFlow(true)
    private val ownersMap = MutableStateFlow<Map<String, AlertOwner>>(emptyMap())
    private val observedOwnerIds = mutableSetOf<String>()

    private val _uiEvents = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1.seconds)
        }
    }.onStart { emit(System.currentTimeMillis()) }

    private val feedAlertsState = observeFeedAlertsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val myAlertsState = observeMyAlertsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<HomeUiState> =
        combine(
            authRepository.currentUser,
            feedAlertsState,
            myAlertsState,
            ownersMap,
            combine(loadingState, ticker) { loading, now -> loading to now }
        ) { user, feedAlerts, myAlerts, owners, loadingAndNow ->
            val (isLoading, now) = loadingAndNow

            val readyFeedAlerts = feedAlerts.filter {
                it.targetTime.toEpochMilliseconds() <= now
            }

            val myAlertsUi = myAlerts.map { alert ->
                val createdAtMillis = alert.createdAt.toEpochMilliseconds()
                val targetMillis = alert.targetTime.toEpochMilliseconds()

                val totalDuration = targetMillis - createdAtMillis
                val elapsed = now - createdAtMillis

                val progress = if (totalDuration > 0)
                    (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                else 1f

                val isActive = targetMillis <= now

                MyKnockAlertUi(
                    alert = alert,
                    progress = progress,
                    isActive = isActive
                )
            }

            HomeUiState(
                user = user,
                myKnockAlerts = myAlertsUi,
                feedKnockAlerts = readyFeedAlerts.map { alert ->
                    DisplayableKnockAlert(
                        alert = alert,
                        owner = owners[alert.ownerId]
                    )
                },
                isLoading = isLoading
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(
                    user = null,
                    isLoading = true
                )
            )

    init {
        viewModelScope.launch {
            delay(3.seconds)
            loadingState.value = false
        }

        feedAlertsState
            .map { alerts -> alerts.map { it.ownerId }.distinct() }
            .distinctUntilChanged()
            .onEach { ownerIds ->
                ownerIds
                    .filterNot { observedOwnerIds.contains(it) }
                    .forEach { ownerId ->
                        observedOwnerIds.add(ownerId)
                        observeOwnerProfile(ownerId)
                    }
            }
            .launchIn(viewModelScope)
    }

    private fun observeOwnerProfile(ownerId: String) {
        otherUsersRepository.observeUser(ownerId)
            .onEach { user ->
                user?.let {
                    ownersMap.update { current ->
                        current + (ownerId to AlertOwner(
                            displayName = it.displayName,
                            photoUrl = it.photoUrl
                        ))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun knockOnAlert(alertId: String) {
        viewModelScope.launch {
            knockOnAlertUseCase(alertId)
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            loadingState.value = true

            runCatching {
                signOutUseCase()
            }.onFailure { error ->
                loadingState.value = false
                _uiEvents.send(
                    HomeUiEvent.ShowSnackbar(
                        message = error.message ?: "Failed to sign out"
                    )
                )
            }
        }
    }
}
