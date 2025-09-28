package com.sujalkumar.knockme.ui.addalert

import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddKnockAlertScreen(
    viewModel: AddKnockAlertViewModel = koinViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alertContent by viewModel.alertContent.collectAsStateWithLifecycle()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var selectedDateTimeMillis by remember { mutableStateOf<Long?>(null) }

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

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddKnockAlertUiState.Success -> {
                Toast.makeText(context, "Alert added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateUp()
            }

            is AddKnockAlertUiState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }

            else -> Unit
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
                value = formattedDateTimeString,
                onValueChange = { /* Not directly editable */ },
                label = { Text("Target Date & Time") },
                modifier = Modifier
                    .fillMaxWidth()
//                    .clickable(onClick = {
//                        showDatePickerDialog = true
//                        Log.i("AddKnockAlertScreen", "onClick")
//                    })
                ,
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
                                    viewModel.onTargetTimestampChanged(selectedDateTimeMillis!!)
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
                    if (alertContent.isNotBlank() && selectedDateTimeMillis != null) {
                        viewModel.addKnockAlert()
                    } else if (selectedDateTimeMillis == null) {
                        Toast.makeText(
                            context,
                            "Please select a target date and time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (alertContent.isBlank()) {
                        Toast.makeText(context, "Alert content cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != AddKnockAlertUiState.Loading && alertContent.isNotBlank() && selectedDateTimeMillis != null
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
