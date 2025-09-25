package com.sujalkumar.knockme.ui.addalert

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddKnockAlertScreen(
    viewModel: AddKnockAlertViewModel = koinViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alertContent by viewModel.alertContent.collectAsStateWithLifecycle()
    // For targetTimestamp, we'll use a local state for the TextField
    // and update the ViewModel only when submitting.
    var manualTimestampString by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddKnockAlertUiState.Success -> {
                Toast.makeText(context, "Alert added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState() // Reset state after success
                onNavigateUp() // Navigate back after success
            }
            is AddKnockAlertUiState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                 viewModel.resetState() // Reset state after error to allow retry
            }
            else -> Unit // Idle, Loading
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New KnockAlert") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Navigate up")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = alertContent,
                onValueChange = { viewModel.onAlertContentChanged(it) },
                label = { Text("Alert Content") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            OutlinedTextField(
                value = manualTimestampString,
                onValueChange = { manualTimestampString = it },
                label = { Text("Target Timestamp (Epoch Millis)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g., ${System.currentTimeMillis() + 3600000}") }
                // TODO: Replace with a proper Date & Time Picker
            )

            Button(
                onClick = {
                    val timestamp = manualTimestampString.toLongOrNull()
                    if (timestamp != null) {
                        viewModel.onTargetTimestampChanged(timestamp)
                        viewModel.addKnockAlert()
                    } else {
                        Toast.makeText(context, "Invalid timestamp format", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != AddKnockAlertUiState.Loading
            ) {
                if (uiState == AddKnockAlertUiState.Loading) {
                    LoadingIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adding Alert...")
                } else {
                    Text("Create KnockAlert")
                }
            }
        }
    }
}
