package com.sujalkumar.knockme.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sujalkumar.knockme.ui.model.UserProfileUi
import com.sujalkumar.knockme.ui.theme.KnockMeTheme

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onNavigateUp: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onEditProfileClick = onEditProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNavigateUp: () -> Unit,
    onEditProfileClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.profile != null -> {
                    ProfileContent(
                        profile = uiState.profile,
                        onEditProfileClick = onEditProfileClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfileUi,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(profile)

        Spacer(modifier = Modifier.height(24.dp))

        profile.bio?.let { bio ->
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = profile.joinedText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (profile.isCurrentUser) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onEditProfileClick) {
                Text(text = "Edit profile")
            }
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfileUi) {
    AsyncImage(
        model = profile.photoUrl,
        contentDescription = "Profile picture",
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = profile.displayName ?: "Unknown user",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    KnockMeTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                profile = UserProfileUi(
                    uid = "user_1",
                    displayName = "Sujal Kumar",
                    photoUrl = null,
                    bio = "Android developer • Kotlin • Jetpack Compose",
                    joinedText = "Joined Jan 2026",
                    isCurrentUser = true
                )
            ),
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    KnockMeTheme {
        ProfileScreen(
            uiState = ProfileUiState(isLoading = true),
            onNavigateUp = {}
        )
    }
}
