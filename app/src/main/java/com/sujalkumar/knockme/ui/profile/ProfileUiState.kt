package com.sujalkumar.knockme.ui.profile

import com.sujalkumar.knockme.ui.model.UserProfileUi

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfileUi? = null
)
