package com.example.wellnesstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class HydrationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "DRINK_WATER") {
            val prefsHelper = SharedPreferencesHelper(context)
            prefsHelper.incrementHydrationCount()

            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(HydrationReminderService.NOTIFICATION_ID + 1)

            // Show a confirmation toast
            android.widget.Toast.makeText(context, "Great! One glass of water added!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}