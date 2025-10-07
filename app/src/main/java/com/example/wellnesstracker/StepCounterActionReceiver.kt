package com.example.wellnesstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wellnesstracker.services.StepCounterService
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class StepCounterActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "PAUSE_STEP_COUNTER" -> {
                val prefsHelper = SharedPreferencesHelper(context)
                prefsHelper.setStepCounterPaused(true)

                // Stop the service
                StepCounterService.stopService(context)

                Toast.makeText(
                    context,
                    "⏸️ Step counter paused",
                    Toast.LENGTH_SHORT
                ).show()
            }
            "RESUME_STEP_COUNTER" -> {
                val prefsHelper = SharedPreferencesHelper(context)
                prefsHelper.setStepCounterPaused(false)

                // Restart the service
                StepCounterService.startService(context)

                Toast.makeText(
                    context,
                    "▶️ Step counter resumed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
