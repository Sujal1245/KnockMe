package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.model.DisplayableKnockAlert
import com.sujalkumar.knockme.ui.model.MyKnockAlertUi

data class HomeUiState(
    val user: User?,
    val myKnockAlerts: List<MyKnockAlertUi> = emptyList(),
    val feedKnockAlerts: List<DisplayableKnockAlert> = emptyList(),
    val isLoading: Boolean = true
)
