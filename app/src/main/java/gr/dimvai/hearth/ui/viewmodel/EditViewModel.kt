package gr.dimvai.hearth.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.dimvai.hearth.data.model.Connection
import gr.dimvai.hearth.data.repository.ConnectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditViewModel(
    private val connectionId: String,
    private val repository: ConnectionRepository
) : ViewModel() {

    private var originalConnection: Connection? = null
    var name by mutableStateOf("")
    var frequencyDays by mutableStateOf(7)
    var lastCommunicationDate by mutableStateOf<LocalDate?>(null)
    var scheduledNextDate by mutableStateOf<LocalDate?>(null)
    
    var isLoading by mutableStateOf(true)
    private var saveJob: Job? = null

    init {
        loadConnection()
    }

    private fun loadConnection() {
        viewModelScope.launch {
            repository.getConnectionById(connectionId)?.let { connection ->
                originalConnection = connection
                name = connection.name
                frequencyDays = connection.frequencyDays
                lastCommunicationDate = connection.lastCommunicationDate
                scheduledNextDate = connection.scheduledNextDate
            }
            isLoading = false
        }
    }

    fun onFieldChanged() {
        if (isLoading) return
        
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // Debounce auto-save
            save()
        }
    }

    private suspend fun save() {
        val current = originalConnection ?: return
        val updated = current.copy(
            name = name,
            frequencyDays = frequencyDays,
            lastCommunicationDate = lastCommunicationDate,
            scheduledNextDate = scheduledNextDate
        )
        repository.update(updated)
        originalConnection = updated
    }

    fun deleteConnection(onSuccess: () -> Unit) {
        viewModelScope.launch {
            originalConnection?.let {
                repository.delete(it)
                onSuccess()
            }
        }
    }
}
