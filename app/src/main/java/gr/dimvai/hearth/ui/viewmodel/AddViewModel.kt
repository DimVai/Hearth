package gr.dimvai.hearth.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.dimvai.hearth.data.model.Connection
import gr.dimvai.hearth.data.repository.ConnectionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddViewModel(private val repository: ConnectionRepository) : ViewModel() {
    var name by mutableStateOf("")
    var frequencyDays by mutableStateOf(7)
    var lastCommunicationDate by mutableStateOf<LocalDate?>(null)
    var scheduledNextDate by mutableStateOf<LocalDate?>(null)

    fun saveConnection(onSuccess: () -> Unit) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val connection = Connection(
                name = name.trim(),
                frequencyDays = frequencyDays,
                lastCommunicationDate = lastCommunicationDate,
                scheduledNextDate = scheduledNextDate
            )
            repository.insert(connection)
            onSuccess()
        }
    }
}
