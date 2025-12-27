package com.sujalkumar.knockme.domain.model

import kotlin.time.Instant

data class KnockAlert(
    val id: String,
    val ownerId: String,
    val content: String,
    val targetTime: Instant,
    val knockedByUserIds: List<String>
)
