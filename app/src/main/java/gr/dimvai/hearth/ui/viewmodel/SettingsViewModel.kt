package gr.dimvai.hearth.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gr.dimvai.hearth.data.local.SettingsDataStore
import gr.dimvai.hearth.notifications.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 10,
    val reminderMinute: Int = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    val uiState: StateFlow<SettingsState> = combine(
        settingsDataStore.remindersEnabled,
        settingsDataStore.reminderHour,
        settingsDataStore.reminderMinute
    ) { enabled, hour, minute ->
        SettingsState(enabled, hour, minute)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

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
