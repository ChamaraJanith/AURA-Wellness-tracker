package com.example.wellnesstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class HydrationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "DRINK_WATER") {
            val prefsHelper = SharedPreferencesHelper(context)
            val currentCount = prefsHelper.getHydrationCount()
            prefsHelper.saveHydrationCount(currentCount + 1)

            Toast.makeText(
                context,
                "✓ Water logged! ${currentCount + 1} glasses today",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
