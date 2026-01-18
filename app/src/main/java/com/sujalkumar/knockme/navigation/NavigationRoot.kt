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
import com.sujalkumar.knockme.domain.repository.AuthRepository
import org.koin.compose.koinInject

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val authRepository: AuthRepository = koinInject()
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack()

    LaunchedEffect(currentUser) {
        val targetRoute = if (currentUser != null) {
            Route.Main
        } else {
            Route.Auth
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
                entry<Route.Auth> {
                    AuthNavigation()
                }

                entry<Route.Main> {
                    MainNavigation()
                }
            }
        )
    } else {
        // Intentionally empty – system SplashScreen is still visible
    }
}
