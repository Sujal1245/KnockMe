package com.sujalkumar.knockme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sujalkumar.knockme.ui.addalert.AddKnockAlertScreen
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import com.sujalkumar.knockme.ui.home.HomeScreen
import com.sujalkumar.knockme.ui.onboarding.OnboardingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack()

    LaunchedEffect(
        authState.isCheckingSession,
        authState.isSignedIn
    ) {
        if (authState.isCheckingSession) return@LaunchedEffect

        val targetRoute = if (authState.isSignedIn) {
            Route.Home
        } else {
            Route.Onboarding
        }

        if (backStack.isEmpty() || backStack[0] != targetRoute) {
            backStack.clear()
            backStack.add(targetRoute)
        }
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Route.Onboarding> {
                    OnboardingScreen()
                }

                entry<Route.Home> {
                    HomeScreen(
                        onNavigateToAddAlert = {
                            backStack.add(Route.AddKnockAlert)
                        },
                        onLogout = {
                            authViewModel.signOut()
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
            }
        )
    } else {
        // Intentionally empty – system SplashScreen is still visible
    }
}
