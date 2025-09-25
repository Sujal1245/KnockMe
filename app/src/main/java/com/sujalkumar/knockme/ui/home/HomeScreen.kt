package com.sujalkumar.knockme.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.data.model.KnockAlert
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    onNavigateToAddAlert: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onKnockAction = { alert -> homeViewModel.knockOnAlert(alert.id) }, 
        onSignOut = { authViewModel.signOut() },
        onNavigateToAddAlert = onNavigateToAddAlert,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onKnockAction: (KnockAlert) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAddAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("KnockMe") }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(), // Changed from modifier.fillMaxSize()
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(), // Changed from modifier.fillMaxSize()
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is HomeUiState.Success -> {
                    if (state.user != null) {
                        HomeScreenSuccessContent(
                            user = state.user,
                            knockAlerts = state.knockAlerts, 
                            onSignOut = onSignOut,
                            onKnockAction = onKnockAction,
                            onNavigateToAddAlert = onNavigateToAddAlert,
                            modifier = Modifier // Pass a new Modifier if needed, or rely on default
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(), // Changed from modifier.fillMaxSize()
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No user is currently logged in.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeScreenSuccessContent(
    user: AppUser,
    knockAlerts: List<KnockAlert>,
    onSignOut: () -> Unit,
    onKnockAction: (KnockAlert) -> Unit,
    onNavigateToAddAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User greeting",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Hello, ${user.displayName?.takeIf { it.isNotBlank() } ?: "User"}!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Active KnockAlerts",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedCard(
                    onClick = onNavigateToAddAlert,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            modifier = Modifier.size(48.dp),
                            contentDescription = "Add new KnockAlert",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Create a new KnockAlert",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (knockAlerts.isEmpty()) {
            Text(
                text = "No active KnockAlerts right now. Create one to get started!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(knockAlerts) { alert ->
                    KnockAlertItem(alert = alert, onKnockAction = onKnockAction)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign Out")
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun KnockAlertItem(
    alert: KnockAlert,
    onKnockAction: (KnockAlert) -> Unit, 
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "From: ${alert.ownerId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Active since: ${alert.targetTimestamp}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onKnockAction(alert) }) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Knock",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewNoUser() {
    HomeScreen(
        uiState = HomeUiState.Success(user = null, knockAlerts = emptyList()), 
        onSignOut = {}, 
        onKnockAction = {}, 
        onNavigateToAddAlert = {}
    )
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewWithUser() {
    val sampleUser = AppUser(
        uid = "12345",
        displayName = "John Doe",
        email = "john.doe@example.com"
    )
    val now = Clock.System.now().toEpochMilliseconds()
    val sampleAlerts = listOf(
        KnockAlert(
            id = "alert1",
            ownerId = "p1",
            content = "Is anyone home? Dinner is ready!",
            targetTimestamp = now - 100000 
        ),
        KnockAlert(
            id = "alert2",
            ownerId = "p2",
            content = "Package delivered at your doorstep.",
            targetTimestamp = now - 50000 
        ),
        KnockAlert(
            id = "alert3",
            ownerId = "12345", 
            content = "Reminder: Meeting at 3 PM.",
            targetTimestamp = now - 10000 
        ) 
    )
    HomeScreen(
        uiState = HomeUiState.Success(user = sampleUser, knockAlerts = sampleAlerts), 
        onSignOut = {}, 
        onKnockAction = { println("Preview Knock: ${it.id}") },
        onNavigateToAddAlert = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewError() {
    HomeScreen(
        uiState = HomeUiState.Error("Preview error message"), 
        onSignOut = {}, 
        onKnockAction = {}, 
        onNavigateToAddAlert = {}
    )
}
