package gr.dimvai.hearth.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import gr.dimvai.hearth.HearthApplication
import gr.dimvai.hearth.MainActivity
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
        val pendingResult = goAsync() // Κρατάμε τον Receiver "ζωντανό" για λίγο παραπάνω
        val application = context.applicationContext as HearthApplication
        val repository = application.repository
        val settings = SettingsDataStore(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Έλεγχος αν οι ειδοποιήσεις είναι ενεργές
                if (!settings.remindersEnabled.first()) return@launch

                // 2. Πάντα προγραμματίζουμε το επόμενο ραντεβού (για να μη σπάσει η αλυσίδα)
                val hour = settings.reminderHour.first()
                val minute = settings.reminderMinute.first()
                NotificationScheduler.schedule(context, hour, minute)

                // 3. Έλεγχος αν έχουμε ήδη στείλει ειδοποίηση σήμερα
                val today = LocalDate.now().toString()
                if (settings.lastNotifiedDate.first() == today) return@launch

                // 4. Έλεγχος αν υπάρχουν επαφές για επικοινωνία
                val connections = repository.allConnections.first()
                val dueConnections = connections.filter { it.isOverdue || it.isToday }

                if (dueConnections.isNotEmpty()) {
                    showNotification(context, dueConnections)
                    settings.saveLastNotifiedDate(today)
                }
            } finally {
                pendingResult.finish() // Ενημερώνουμε το σύστημα ότι τελειώσαμε
            }
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

        // Δημιουργούμε ένα Intent που θα ανοίγει την MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // Αν η εφαρμογή είναι ήδη ανοιχτή, απλώς τη φέρνει μπροστά
        }
        
        // Το PendingIntent είναι σαν ένα "token" που δίνουμε στο σύστημα Android 
        // για να εκτελέσει το Intent μας όταν ο χρήστης πατήσει το notification.
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent) // Εδώ συνδέουμε το κλικ με το Intent
            .setAutoCancel(true) // Αυτό κάνει το notification να εξαφανίζεται μόλις πατηθεί
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}
