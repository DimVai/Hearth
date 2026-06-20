package gr.dimvai.hearth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val LAST_NOTIFIED_DATE = stringPreferencesKey("last_notified_date")
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDERS_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_HOUR] ?: 10 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }
    val lastNotifiedDate: Flow<String?> = context.dataStore.data.map { it[LAST_NOTIFIED_DATE] }

    suspend fun saveRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDERS_ENABLED] = enabled }
    }

    suspend fun saveReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[REMINDER_HOUR] = hour
            it[REMINDER_MINUTE] = minute
        }
    }

    suspend fun saveLastNotifiedDate(date: String) {
        context.dataStore.edit { it[LAST_NOTIFIED_DATE] = date }
    }
}
