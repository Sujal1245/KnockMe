package com.sujalkumar.knockme.ui.model

import com.sujalkumar.knockme.domain.model.KnockAlert

data class MyKnockAlertUi(
    val alert: KnockAlert,
    val progress: Float,
    val isActive: Boolean,
    val knockers: List<ProfileUi> = emptyList()
)
