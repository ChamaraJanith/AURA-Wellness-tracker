package com.example.wellnesstracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wellnesstracker.utils.SharedPreferencesHelper
import kotlinx.coroutines.*

class HydrationReminderService : Service() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "hydration_reminder_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, HydrationReminderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, HydrationReminderService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefsHelper = SharedPreferencesHelper(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (prefsHelper.isHydrationReminderEnabled()) {
            startForeground(NOTIFICATION_ID, createServiceNotification())
            startHydrationReminders()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startHydrationReminders() {
        serviceJob = serviceScope.launch {
            while (isActive && prefsHelper.isHydrationReminderEnabled()) {
                try {
                    val intervalMinutes = prefsHelper.getHydrationReminderInterval()
                    delay(intervalMinutes * 60 * 1000L) // Convert minutes to milliseconds

                    if (isActive && prefsHelper.isHydrationReminderEnabled()) {
                        showHydrationNotification()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showHydrationNotification() {
        val currentCount = prefsHelper.getHydrationCount()
        val goal = prefsHelper.getHydrationGoal()
        val remaining = maxOf(0, goal - currentCount)

        val title = if (remaining > 0) {
            "💧 Time to Hydrate!"
        } else {
            "🎉 Hydration Goal Achieved!"
        }

        val message = if (remaining > 0) {
            "You have $remaining glasses left to reach your daily goal of $goal glasses."
        } else {
            "Great job! You've reached your hydration goal for today."
        }

        // Intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action to mark water as consumed
        val drinkIntent = Intent(this, HydrationActionReceiver::class.java).apply {
            action = "DRINK_WATER"
        }
        val drinkPendingIntent = PendingIntent.getBroadcast(
            this, 0, drinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_water_drop, "Drink Water", drinkPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createServiceNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hydration Reminder Active")
            .setContentText("Monitoring your hydration goals")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications to remind you to stay hydrated"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}