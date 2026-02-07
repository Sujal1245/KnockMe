package com.sujalkumar.knockme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.ui.mapper.toUserProfileUi
import com.sujalkumar.knockme.ui.user.UserStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    userStore: UserStore,
    private val userId: String,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = userStore.users
        .map { users ->
            val user = users[userId]
            if (user == null) {
                ProfileUiState(isLoading = true)
            } else {
                ProfileUiState(
                    isLoading = false,
                    profile = user.toUserProfileUi(
                        currentUserId = authRepository.currentUser.value?.uid
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        userStore.ensureUserObserved(userId)
    }
}
