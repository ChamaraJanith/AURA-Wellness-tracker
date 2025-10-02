package com.example.wellnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class HydrationFragment : Fragment() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var textWaterProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonAddWater: Button
    private lateinit var buttonSetReminder: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = SharedPreferencesHelper(requireContext())

        initializeViews(view)
        updateWaterProgress()
    }

    private fun initializeViews(view: View) {
        textWaterProgress = view.findViewById(R.id.text_water_progress)
        progressBar = view.findViewById(R.id.progress_water)
        buttonAddWater = view.findViewById(R.id.button_add_water)
        buttonSetReminder = view.findViewById(R.id.button_set_reminder)

        buttonAddWater.setOnClickListener {
            prefsHelper.incrementHydrationCount()
            updateWaterProgress()
        }

        buttonSetReminder.setOnClickListener {
            showReminderSettingsDialog()
        }
    }

    private fun updateWaterProgress() {
        val goal = prefsHelper.getHydrationGoal()
        val count = prefsHelper.getHydrationCount()
        textWaterProgress.text = "$count / $goal glasses today"
        progressBar.max = goal
        progressBar.progress = count

        // Check if goal is reached
        if (count >= goal) {
            android.widget.Toast.makeText(requireContext(), "Congratulations! You've reached your hydration goal for today!", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun showReminderSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_hydration_settings, null)
        val switchReminder = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_reminder)
        val spinnerInterval = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_interval)

        // Set current values
        switchReminder.isChecked = prefsHelper.isHydrationReminderEnabled()

        val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours")
        val currentInterval = prefsHelper.getHydrationReminderInterval()
        val intervalPosition = when (currentInterval) {
            30 -> 0
            60 -> 1
            120 -> 2
            180 -> 3
            else -> 1
        }

        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapter
        spinnerInterval.setSelection(intervalPosition)

        AlertDialog.Builder(requireContext())
            .setTitle("Hydration Reminder Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val isEnabled = switchReminder.isChecked
                val intervalText = intervals[spinnerInterval.selectedItemPosition]
                val intervalMinutes = when (intervalText) {
                    "30 minutes" -> 30
                    "1 hour" -> 60
                    "2 hours" -> 120
                    "3 hours" -> 180
                    else -> 60
                }

                prefsHelper.setHydrationReminderEnabled(isEnabled)
                prefsHelper.setHydrationReminderInterval(intervalMinutes)

                if (isEnabled) {
                    HydrationReminderService.startService(requireContext())
                    android.widget.Toast.makeText(requireContext(), "Reminders enabled!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    HydrationReminderService.stopService(requireContext())
                    android.widget.Toast.makeText(requireContext(), "Reminders disabled", android.widget.Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}