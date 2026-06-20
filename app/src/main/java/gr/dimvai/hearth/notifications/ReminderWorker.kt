package gr.dimvai.hearth.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import gr.dimvai.hearth.HearthApplication
import gr.dimvai.hearth.R
import gr.dimvai.hearth.data.local.SettingsDataStore
import gr.dimvai.hearth.data.model.Connection
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as HearthApplication
        val repository = application.repository
        val settings = SettingsDataStore(applicationContext)

        // Check if reminders are still enabled
        if (!settings.remindersEnabled.first()) return Result.success()

        // Check if we already notified today
        val today = LocalDate.now().toString()
        if (settings.lastNotifiedDate.first() == today) return Result.success()

        val connections = repository.allConnections.first()
        val dueConnections = connections.filter { it.isOverdue || it.isToday }

        if (dueConnections.isNotEmpty()) {
            showNotification(dueConnections)
            settings.saveLastNotifiedDate(today)
        }

        return Result.success()
    }

    private fun showNotification(dueConnections: List<Connection>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hearth_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hearth Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (dueConnections.size == 1) {
            "Εκκρεμεί επικοινωνία με ${dueConnections.first().name}"
        } else {
            "Εκκρεμούν ${dueConnections.size} επικοινωνίες"
        }

        val names = dueConnections.take(3).joinToString(", ") { it.name }
        val body = if (dueConnections.size > 3) "$names και άλλοι..." else names

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logo) // Using the logo as icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        fun schedule(context: Context, hour: Int, minute: Int) {
            val workManager = WorkManager.getInstance(context)
            
            // Calculate delay until the target time
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("daily_reminder")
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()

            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
        }
    }
}
