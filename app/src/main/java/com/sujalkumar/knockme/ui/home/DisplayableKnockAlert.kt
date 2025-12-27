package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.domain.model.KnockAlert

data class DisplayableKnockAlert(
    val alert: KnockAlert,
    val ownerDisplayName: String?,
    val hasKnocked: Boolean = false
)
