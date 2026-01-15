package com.sujalkumar.knockme.data.mapper

import com.sujalkumar.knockme.data.model.FirebaseKnockAlert
import com.sujalkumar.knockme.domain.model.KnockAlert
import kotlin.time.Instant

fun FirebaseKnockAlert.toKnockAlert(): KnockAlert {
    return KnockAlert(
        id = id,
        ownerId = ownerId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(createdAtTimestamp),
        targetTime = Instant.fromEpochMilliseconds(targetTimestamp),
        knockedByUserIds = knockedByUids
    )
}

fun KnockAlert.toFirebaseKnockAlert(): FirebaseKnockAlert {
    return FirebaseKnockAlert(
        id = id,
        ownerId = ownerId,
        content = content,
        createdAtTimestamp = createdAt.toEpochMilliseconds(),
        targetTimestamp = targetTime.toEpochMilliseconds(),
        knockedByUids = knockedByUserIds
    )
}
