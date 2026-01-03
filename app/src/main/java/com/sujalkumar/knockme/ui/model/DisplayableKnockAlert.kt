package com.sujalkumar.knockme.ui.model

import com.sujalkumar.knockme.domain.model.KnockAlert

data class DisplayableKnockAlert(
    val alert: KnockAlert,
    val owner: AlertOwner?
) {
    fun hasKnocked(currentUserId: String?): Boolean =
        currentUserId != null && currentUserId in alert.knockedByUserIds
}