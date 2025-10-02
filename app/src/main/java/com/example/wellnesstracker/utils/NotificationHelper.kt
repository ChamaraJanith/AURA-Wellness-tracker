package com.example.wellnesstracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.wellnesstracker.R

object NotificationHelper {
    private const val CHANNEL_ID = "step_goal_channel"
    private const val CHANNEL_NAME = "Step Goal Notifications"

    fun showStepGoalAchieved(context: Context, steps: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("🎉 Step Goal Achieved!")
            .setContentText("You've reached your goal of ${String.format("%,d", steps)} steps today. Great job!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        manager.notify(1001, builder.build())
    }
}
