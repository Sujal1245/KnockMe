package com.sujalkumar.knockme.ui.addalert

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujalkumar.knockme.ui.theme.KnockMeTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Instant

// Helper function to format Long timestamp to a readable string
private fun formatEpochMillisToDateTime(millis: Long?): String {
    if (millis == null) return "Select Date and Time"
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }
    // Example format: "MMM dd, yyyy HH:mm" (e.g., "Jul 04, 2024 14:30")
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(calendar.time)
}

@Composable
fun AddKnockAlertRoute(
    viewModel: AddKnockAlertViewModel = koinViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddKnockAlertScreen(
        uiState = uiState,
        onAlertContentChanged = viewModel::onAlertContentChanged,
        onTargetTimeChanged = viewModel::onTargetTimeChanged,
        onSubmit = viewModel::addKnockAlert,
        onResetState = viewModel::resetState,
        onNavigateUp = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AddKnockAlertScreen(
    uiState: AddKnockAlertUiState,
    onAlertContentChanged: (String) -> Unit,
    onTargetTimeChanged: (Long) -> Unit,
    onSubmit: () -> Unit,
    onResetState: () -> Unit,
    onNavigateUp: () -> Unit
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var selectedDateTimeMillis by remember { mutableStateOf(uiState.targetTime?.toEpochMilliseconds()) }

    val context = LocalContext.current

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateTimeMillis ?: System.currentTimeMillis()
    )

    val initialCalendar = remember { Calendar.getInstance() }
    if (selectedDateTimeMillis != null) {
        initialCalendar.timeInMillis = selectedDateTimeMillis!!
    } else {
        // If no time is selected yet, default to current time for TimePicker init
        initialCalendar.timeInMillis = System.currentTimeMillis()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    val formattedDateTimeString by remember(selectedDateTimeMillis) {
        derivedStateOf { formatEpochMillisToDateTime(selectedDateTimeMillis) }
    }

    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
        when {
            uiState.isSuccess -> {
                Toast.makeText(context, "Alert added successfully!", Toast.LENGTH_SHORT).show()
                onResetState()
                onNavigateUp()
            }

            uiState.errorMessage != null -> {
                Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
                onResetState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New KnockAlert") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate up"
                        )
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
                value = uiState.alertContent,
                onValueChange = { onAlertContentChanged(it) },
                label = { Text("Alert Content") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            OutlinedTextField(
                value = formattedDateTimeString,
                onValueChange = { /* Not directly editable */ },
                label = { Text("Target Date & Time") },
                modifier = Modifier
                    .fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Select Date and Time",
                        modifier = Modifier.clickable { showDatePickerDialog = true })
                }
            )

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePickerDialog = false
                                if (datePickerState.selectedDateMillis != null) {
                                    showTimePickerDialog = true
                                }
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePickerDialog) {
                AlertDialog(
                    onDismissRequest = { showTimePickerDialog = false },
                    title = { Text(text = "Select Time") },
                    text = {
                        TimePicker(
                            state = timePickerState // Modifier.fillMaxWidth() removed
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showTimePickerDialog = false
                                val selectedDateMillisFromPicker =
                                    datePickerState.selectedDateMillis
                                if (selectedDateMillisFromPicker != null) {
                                    val utcCalendar =
                                        Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    utcCalendar.timeInMillis = selectedDateMillisFromPicker
                                    val year = utcCalendar.get(Calendar.YEAR)
                                    val month = utcCalendar.get(Calendar.MONTH)
                                    val dayOfMonth = utcCalendar.get(Calendar.DAY_OF_MONTH)

                                    val localCalendar = Calendar.getInstance()
                                    localCalendar.set(Calendar.YEAR, year)
                                    localCalendar.set(Calendar.MONTH, month)
                                    localCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    localCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    localCalendar.set(Calendar.MINUTE, timePickerState.minute)
                                    localCalendar.set(Calendar.SECOND, 0)
                                    localCalendar.set(Calendar.MILLISECOND, 0)

                                    selectedDateTimeMillis = localCalendar.timeInMillis
                                    onTargetTimeChanged(selectedDateTimeMillis!!)
                                }
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Button(
                onClick = {
                    if (uiState.alertContent.isNotBlank() && selectedDateTimeMillis != null) {
                        onSubmit()
                    } else if (selectedDateTimeMillis == null) {
                        Toast.makeText(
                            context,
                            "Please select a target date and time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (uiState.alertContent.isBlank()) {
                        Toast.makeText(context, "Alert content cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading &&
                        uiState.alertContent.isNotBlank() &&
                        selectedDateTimeMillis != null
            ) {
                if (uiState.isLoading) {
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

@Preview(showBackground = true)
@Composable
private fun AddKnockAlertPreview() {
    KnockMeTheme {
        AddKnockAlertScreen(
            uiState = AddKnockAlertUiState(),
            onAlertContentChanged = {},
            onTargetTimeChanged = {},
            onSubmit = {},
            onResetState = {},
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddKnockAlertLoadingPreview() {
    KnockMeTheme {
        AddKnockAlertScreen(
            uiState = AddKnockAlertUiState(
                alertContent = "Don't forget to buy milk",
                targetTime = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                isLoading = true
            ),
            onAlertContentChanged = {},
            onTargetTimeChanged = {},
            onSubmit = {},
            onResetState = {},
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddKnockAlertFilledPreview() {
    KnockMeTheme {
        AddKnockAlertScreen(
            uiState = AddKnockAlertUiState(
                alertContent = "Meeting with Team",
                targetTime = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            ),
            onAlertContentChanged = {},
            onTargetTimeChanged = {},
            onSubmit = {},
            onResetState = {},
            onNavigateUp = {}
        )
    }
}
