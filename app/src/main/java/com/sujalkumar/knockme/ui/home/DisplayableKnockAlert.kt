package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.domain.model.KnockAlert

data class DisplayableKnockAlert(
    val alert: KnockAlert,
    val ownerDisplayName: String?
) {
    fun hasKnocked(currentUserId: String?): Boolean =
        currentUserId != null && currentUserId in alert.knockedByUserIds
}
