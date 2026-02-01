package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.KnockAlertResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.usecase.KnockOnAlertUseCase
import com.sujalkumar.knockme.domain.usecase.ObserveFeedAlertsUseCase
import com.sujalkumar.knockme.domain.usecase.ObserveMyAlertsUseCase
import com.sujalkumar.knockme.domain.usecase.SignOutUseCase
import com.sujalkumar.knockme.ui.common.UiText
import com.sujalkumar.knockme.ui.mapper.toUiText
import com.sujalkumar.knockme.ui.model.FeedKnockAlertUi
import com.sujalkumar.knockme.ui.model.MyKnockAlertUi
import com.sujalkumar.knockme.ui.model.ProfileUi
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
    private val profilesMap = MutableStateFlow<Map<String, ProfileUi>>(emptyMap())
    private val observedProfileIds = mutableSetOf<String>()

    private val _uiEvents = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1.seconds)
        }
    }.onStart { emit(System.currentTimeMillis()) }

    private val feedAlertsStateFlow = observeFeedAlertsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val myAlertsStateFlow = observeMyAlertsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<HomeUiState> =
        combine(
            authRepository.currentUser,
            feedAlertsStateFlow,
            myAlertsStateFlow,
            profilesMap,
            combine(loadingState, ticker) { loading, now -> loading to now }
        ) { user, feedAlerts, myAlerts, profiles, loadingAndNow ->
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
                    isActive = isActive,
                    knockers = alert.knockedByUserIds.mapNotNull { profiles[it] }
                )
            }

            HomeUiState(
                user = user,
                myKnockAlerts = myAlertsUi,
                feedKnockAlerts = readyFeedAlerts.map { alert ->
                    FeedKnockAlertUi(
                        alert = alert,
                        owner = profiles[alert.ownerId]
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

        feedAlertsStateFlow
            .map { alerts ->
                alerts.map {
                    it.ownerId
                }.distinct()
            }
            .distinctUntilChanged()
            .onEach { ownerIds ->
                ownerIds.forEach(::observeProfile)
            }
            .launchIn(viewModelScope)

        myAlertsStateFlow
            .map { alerts ->
                alerts.flatMap {
                    it.knockedByUserIds
                }.distinct()
            }
            .distinctUntilChanged()
            .onEach { knockerIds ->
                knockerIds.forEach(::observeProfile)
            }
            .launchIn(viewModelScope)
    }


    private fun observeProfile(userId: String) {
        if (!observedProfileIds.add(userId)) return

        otherUsersRepository.observeUser(userId)
            .onEach { user ->
                user?.let {
                    profilesMap.update { current ->
                        current + (userId to ProfileUi(
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
            when (val result = knockOnAlertUseCase(alertId)) {
                is KnockAlertResult.Success -> {
                    // No-op: UI will update reactively via flows
                }

                is KnockAlertResult.Failure -> {
                    _uiEvents.send(
                        HomeUiEvent.ShowSnackbar(
                            message = result.error.toUiText()
                        )
                    )
                }
            }
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
                        message =
                            error.message?.let {
                                UiText.DynamicString(it)
                            } ?: UiText.StringResource(R.string.sign_out_fallback_error)
                    )
                )
            }
        }
    }
}
