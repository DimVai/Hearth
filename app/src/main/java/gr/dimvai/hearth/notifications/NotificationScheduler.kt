package gr.dimvai.hearth.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import gr.dimvai.hearth.data.local.SettingsDataStore
import java.text.SimpleDateFormat
import java.util.*

object NotificationScheduler {

    suspend fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Χρησιμοποιούμε την inexact μέθοδο που επιτρέπεται στο Doze mode
        // αλλά δεν απαιτεί ειδικά δικαιώματα "Exact Alarm".
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Debug: Αποθήκευση της επόμενης προγραμματισμένης ώρας
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val formattedDate = sdf.format(calendar.time)
        SettingsDataStore(context).saveNextScheduledAlarm(formattedDate)
    }

    suspend fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        SettingsDataStore(context).saveNextScheduledAlarm(null)
    }
}
