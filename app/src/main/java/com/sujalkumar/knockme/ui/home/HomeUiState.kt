package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.User

data class HomeUiState(
    val user: User?,
    val myKnockAlerts: List<KnockAlert> = emptyList(),
    val feedKnockAlerts: List<DisplayableKnockAlert> = emptyList()
)
