package gr.dimvai.hearth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gr.dimvai.hearth.ui.components.HearthHeader
import gr.dimvai.hearth.ui.viewmodel.AddViewModel
import gr.dimvai.hearth.ui.viewmodel.EditViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddScreen(
    viewModel: AddViewModel,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            HearthHeader(
                title = "Νέα Επαφή",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConnectionForm(
                name = viewModel.name,
                onNameChange = { viewModel.name = it },
                frequencyDays = viewModel.frequencyDays,
                onFrequencyChange = { viewModel.frequencyDays = it },
                lastCommunicationDate = viewModel.lastCommunicationDate,
                onDateChange = { viewModel.lastCommunicationDate = it },
                scheduledNextDate = viewModel.scheduledNextDate,
                onScheduledDateChange = { viewModel.scheduledNextDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveConnection(onBackClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = viewModel.name.isNotBlank()
            ) {
                Text(
                    text = "Αποθήκευση",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EditScreen(
    viewModel: EditViewModel,
    onBackClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                HearthHeader(
                    title = "Επεξεργασία Επαφής",
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConnectionForm(
                    name = viewModel.name,
                    onNameChange = { 
                        viewModel.name = it
                        viewModel.onFieldChanged()
                    },
                    frequencyDays = viewModel.frequencyDays,
                    onFrequencyChange = { 
                        viewModel.frequencyDays = it
                        viewModel.onFieldChanged()
                    },
                    lastCommunicationDate = viewModel.lastCommunicationDate,
                    onDateChange = { 
                        viewModel.lastCommunicationDate = it
                        viewModel.onFieldChanged()
                    },
                    scheduledNextDate = viewModel.scheduledNextDate,
                    onScheduledDateChange = {
                        viewModel.scheduledNextDate = it
                        viewModel.onFieldChanged()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Διαγραφή επαφής")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Διαγραφή επαφής") },
            text = { Text("Θέλετε σίγουρα να διαγράψετε την επαφή ${viewModel.name};") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteConnection(onBackClick)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Διαγραφή")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Ακύρωση")
                }
            }
        )
    }
}

@Composable
fun ConnectionForm(
    name: String,
    onNameChange: (String) -> Unit,
    frequencyDays: Int,
    onFrequencyChange: (Int) -> Unit,
    lastCommunicationDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    scheduledNextDate: LocalDate? = null,
    onScheduledDateChange: ((LocalDate?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Όνομα") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Συχνότητα επικοινωνίας", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        FrequencySelector(selectedDays = frequencyDays, onFrequencyChange = onFrequencyChange)

        HearthDatePicker(
            label = "Τελευταία επικοινωνία (προαιρετικό)",
            selectedDate = lastCommunicationDate,
            onDateChange = onDateChange
        )

        if (onScheduledDateChange != null) {
            HearthDatePicker(
                label = "Επόμενη προγραμματισμένη επικοινωνία (προαιρετικό)",
                selectedDate = scheduledNextDate,
                onDateChange = onScheduledDateChange
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now()
                QuickDateButton("Σήμερα", today, onScheduledDateChange)
                QuickDateButton("Αύριο", today.plusDays(1), onScheduledDateChange)
                QuickDateButton("+1 μέρα", scheduledNextDate?.plusDays(1) ?: today.plusDays(1), onScheduledDateChange)
            }
        }
    }
}

@Composable
fun QuickDateButton(label: String, date: LocalDate, onClick: (LocalDate) -> Unit) {
    OutlinedButton(onClick = { onClick(date) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencySelector(selectedDays: Int, onFrequencyChange: (Int) -> Unit) {
    val options = listOf(
        1 to "Κάθε μέρα",
        2 to "Κάθε 2 μέρες",
        7 to "Κάθε εβδομάδα",
        14 to "Κάθε 2 εβδομάδες",
        30 to "Κάθε μήνα",
        60 to "Κάθε 2 μήνες"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = options.find { it.first == selectedDays }?.second ?: "Κάθε $selectedDays μέρες",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (days, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFrequencyChange(days)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HearthDatePicker(
    label: String,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = selectedDate?.format(formatter) ?: "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false, // Disable to pass clicks to the parent Box
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        )
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateChange(date)
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    onDateChange(null)
                    showDialog = false 
                }) {
                    Text("Καθαρισμός")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
