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

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
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
                HomeScreen(
                    onNavigateToAddAlert = {
                        mainBackStack.add(Route.Main.AddKnockAlert)
                    },
                    onLogout = onLogout
                )
            }

            entry<Route.Main.AddKnockAlert> {
                AddKnockAlertScreen(
                    onNavigateUp = {
                        mainBackStack.remove(Route.Main.AddKnockAlert)
                    }
                )
            }
        }
    )
}