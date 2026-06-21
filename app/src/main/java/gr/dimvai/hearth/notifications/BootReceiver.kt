package gr.dimvai.hearth.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import gr.dimvai.hearth.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settings = SettingsDataStore(context)
            CoroutineScope(Dispatchers.IO).launch {
                if (settings.remindersEnabled.first()) {
                    val hour = settings.reminderHour.first()
                    val minute = settings.reminderMinute.first()
                    NotificationScheduler.schedule(context, hour, minute)
                }
            }
        }
    }
}
