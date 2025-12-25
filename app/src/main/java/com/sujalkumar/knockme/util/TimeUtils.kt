package com.sujalkumar.knockme.util

import kotlin.time.Duration.Companion.milliseconds

object TimeUtils {

    fun toRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val duration = (now - timestamp).milliseconds

        return when {
            duration.inWholeSeconds < 60 -> "just now"
            duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes} minute${if (duration.inWholeMinutes > 1) "s" else ""} ago"
            duration.inWholeHours < 24 -> "${duration.inWholeHours} hour${if (duration.inWholeHours > 1) "s" else ""} ago"
            duration.inWholeDays < 7 -> "${duration.inWholeDays} day${if (duration.inWholeDays > 1) "s" else ""} ago"
            else -> {
                val weeks = duration.inWholeDays / 7
                "${weeks} week${if (weeks > 1) "s" else ""} ago"
            }
        }
    }
}
