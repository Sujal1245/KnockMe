package com.sujalkumar.knockme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sujalkumar.knockme.ui.addalert.AddKnockAlertScreen
import com.sujalkumar.knockme.ui.home.HomeScreen
import com.sujalkumar.knockme.ui.onboarding.OnboardingScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingNavKey : NavKey

@Serializable
data object HomeNavKey : NavKey

@Serializable
data object AddKnockAlertNavKey : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(OnboardingNavKey)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is OnboardingNavKey -> {
                    NavEntry(key = key) {
                        OnboardingScreen(
                            onNavigateToHome = {
                                backStack[0] = HomeNavKey
                            }
                        )
                    }
                }

                is HomeNavKey -> {
                    NavEntry(key = key) {
                        HomeScreen(
                            onNavigateToAddAlert = {
                                backStack.add(AddKnockAlertNavKey)
                            },
                            onLogout = {
                                // Will fix this later.
//                                backStack[0] = OnboardingNavKey
                            }
                        )
                    }
                }

                is AddKnockAlertNavKey -> {
                    NavEntry(key = key) {
                        AddKnockAlertScreen(
                            onNavigateUp = {
                                backStack.remove(AddKnockAlertNavKey)
                            }
                        )
                    }
                }

                else -> throw RuntimeException("Invalid NavKey: $key")
            }
        }
    )
}
