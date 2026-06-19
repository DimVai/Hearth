package gr.dimvai.hearth.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.dimvai.hearth.data.model.Connection
import gr.dimvai.hearth.ui.components.HearthHeader
import gr.dimvai.hearth.ui.theme.Accent
import gr.dimvai.hearth.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HearthHeader(
                title = "Hearth",
                showSettingsButton = true,
                onSettingsClick = onSettingsClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Accent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.overdue.isNotEmpty()) {
                item { SectionHeader("ΕΚΠΡΟΘΕΣΜΕΣ", Color.Red) }
                items(
                    items = state.overdue,
                    key = { connection -> connection.id }
                ) { connection ->
                    ConnectionCard(connection, viewModel, onEditClick)
                }
            }

            item { SectionHeader("ΣΗΜΕΡΑ", MaterialTheme.colorScheme.onSurfaceVariant) }
            if (state.today.isEmpty()) {
                item { EmptyState("Καμία επαφή.") }
            } else {
                items(
                    items = state.today,
                    key = { it.id }
                ) { connection ->
                    ConnectionCard(connection, viewModel, onEditClick)
                }
            }

            item { SectionHeader("ΠΡΟΣΕΧΕΙΣ", MaterialTheme.colorScheme.onSurfaceVariant) }
            if (state.upcoming.isEmpty()) {
                item { EmptyState("Καμία επαφή.") }
            } else {
                items(
                    items = state.upcoming,
                    key = { it.id }
                ) { connection ->
                    ConnectionCard(connection, viewModel, onEditClick)
                }
            }

            item { SectionHeader("ΑΡΓΟΤΕΡΑ", MaterialTheme.colorScheme.onSurfaceVariant) }
            if (state.later.isEmpty()) {
                item { EmptyState("Καμία επαφή.") }
            } else {
                items(
                    items = state.later,
                    key = { it.id }
                ) { connection ->
                    ConnectionCard(connection, viewModel, onEditClick)
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

@Composable
fun EmptyState(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ConnectionCard(
    connection: Connection,
    viewModel: DashboardViewModel,
    onEditClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (connection.isOverdue) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Accent)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (connection.isToday) "Σήμερα" else "Σε ${connection.frequencyDays} μέρες",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connection.isOverdue) Accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledIconButton(
                    onClick = { viewModel.markCommunicated(connection) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark as done")
                }
                
                OutlinedIconButton(onClick = { viewModel.postponeToTomorrow(connection) }) {
                    Text("+1", fontWeight = FontWeight.Bold)
                }

                OutlinedIconButton(onClick = { onEditClick(connection.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }
    }
}
