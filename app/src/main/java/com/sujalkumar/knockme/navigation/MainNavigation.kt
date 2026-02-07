package com.sujalkumar.knockme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sujalkumar.knockme.ui.addalert.AddKnockAlertRoute
import com.sujalkumar.knockme.ui.home.HomeRoute
import com.sujalkumar.knockme.ui.profile.ProfileRoute
import com.sujalkumar.knockme.ui.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier
) {
    val mainBackStack = rememberNavBackStack(Route.Main.Home)
    NavDisplay(
        modifier = modifier,
        backStack = mainBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Main.Home> {
                HomeRoute(
                    onNavigateToAddAlert = {
                        mainBackStack.add(Route.Main.AddKnockAlert)
                    },
                    onNavigateToProfile = { userId ->
                        mainBackStack.add(Route.Main.Profile(userId))
                    }
                )
            }

            entry<Route.Main.AddKnockAlert> {
                AddKnockAlertRoute(
                    onNavigateUp = {
                        mainBackStack.remove(Route.Main.AddKnockAlert)
                    }
                )
            }

            entry<Route.Main.Profile> { route ->
                val viewModel = koinViewModel<ProfileViewModel>(
                    parameters = { parametersOf(route.userId) }
                )

                ProfileRoute(
                    viewModel = viewModel,
                    onNavigateUp = {
                        mainBackStack.remove(route)
                    },
                    onEditProfileClick = { }
                )
            }
        }
    )
}