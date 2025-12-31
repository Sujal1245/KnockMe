package com.sujalkumar.knockme.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Auth : Route {

        @Serializable
        data object Onboarding : NavKey

    }

    @Serializable
    data object Main : Route {

        @Serializable
        data object Home : NavKey

        @Serializable
        data object AddKnockAlert : NavKey

    }
}