package gr.dimvai.hearth.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import gr.dimvai.hearth.HearthApplication
import gr.dimvai.hearth.R
import gr.dimvai.hearth.data.local.SettingsDataStore
import gr.dimvai.hearth.data.model.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val application = context.applicationContext as HearthApplication
        val repository = application.repository
        val settings = SettingsDataStore(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Check if reminders are still enabled
            if (!settings.remindersEnabled.first()) return@launch

            // Check if we already notified today
            val today = LocalDate.now().toString()
            if (settings.lastNotifiedDate.first() == today) return@launch

            val connections = repository.allConnections.first()
            val dueConnections = connections.filter { it.isOverdue || it.isToday }

            if (dueConnections.isNotEmpty()) {
                showNotification(context, dueConnections)
                settings.saveLastNotifiedDate(today)
            }
            
            // Reschedule for tomorrow
            val hour = settings.reminderHour.first()
            val minute = settings.reminderMinute.first()
            NotificationScheduler.schedule(context, hour, minute)
        }
    }

    private fun showNotification(context: Context, dueConnections: List<Connection>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hearth_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hearth Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (dueConnections.size == 1) {
            "Εκκρεμεί επικοινωνία με ${dueConnections.first().name}"
        } else {
            "Εκκρεμούν ${dueConnections.size} επικοινωνίες"
        }

        val names = dueConnections.take(3).joinToString(", ") { it.name }
        val body = if (dueConnections.size > 3) "$names και άλλοι..." else names

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}
