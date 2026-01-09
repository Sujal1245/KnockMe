package com.sujalkumar.knockme.ui.addalert

import kotlin.time.Instant

data class AddKnockAlertUiState(
    val alertContent: String = "",
    val targetTime: Instant? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
