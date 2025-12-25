package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.model.KnockAlert

data class HomeUiState(
    val user: AppUser?,
    val myKnockAlerts: List<KnockAlert> = emptyList(),
    val feedKnockAlerts: List<DisplayableKnockAlert> = emptyList()
)
