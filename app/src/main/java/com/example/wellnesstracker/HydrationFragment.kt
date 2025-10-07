package com.example.wellnesstracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class HydrationFragment : Fragment() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var textWaterProgress: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var tvWaterGoal: TextView
    private lateinit var buttonAddWater: Button
    private lateinit var buttonSetReminder: Button

    private var waterCount = 0
    private var waterGoal = 8

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
        loadHydrationData()
        updateUI()

        buttonAddWater.setOnClickListener {
            addWaterGlass()
        }

        buttonSetReminder.setOnClickListener {
            showReminderSettingsDialog()
        }
    }

    private fun initializeViews(view: View) {
        textWaterProgress = view.findViewById(R.id.text_water_progress)
        progressWater = view.findViewById(R.id.progress_water)
        tvWaterGoal = view.findViewById(R.id.tv_water_goal)
        buttonAddWater = view.findViewById(R.id.button_add_water)
        buttonSetReminder = view.findViewById(R.id.button_set_reminder)
    }

    private fun loadHydrationData() {
        waterCount = prefsHelper.getHydrationCount()
        waterGoal = prefsHelper.getHydrationGoal()
    }

    private fun updateUI() {
        textWaterProgress.text = "$waterCount / $waterGoal"
        tvWaterGoal.text = "Goal: $waterGoal glasses"

        progressWater.max = waterGoal
        progressWater.progress = waterCount

        // Update button text based on reminder status
        if (prefsHelper.isHydrationReminderEnabled()) {
            buttonSetReminder.text = "⏰ Reminder Active"
        } else {
            buttonSetReminder.text = "⏰ Reminder Settings"
        }
    }

    private fun addWaterGlass() {
        waterCount++
        prefsHelper.saveHydrationCount(waterCount)
        updateUI()

        if (waterCount >= waterGoal) {
            Toast.makeText(
                requireContext(),
                "🎉 Congratulations! You've reached your hydration goal!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Great! ${waterGoal - waterCount} more to go!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showReminderSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_hydration_reminder, null)

        val switchReminder = dialogView.findViewById<Switch>(R.id.switch_reminder)
        val spinnerInterval = dialogView.findViewById<Spinner>(R.id.spinner_interval)
        val editGoal = dialogView.findViewById<EditText>(R.id.edit_goal)

        // Set current values
        switchReminder.isChecked = prefsHelper.isHydrationReminderEnabled()
        editGoal.setText(waterGoal.toString())

        // Interval options (in minutes)
        val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours", "4 hours")
        val intervalValues = arrayOf(30, 60, 120, 180, 240)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            intervals
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapter

        // Set current interval
        val currentInterval = prefsHelper.getHydrationReminderInterval()
        val intervalIndex = intervalValues.indexOf(currentInterval)
        if (intervalIndex >= 0) {
            spinnerInterval.setSelection(intervalIndex)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Hydration Reminder Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val isEnabled = switchReminder.isChecked
                val selectedInterval = intervalValues[spinnerInterval.selectedItemPosition]
                val newGoal = editGoal.text.toString().toIntOrNull() ?: 8

                // Save settings
                prefsHelper.setHydrationReminderEnabled(isEnabled)
                prefsHelper.setHydrationReminderInterval(selectedInterval)
                prefsHelper.saveHydrationGoal(newGoal)

                waterGoal = newGoal
                updateUI()

                // Start or stop service based on settings
                if (isEnabled) {
                    HydrationReminderService.startService(requireContext())
                    Toast.makeText(
                        requireContext(),
                        "Reminder enabled! You'll be notified every ${intervals[spinnerInterval.selectedItemPosition]}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    HydrationReminderService.stopService(requireContext())
                    Toast.makeText(
                        requireContext(),
                        "Reminder disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadHydrationData()
        updateUI()
    }
}
