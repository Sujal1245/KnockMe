package com.sujalkumar.knockme.ui.model

import com.sujalkumar.knockme.domain.model.KnockAlert

data class FeedKnockAlertUi(
    val alert: KnockAlert,
    val owner: ProfileUi?
) {
    fun hasKnocked(currentUserId: String?): Boolean =
        currentUserId != null && currentUserId in alert.knockedByUserIds
}
