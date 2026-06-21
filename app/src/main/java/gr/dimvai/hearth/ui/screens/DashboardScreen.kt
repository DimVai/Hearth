package gr.dimvai.hearth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.dimvai.hearth.data.model.Connection
import gr.dimvai.hearth.ui.components.HearthHeader
import gr.dimvai.hearth.ui.theme.Accent
import gr.dimvai.hearth.ui.theme.AddButtonColor
import gr.dimvai.hearth.ui.theme.OnAddButtonColor
import gr.dimvai.hearth.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
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
                containerColor = AddButtonColor,
                contentColor = OnAddButtonColor,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
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
                    ConnectionCard(
                        connection = connection,
                        viewModel = viewModel,
                        isUpcomingOrLater = false,
                        onEditClick = onEditClick
                    )
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
                    ConnectionCard(
                        connection = connection,
                        viewModel = viewModel,
                        isUpcomingOrLater = false,
                        onEditClick = onEditClick
                    )
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
                    ConnectionCard(
                        connection = connection,
                        viewModel = viewModel,
                        isUpcomingOrLater = true,
                        onEditClick = onEditClick
                    )
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
                    ConnectionCard(
                        connection = connection,
                        viewModel = viewModel,
                        isUpcomingOrLater = true,
                        onEditClick = onEditClick
                    )
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
            style = MaterialTheme.typography.titleMedium,
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
    isUpcomingOrLater: Boolean,
    onEditClick: (String) -> Unit
) {
    val buttonSize = 56.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick(connection.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                .compositeOver(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (connection.isOverdue) Accent else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                val daysUntil = connection.getDaysUntilNext()
                val nextCommText = when {
                    daysUntil < 0 -> "Εκπρόθεσμο (${-daysUntil} μέρες)"
                    daysUntil == 0L -> "Σήμερα"
                    daysUntil == 1L -> "Αύριο"
                    else -> "Σε $daysUntil μέρες"
                }
                
                Text(
                    text = nextCommText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (connection.isOverdue) Accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = connection.getFrequencyLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isUpcomingOrLater) {
                    FilledIconButton(
                        onClick = { viewModel.moveToToday(connection) },
                        modifier = Modifier.size(buttonSize),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "Move to today",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = { viewModel.markCommunicated(connection) },
                        modifier = Modifier.size(buttonSize),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark as done",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                FilledIconButton(
                    onClick = { viewModel.postponeByOneDay(connection) },
                    modifier = Modifier.size(buttonSize),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Accent,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("+1", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
