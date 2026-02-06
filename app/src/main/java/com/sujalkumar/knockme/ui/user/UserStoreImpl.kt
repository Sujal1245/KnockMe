package com.sujalkumar.knockme.ui.user

import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class UserStoreImpl(
    private val otherUsersRepository: OtherUsersRepository,
    private val scope: CoroutineScope
) : UserStore {

    private val observedUserIds = mutableSetOf<String>()

    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    override val users: StateFlow<Map<String, User>> = _users.asStateFlow()

    override fun ensureUserObserved(userId: String) {
        if (!observedUserIds.add(userId)) return

        otherUsersRepository.observeUser(userId)
            .onEach { user ->
                user?.let {
                    _users.update { current ->
                        current + (userId to it)
                    }
                }
            }
            .launchIn(scope)
    }
}