package com.sujalkumar.knockme.ui.mapper

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.model.UserProfileUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun User.toUserProfileUi(currentUserId: String?): UserProfileUi {
    val joinedText = createdAtMillis?.let { millis ->
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        "Joined ${formatter.format(Date(millis))}"
    } ?: ""

    return UserProfileUi(
        uid = uid,
        displayName = displayName,
        photoUrl = photoUrl,
        bio = "",
        joinedText = joinedText,
        isCurrentUser = uid == currentUserId
    )
}

