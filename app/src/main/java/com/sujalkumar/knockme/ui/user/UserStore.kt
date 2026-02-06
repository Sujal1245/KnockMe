package com.sujalkumar.knockme.ui.user

import com.sujalkumar.knockme.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserStore {

    /**
     * Map of userId -> User
     * Shared, in-memory cache of observed users.
     */
    val users: StateFlow<Map<String, User>>

    /**
     * Ensures that the given userId is being observed.
     * Calling this multiple times for the same userId is safe.
     */
    fun ensureUserObserved(userId: String)
}
