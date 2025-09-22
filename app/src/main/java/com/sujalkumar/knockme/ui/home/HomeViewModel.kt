package com.sujalkumar.knockme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    userDetailsRepository: UserDetailsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        userDetailsRepository.user
            .onEach { appUser ->
                _uiState.value = HomeUiState.Success(appUser)
            }
            .catch { 
                _uiState.value = HomeUiState.Error 
            }
            .launchIn(viewModelScope)
    }
}

sealed interface HomeUiState {
    data class Success(val user: AppUser?) : HomeUiState
    data object Error : HomeUiState
    data object Loading : HomeUiState
}
