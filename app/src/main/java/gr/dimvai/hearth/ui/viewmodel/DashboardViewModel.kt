package gr.dimvai.hearth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.dimvai.hearth.data.model.Connection
import gr.dimvai.hearth.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardState(
    val overdue: List<Connection> = emptyList(),
    val today: List<Connection> = emptyList(),
    val upcoming: List<Connection> = emptyList(),
    val later: List<Connection> = emptyList()
)

class DashboardViewModel(private val repository: ConnectionRepository) : ViewModel() {

    val uiState: StateFlow<DashboardState> = repository.allConnections
        .map { connections ->
            val today = LocalDate.now()
            val groups = connections.groupBy { conn ->
                val nextDate = conn.calculateNextCommunicationDate()
                when {
                    nextDate.isBefore(today) -> "overdue"
                    nextDate.isEqual(today) -> "today"
                    nextDate.isBefore(today.plusDays(3)) -> "upcoming"
                    else -> "later"
                }
            }

            DashboardState(
                overdue = groups["overdue"]?.sortedBy { it.calculateNextCommunicationDate() } ?: emptyList(),
                today = groups["today"]?.sortedBy { it.name } ?: emptyList(),
                upcoming = groups["upcoming"]?.sortedBy { it.calculateNextCommunicationDate() } ?: emptyList(),
                later = groups["later"]?.sortedBy { it.calculateNextCommunicationDate() } ?: emptyList()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState()
        )

    fun markCommunicated(connection: Connection) {
        viewModelScope.launch {
            repository.update(connection.copy(
                lastCommunicationDate = LocalDate.now(),
                scheduledNextDate = null
            ))
        }
    }

    fun postponeByOneDay(connection: Connection) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentNextDate = connection.calculateNextCommunicationDate()
            val newDate = if (currentNextDate.plusDays(1).isBefore(today)) {
                today
            } else {
                currentNextDate.plusDays(1)
            }
            
            repository.update(connection.copy(
                scheduledNextDate = newDate
            ))
        }
    }
}
