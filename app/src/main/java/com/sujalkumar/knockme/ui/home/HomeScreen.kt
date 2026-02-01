package com.sujalkumar.knockme.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.TimeToLeave
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.KnockAlert
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.ui.common.asString
import com.sujalkumar.knockme.ui.model.FeedKnockAlertUi
import com.sujalkumar.knockme.ui.model.MyKnockAlertUi
import com.sujalkumar.knockme.ui.model.ProfileUi
import com.sujalkumar.knockme.ui.theme.KnockMeTheme
import com.sujalkumar.knockme.util.TimeUtils
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToAddAlert: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedAlertForKnockers by remember {
        mutableStateOf<MyKnockAlertUi?>(null)
    }
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is HomeUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onShowKnockers = { alert ->
            selectedAlertForKnockers = alert
            showBottomSheet = true
        },
        onKnockAction = { alert -> viewModel.knockOnAlert(alert.id) },
        onSignOut = viewModel::onSignOut,
        onNavigateToAddAlert = onNavigateToAddAlert,
        modifier = modifier,
    )

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = {
            showBottomSheet = false
        }) {
            KnockersBottomSheetContent(
                alert = selectedAlertForKnockers!!
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onShowKnockers: (MyKnockAlertUi) -> Unit,
    onKnockAction: (KnockAlert) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAddAlert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showFab = uiState.myKnockAlerts.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(tonalElevation = 3.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.knockme),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = onSignOut) {
                            Icon(
                                imageVector = Icons.Outlined.TimeToLeave,
                                contentDescription = stringResource(R.string.logout)
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = onNavigateToAddAlert) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_knockalert)
                    )
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
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                HomeScreenSuccessContent(
                    user = uiState.user,
                    myKnockAlerts = uiState.myKnockAlerts,
                    feedKnockAlerts = uiState.feedKnockAlerts,
                    onShowKnockers = onShowKnockers,
                    onKnockAction = onKnockAction,
                    onNavigateToAddAlert = onNavigateToAddAlert,
                    modifier = Modifier
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
internal fun HomeScreenSuccessContent(
    user: User?,
    myKnockAlerts: List<MyKnockAlertUi>,
    feedKnockAlerts: List<FeedKnockAlertUi>,
    onShowKnockers: (MyKnockAlertUi) -> Unit,
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
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user?.displayName ?: "User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your KnockAlerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (myKnockAlerts.isEmpty()) {
            OutlinedCard(
                onClick = onNavigateToAddAlert,
                shape = MaterialTheme.shapes.large,
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
                        contentDescription = stringResource(R.string.add_icon),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.create_your_first_knockalert),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { myKnockAlerts.size })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) { page ->
                    val pageOffset by remember {
                        derivedStateOf {
                            ((pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction).absoluteValue
                        }
                    }

                    val scale = lerp(
                        start = 0.92f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    MyKnockAlertCard(
                        myAlert = myKnockAlerts[page],
                        onShowKnockers = { alert ->
                            onShowKnockers(alert)
                        },
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }

                if (pagerState.pageCount > 1) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { index ->
                            val selected = pagerState.currentPage == index

                            val width by animateDpAsState(
                                targetValue = if (selected) 20.dp else 6.dp,
                                label = stringResource(R.string.pagerindicatorwidth)
                            )

                            val color by animateColorAsState(
                                targetValue =
                                    if (selected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.4f
                                        ),
                                label = stringResource(R.string.pagerindicatorcolor)
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(6.dp)
                                    .width(width)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
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
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.knockalerts_from_others),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (feedKnockAlerts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_active_knockalerts_from_others_right_now),
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
                        items(
                            items = feedKnockAlerts,
                            key = { it.alert.id }
                        ) { displayableAlert ->
                            val index = feedKnockAlerts.indexOf(displayableAlert)
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
                                currentUserId = user?.uid,
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

@Composable
internal fun MyKnockAlertCard(
    myAlert: MyKnockAlertUi,
    onShowKnockers: (MyKnockAlertUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (myAlert.isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainer,
        label = "AlertCardColor"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = myAlert.alert.content,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (myAlert.isActive) {

                if (myAlert.alert.knockedByUserIds.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = stringResource(R.string.android)
                        )
                        Text(
                            text = stringResource(R.string.no_one_has_knocked_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Button(
                        onClick = { onShowKnockers(myAlert) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(
                                R.string.knocked_by_users,
                                myAlert.alert.knockedByUserIds.size
                            )
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.active_for, TimeUtils.toRelativeTime(
                            myAlert.alert.targetTime.toEpochMilliseconds()
                        )
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                val animatedProgress by animateFloatAsState(
                    targetValue = myAlert.progress,
                    label = stringResource(R.string.alertprogress)
                )

                Column {
                    val formattedTime = remember(myAlert.alert.targetTime) {
                        DateFormat.format(
                            "EEE, dd MMM • hh:mm a",
                            myAlert.alert.targetTime.toEpochMilliseconds()
                        ).toString()
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.ready, (animatedProgress * 100).toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun KnockAlertItem(
    displayableAlert: FeedKnockAlertUi,
    currentUserId: String?,
    onKnockAction: (KnockAlert) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    val alert = displayableAlert.alert

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
            val errorColor = MaterialTheme.colorScheme.inverseOnSurface

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(displayableAlert.owner?.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.alert_owner_avatar),
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(errorColor),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                alignment = Alignment.Center
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(
                        R.string.from,
                        displayableAlert.owner?.displayName ?: stringResource(R.string.unknown)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(
                        R.string.active_since,
                        TimeUtils.toRelativeTime(alert.targetTime.toEpochMilliseconds())
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val hasKnocked = displayableAlert.hasKnocked(currentUserId)

            val backgroundColor by animateColorAsState(
                targetValue =
                    if (hasKnocked) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primaryContainer,
                label = "BackgroundColor"
            )
            val iconColor by animateColorAsState(
                targetValue =
                    if (hasKnocked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
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

@Composable
fun KnockersBottomSheetContent(alert: MyKnockAlertUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.knocked_by),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        if (alert.knockers.isEmpty()) {
            Text(
                text = stringResource(R.string.no_one_has_knocked_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            alert.knockers.forEach { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.knocker_avatar),
                        placeholder = ColorPainter(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        error = ColorPainter(
                            MaterialTheme.colorScheme.inverseOnSurface
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = profile.displayName ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Screen - No User")
@Composable
fun HomeScreenPreviewNoUser() {
    KnockMeTheme {
        HomeScreen(
            uiState = HomeUiState(
                user = null,
                myKnockAlerts = emptyList(),
                feedKnockAlerts = emptyList(),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onShowKnockers = {},
            onKnockAction = {},
            onNavigateToAddAlert = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen - User With Alerts")
@Composable
fun HomeScreenPreviewWithUserAndAlerts() {
    KnockMeTheme {
        val sampleUser = User(
            uid = "currentUser123",
            displayName = "Current User"
        )
        val now = Clock.System.now().toEpochMilliseconds()
        val myAlerts = listOf(
            MyKnockAlertUi(
                alert = KnockAlert(
                    id = "myAlert1",
                    ownerId = "currentUser123",
                    content = "This is my first alert, just a reminder to me.",
                    createdAt = Instant.fromEpochMilliseconds(now - 400000),
                    targetTime = Instant.fromEpochMilliseconds(now - 200000),
                    knockedByUserIds = emptyList()
                ),
                progress = 1f,
                isActive = true
            ),
            MyKnockAlertUi(
                alert = KnockAlert(
                    id = "myAlert2",
                    ownerId = "currentUser123",
                    content = "Another personal alert for the horizontal pager.",
                    createdAt = Instant.fromEpochMilliseconds(now - 300000),
                    targetTime = Instant.fromEpochMilliseconds(now + 60000),
                    knockedByUserIds = emptyList()
                ),
                progress = 0.7f,
                isActive = false
            ),
            MyKnockAlertUi(
                alert = KnockAlert(
                    id = "myAlert3",
                    ownerId = "currentUser123",
                    content = "A third alert to test the pager dots properly.",
                    createdAt = Instant.fromEpochMilliseconds(now - 200000),
                    targetTime = Instant.fromEpochMilliseconds(now + 120000),
                    knockedByUserIds = emptyList()
                ),
                progress = 0.3f,
                isActive = false
            )
        )
        val feedAlerts = listOf(
            FeedKnockAlertUi(
                alert = KnockAlert(
                    id = "feedAlert1",
                    ownerId = "otherUser456",
                    content = "Alert from another user, visible in the main feed.",
                    createdAt = Instant.fromEpochMilliseconds(now - 150000),
                    targetTime = Instant.fromEpochMilliseconds(now - 100000),
                    knockedByUserIds = listOf("someUserId")
                ),
                owner = ProfileUi(
                    displayName = "Other User",
                    photoUrl = null
                )
            ),
            FeedKnockAlertUi(
                alert = KnockAlert(
                    id = "feedAlert2",
                    ownerId = "anotherUser789",
                    content = "Second alert in the feed from someone else.",
                    createdAt = Instant.fromEpochMilliseconds(now - 80000),
                    targetTime = Instant.fromEpochMilliseconds(now - 50000),
                    knockedByUserIds = emptyList()
                ),
                owner = ProfileUi(
                    displayName = "Another User",
                    photoUrl = null
                )
            )
        )
        HomeScreen(
            uiState = HomeUiState(
                user = sampleUser,
                myKnockAlerts = myAlerts,
                feedKnockAlerts = feedAlerts,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onKnockAction = { println("Preview Knock: ${it.id}") },
            onShowKnockers = {},
            onNavigateToAddAlert = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen - User, No Own Alerts")
@Composable
fun HomeScreenPreviewUserNoOwnAlerts() {
    KnockMeTheme {
        val sampleUser = User(
            uid = "currentUser123",
            displayName = "Current User"
        )
        val now = Clock.System.now().toEpochMilliseconds()
        val feedAlerts = listOf(
            FeedKnockAlertUi(
                alert = KnockAlert(
                    id = "feedAlert1",
                    ownerId = "otherUser456",
                    content = "Feed alert when user has no own alerts.",
                    createdAt = Instant.fromEpochMilliseconds(now - 200000),
                    targetTime = Instant.fromEpochMilliseconds(now - 100000),
                    knockedByUserIds = emptyList()
                ),
                owner = ProfileUi(
                    displayName = "Other User",
                    photoUrl = null
                )
            )
        )
        HomeScreen(
            uiState = HomeUiState(
                user = sampleUser,
                myKnockAlerts = emptyList(),
                feedKnockAlerts = feedAlerts,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onKnockAction = { println("Preview Knock: ${it.id}") },
            onShowKnockers = {},
            onNavigateToAddAlert = {},
            onSignOut = {}
        )
    }
}
