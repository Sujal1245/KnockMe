package com.sujalkumar.knockme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sujalkumar.knockme.ui.addalert.AddKnockAlertScreen
import com.sujalkumar.knockme.ui.home.HomeScreen
import com.sujalkumar.knockme.ui.onboarding.OnboardingScreen

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Route.Onboarding)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Onboarding> {
                OnboardingScreen(
                    onNavigateToHome = {
                        backStack[0] = Route.Home
                    }
                )
            }

            entry<Route.Home> {
                HomeScreen(
                    onNavigateToAddAlert = {
                        backStack.add(Route.AddKnockAlert)
                    },
                    onLogout = {
                        // Will fix this later.
//                                backStack[0] = OnboardingNavKey
                    }
                )
            }

            entry<Route.AddKnockAlert> {
                AddKnockAlertScreen(
                    onNavigateUp = {
                        backStack.remove(Route.AddKnockAlert)
                    }
                )
            }
        },
//        entryProvider = { key ->
//            when (key) {
//                is Route.Onboarding -> {
//                    NavEntry(key = key) {
//                        OnboardingScreen(
//                            onNavigateToHome = {
//                                backStack[0] = Route.Home
//                            }
//                        )
//                    }
//                }
//
//                is Route.Home -> {
//                    NavEntry(key = key) {
//                        HomeScreen(
//                            onNavigateToAddAlert = {
//                                backStack.add(Route.AddKnockAlert)
//                            },
//                            onLogout = {
//                                // Will fix this later.
////                                backStack[0] = OnboardingNavKey
//                            }
//                        )
//                    }
//                }
//
//                is Route.AddKnockAlert -> {
//                    NavEntry(key = key) {
//                        AddKnockAlertScreen(
//                            onNavigateUp = {
//                                backStack.remove(Route.AddKnockAlert)
//                            }
//                        )
//                    }
//                }
//
//                else -> error("Unknown NavKey: $key")
//            }
//        }
    )
}
