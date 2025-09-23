package com.sujalkumar.knockme.data.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class KnockAlert(
    val owner: AppUser? = null,
    val content: String = "",
    val timestamp: Instant? = null // Added timestamp
)