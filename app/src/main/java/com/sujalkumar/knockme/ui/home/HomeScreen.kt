package com.sujalkumar.knockme.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.TimeToLeave
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import com.sujalkumar.knockme.util.TimeUtils
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
    onNavigateToAddAlert: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onKnockAction = { alert -> homeViewModel.knockOnAlert(alert.id) },
        onSignOut = onLogout,
        onNavigateToAddAlert = onNavigateToAddAlert,
        modifier = modifier
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onKnockAction: (KnockAlert) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAddAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showFab = uiState.myKnockAlerts.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("KnockMe") },
                actions = {
                    IconButton(
                        onClick = onSignOut
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TimeToLeave,
                            contentDescription = "Logout Icon"
                        )
                    }
                })
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = onNavigateToAddAlert) {
                    Icon(Icons.Filled.Add, contentDescription = "Create KnockAlert")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.user != null) {
                HomeScreenSuccessContent(
                    user = uiState.user,
                    myKnockAlerts = uiState.myKnockAlerts,
                    feedKnockAlerts = uiState.feedKnockAlerts,
                    onKnockAction = onKnockAction,
                    onNavigateToAddAlert = onNavigateToAddAlert,
                    modifier = Modifier
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalTime::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
internal fun HomeScreenSuccessContent(
    user: User,
    myKnockAlerts: List<KnockAlert>,
    feedKnockAlerts: List<DisplayableKnockAlert>,
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your KnockAlerts",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (myKnockAlerts.isEmpty()) {
            OutlinedCard(
                onClick = onNavigateToAddAlert,
                modifier = Modifier.fillMaxWidth()
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
                        contentDescription = "Create your first KnockAlert",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Create your first KnockAlert",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { myKnockAlerts.size })
            Column(horizontalAlignment = Alignment.CenterHorizontally) { // Column to center Pager and Dots
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) { page ->
                    MyKnockAlertCard(
                        alert = myKnockAlerts[page],
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                if (pagerState.pageCount > 1) {
                    Row(
                        Modifier
                            .height(24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.3f
                                )
                            val dotSize = if (pagerState.currentPage == iteration) 10.dp else 8.dp
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(dotSize)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLargeIncreased
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KnockAlerts from Others",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (feedKnockAlerts.isEmpty()) {
                    Text(
                        text = "No active KnockAlerts from others right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(feedKnockAlerts) { index, displayableAlert ->
                            val shape = when {
                                feedKnockAlerts.size == 1 -> RoundedCornerShape(16.dp)

                                index == 0 -> RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 8.dp,
                                    bottomEnd = 8.dp
                                )

                                index == feedKnockAlerts.lastIndex -> RoundedCornerShape(
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp,
                                    topStart = 8.dp,
                                    topEnd = 8.dp,
                                )

                                else -> RoundedCornerShape(8.dp)
                            }

                            KnockAlertItem(
                                displayableAlert = displayableAlert,
                                currentUserId = user.uid,
                                onKnockAction = onKnockAction,
                                shape = shape
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun MyKnockAlertCard(
    alert: KnockAlert,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = alert.content,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                maxLines = 3,
                minLines = 2
            )
            Text(
                text = "Active since: ${TimeUtils.toRelativeTime(alert.targetTime.toEpochMilliseconds())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun KnockAlertItem(
    displayableAlert: DisplayableKnockAlert,
    currentUserId: String?,
    onKnockAction: (KnockAlert) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    val alert = displayableAlert.alert
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape
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
                    text = "From: ${displayableAlert.ownerDisplayName ?: "Unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Active since: ${TimeUtils.toRelativeTime(alert.targetTime.toEpochMilliseconds())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val hasKnocked = displayableAlert.hasKnocked(currentUserId)
            val backgroundColor by animateColorAsState(
                targetValue = if (hasKnocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                label = "BackgroundColor"
            )
            val iconColor by animateColorAsState(
                targetValue = if (hasKnocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                label = "IconColor"
            )
            IconButton(
                onClick = { onKnockAction(alert) },
                enabled = !hasKnocked,
                modifier = Modifier
                    .clip(if (hasKnocked) CircleShape else MaterialShapes.Cookie9Sided.toShape())
                    .background(backgroundColor)
            ) {
                Crossfade(targetState = hasKnocked, label = "IconSwap") { isKnocked ->
                    Icon(
                        imageVector = if (isKnocked) Icons.Filled.Check else Icons.Filled.Build,
                        contentDescription = if (isKnocked) "Knocked" else "Knock",
                        tint = iconColor
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Screen - No User")
@Composable
fun HomeScreenPreviewNoUser() {
    HomeScreen(
        uiState = HomeUiState(
            user = null,
            myKnockAlerts = emptyList(),
            feedKnockAlerts = emptyList()
        ),
        snackbarHostState = SnackbarHostState(),
        onKnockAction = {},
        onNavigateToAddAlert = {},
        onSignOut = {}
    )
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true, name = "Home Screen - User With Alerts")
@Composable
fun HomeScreenPreviewWithUserAndAlerts() {
    val sampleUser = User(
        uid = "currentUser123",
        displayName = "Current User"
    )
    val now = Clock.System.now().toEpochMilliseconds()
    val myAlerts = listOf(
        KnockAlert(
            id = "myAlert1",
            ownerId = "currentUser123",
            content = "This is my first alert, just a reminder for myself.",
            targetTime = Instant.fromEpochMilliseconds(now - 200000),
            knockedByUserIds = emptyList()
        ),
        KnockAlert(
            id = "myAlert2",
            ownerId = "currentUser123",
            content = "Another personal alert for the horizontal pager.",
            targetTime = Instant.fromEpochMilliseconds(now - 150000),
            knockedByUserIds = emptyList()
        ),
        KnockAlert(
            id = "myAlert3",
            ownerId = "currentUser123",
            content = "A third alert to test the pager dots properly.",
            targetTime = Instant.fromEpochMilliseconds(now - 100000),
            knockedByUserIds = emptyList()
        )
    )
    val feedAlerts = listOf(
        DisplayableKnockAlert(
            alert = KnockAlert(
                id = "feedAlert1",
                ownerId = "otherUser456",
                content = "Alert from another user, visible in the main feed.",
                targetTime = Instant.fromEpochMilliseconds(now - 100000),
                knockedByUserIds = listOf("someUserId")
            ),
            ownerDisplayName = "Other User"
        ),
        DisplayableKnockAlert(
            alert = KnockAlert(
                id = "feedAlert2",
                ownerId = "anotherUser789",
                content = "Second alert in the feed from someone else.",
                targetTime = Instant.fromEpochMilliseconds(now - 50000),
                knockedByUserIds = emptyList()
            ),
            ownerDisplayName = "Another User"
        )
    )
    HomeScreen(
        uiState = HomeUiState(
            user = sampleUser,
            myKnockAlerts = myAlerts,
            feedKnockAlerts = feedAlerts
        ),
        snackbarHostState = SnackbarHostState(),
        onKnockAction = { println("Preview Knock: ${it.id}") },
        onNavigateToAddAlert = {},
        onSignOut = {}
    )
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true, name = "Home Screen - User, No Own Alerts")
@Composable
fun HomeScreenPreviewUserNoOwnAlerts() {
    val sampleUser = User(
        uid = "currentUser123",
        displayName = "Current User"
    )
    val now = Clock.System.now().toEpochMilliseconds()
    val feedAlerts = listOf(
        DisplayableKnockAlert(
            alert = KnockAlert(
                id = "feedAlert1",
                ownerId = "otherUser456",
                content = "Feed alert when user has no own alerts.",
                targetTime = Instant.fromEpochMilliseconds(now - 100000),
                knockedByUserIds = emptyList()
            ),
            ownerDisplayName = "Other User"
        )
    )
    HomeScreen(
        uiState = HomeUiState(
            user = sampleUser,
            myKnockAlerts = emptyList(),
            feedKnockAlerts = feedAlerts
        ),
        snackbarHostState = SnackbarHostState(),
        onKnockAction = { println("Preview Knock: ${it.id}") },
        onNavigateToAddAlert = {},
        onSignOut = {}
    )
}
