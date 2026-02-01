package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.model.FeedKnockAlertUi
import com.sujalkumar.knockme.ui.model.MyKnockAlertUi

data class HomeUiState(
    val user: User?,
    val myKnockAlerts: List<MyKnockAlertUi> = emptyList(),
    val feedKnockAlerts: List<FeedKnockAlertUi> = emptyList(),
    val isLoading: Boolean = true
)
