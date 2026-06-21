package gr.dimvai.hearth.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gr.dimvai.hearth.data.local.SettingsDataStore
import gr.dimvai.hearth.data.model.BackupData
import gr.dimvai.hearth.data.repository.ConnectionRepository
import gr.dimvai.hearth.notifications.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class SettingsState(
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 10,
    val reminderMinute: Int = 0,
    val backupRestoreMessage: String? = null
)

class SettingsViewModel(
    private val repository: ConnectionRepository,
    application: Application
) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsState> = combine(
        settingsDataStore.remindersEnabled,
        settingsDataStore.reminderHour,
        settingsDataStore.reminderMinute,
        _message
    ) { enabled, hour, minute, message ->
        SettingsState(enabled, hour, minute, message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun clearMessage() {
        _message.value = null
    }

    fun backupContacts(uri: Uri) {
        viewModelScope.launch {
            try {
                val connections = repository.getAllConnectionsOnce()
                val backupData = BackupData(connections = connections)
                val jsonString = Json.encodeToString(backupData)

                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                }
                _message.value = "Το backup ολοκληρώθηκε επιτυχώς!"
            } catch (e: Exception) {
                _message.value = "Σφάλμα κατά το backup: ${e.message}"
            }
        }
    }

    fun restoreContacts(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }

                if (jsonString != null) {
                    val backupData = Json.decodeFromString<BackupData>(jsonString)
                    repository.insertConnections(backupData.connections)
                    _message.value = "Η επαναφορά ολοκληρώθηκε επιτυχώς!"
                } else {
                    _message.value = "Σφάλμα: Το αρχείο είναι κενό."
                }
            } catch (e: Exception) {
                _message.value = "Σφάλμα κατά την επαναφορά: ${e.message}"
            }
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveRemindersEnabled(enabled)
            if (enabled) {
                val current = uiState.value
                NotificationScheduler.schedule(getApplication(), current.reminderHour, current.reminderMinute)
            } else {
                NotificationScheduler.cancel(getApplication())
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsDataStore.saveReminderTime(hour, minute)
            if (uiState.value.remindersEnabled) {
                NotificationScheduler.schedule(getApplication(), hour, minute)
            }
        }
    }
}
