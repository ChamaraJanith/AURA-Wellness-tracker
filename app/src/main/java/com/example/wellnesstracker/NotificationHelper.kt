package com.example.wellnesstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    companion object {
        const val HABIT_REMINDER_CHANNEL = "habit_reminders"
        const val ACHIEVEMENT_CHANNEL = "achievements"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val habitChannel = NotificationChannel(
                HABIT_REMINDER_CHANNEL,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your daily habits"
            }

            val achievementChannel = NotificationChannel(
                ACHIEVEMENT_CHANNEL,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for achievements and milestones"
            }

            notificationManager.createNotificationChannel(habitChannel)
            notificationManager.createNotificationChannel(achievementChannel)
        }
    }

    fun showHabitReminder(habitName: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HABIT_REMINDER_CHANNEL)
            .setSmallIcon(R.drawable.ic_habits)
            .setContentTitle("Habit Reminder")
            .setContentText("Don't forget to complete: $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showAchievement(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL)
            .setSmallIcon(R.drawable.ic_habits)
            .setContentTitle("🎉 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showDailyProgress(completed: Int, total: Int) {
        if (completed == total && total > 0) {
            showAchievement(
                "All Habits Completed!",
                "You've completed all $total habits today. Great job!"
            )
        } else if (completed > 0) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, HABIT_REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_habits)
                .setContentTitle("Progress Update")
                .setContentText("$completed of $total habits completed today")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(999, notification)
        }
    }
}