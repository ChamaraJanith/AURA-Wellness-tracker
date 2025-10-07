package com.example.wellnesstracker

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.text.DecimalFormat

class StepsFragment : Fragment(), SensorEventListener {

    private val viewModel: HomeViewModel by activityViewModels()

    // UI Components
    private lateinit var textStepCount: TextView
    private lateinit var textProgressPercentage: TextView
    private lateinit var textGoalInfo: TextView
    private lateinit var textDistance: TextView
    private lateinit var textCalories: TextView
    private lateinit var textRealtimeStatus: TextView
    private lateinit var progressSteps: ProgressBar
    private lateinit var buttonAddSteps: Button
    private lateinit var buttonSetGoal: Button
    private lateinit var buttonReset: Button
    private lateinit var achievementBanner: View
    private lateinit var statusIndicator: View

    // Sensor components
    private var sensorManager: SensorManager? = null
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var isListening = false
    private var lastStepCount = 0
    private var sensorInitialSteps = 0
    private var isFirstSensorReading = true

    companion object {
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_steps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupObservers()
        setupRealTimeStepDetector()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        try {
            textStepCount = view.findViewById(R.id.text_step_count)
            textProgressPercentage = view.findViewById(R.id.text_progress_percentage)
            textGoalInfo = view.findViewById(R.id.text_goal_info)
            textDistance = view.findViewById(R.id.text_distance)
            textCalories = view.findViewById(R.id.text_calories)
            textRealtimeStatus = view.findViewById(R.id.text_realtime_status)
            progressSteps = view.findViewById(R.id.progress_steps)
            buttonAddSteps = view.findViewById(R.id.button_add_steps)
            buttonSetGoal = view.findViewById(R.id.button_set_goal)
            buttonReset = view.findViewById(R.id.button_reset)
            achievementBanner = view.findViewById(R.id.achievement_banner)
            statusIndicator = view.findViewById(R.id.status_indicator)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        // Observe steps data from ViewModel
        viewModel.stepsData.observe(viewLifecycleOwner) { steps ->
            updateStepsDisplay(steps.current, steps.goal)
        }

        // Observe distance
        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            val df = DecimalFormat("#.##")
            textDistance.text = "${df.format(distance)} km"
        }

        // Observe calories
        viewModel.caloriesData.observe(viewLifecycleOwner) { calories ->
            textCalories.text = "${calories.current} cal"
        }
    }

    private fun setupClickListeners() {
        buttonAddSteps.setOnClickListener { showAddStepsDialog() }
        buttonSetGoal.setOnClickListener { showSetGoalDialog() }
        buttonReset.setOnClickListener { showResetConfirmation() }
    }

    private fun setupRealTimeStepDetector() {
        try {
            sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

            stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            if (stepDetectorSensor == null && stepCounterSensor == null) {
                updateSensorStatus(false)
                return
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION
                )
            } else {
                startRealTimeStepCounting()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error setting up sensors: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRealTimeStepCounting() {
        try {
            var sensorsRegistered = 0

            stepDetectorSensor?.let { sensor ->
                val success = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                if (success == true) {
                    sensorsRegistered++
                }
            }

            stepCounterSensor?.let { sensor ->
                val success = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                if (success == true) {
                    sensorsRegistered++
                }
            }

            if (sensorsRegistered > 0) {
                isListening = true
                updateSensorStatus(true)
                Toast.makeText(
                    requireContext(),
                    "Real-time step counting started!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                updateSensorStatus(false)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error starting step counting: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSensorStatus(isActive: Boolean) {
        try {
            if (isActive) {
                textRealtimeStatus.text = "Real-time counting active"
                textRealtimeStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            } else {
                textRealtimeStatus.text = "Sensor not available"
                textRealtimeStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
        } catch (e: Exception) {
            // Ignore if views not available
        }
    }

    private fun stopRealTimeStepCounting() {
        if (isListening) {
            try {
                sensorManager?.unregisterListener(this)
                isListening = false
                updateSensorStatus(false)
            } catch (e: Exception) {
                // Ignore errors during cleanup
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try {
            event?.let { sensorEvent ->
                when (sensorEvent.sensor.type) {
                    Sensor.TYPE_STEP_DETECTOR -> {
                        handleStepDetected()
                    }
                    Sensor.TYPE_STEP_COUNTER -> {
                        handleStepCounter(sensorEvent.values[0].toInt())
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore sensor errors
        }
    }

    private fun handleStepDetected() {
        try {
            val currentSteps = viewModel.stepsData.value?.current ?: 0
            viewModel.updateSteps(currentSteps + 1)
        } catch (e: Exception) {
            // Ignore if context not available
        }
    }

    private fun handleStepCounter(totalSteps: Int) {
        try {
            if (isFirstSensorReading) {
                sensorInitialSteps = totalSteps
                lastStepCount = totalSteps
                isFirstSensorReading = false
                return
            }

            val newSteps = totalSteps - lastStepCount
            if (newSteps > 0 && newSteps < 100) { // Validate reasonable step increment
                val currentSteps = viewModel.stepsData.value?.current ?: 0
                viewModel.updateSteps(currentSteps + newSteps)
                lastStepCount = totalSteps
            }
        } catch (e: Exception) {
            // Ignore if context not available
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRealTimeStepCounting()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission required for real-time step counting",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateStepsDisplay(count: Int, goal: Int) {
        try {
            // Update main step count
            textStepCount.text = formatNumber(count)

            // Update progress percentage
            val percentage = if (goal > 0) (count * 100) / goal else 0
            textProgressPercentage.text = "$percentage%"

            // Update goal info
            textGoalInfo.text = "Goal: ${formatNumber(goal)} steps"

            // Update progress bar
            progressSteps.max = goal
            progressSteps.progress = minOf(count, goal)

            // Check if goal is reached
            if (count >= goal && count > 0) {
                showAchievementBanner()
            } else {
                hideAchievementBanner()
            }

        } catch (e: Exception) {
            // Handle display update errors gracefully
        }
    }

    private fun showAchievementBanner() {
        try {
            achievementBanner.visibility = View.VISIBLE
        } catch (e: Exception) {
            // Ignore if view not available
        }
    }

    private fun hideAchievementBanner() {
        try {
            achievementBanner.visibility = View.GONE
        } catch (e: Exception) {
            // Ignore if view not available
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    private fun showAddStepsDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_steps, null)
            val editSteps = dialogView.findViewById<EditText>(R.id.edit_steps)

            AlertDialog.Builder(requireContext())
                .setTitle("Add Steps Manually")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val stepsText = editSteps.text.toString().trim()
                    if (stepsText.isNotEmpty()) {
                        val steps = stepsText.toIntOrNull() ?: 0
                        if (steps > 0) {
                            val currentSteps = viewModel.stepsData.value?.current ?: 0
                            viewModel.updateSteps(currentSteps + steps)
                            Toast.makeText(
                                requireContext(),
                                "Added ${formatNumber(steps)} steps!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error showing dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSetGoalDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_goal, null)
            val editGoal = dialogView.findViewById<EditText>(R.id.edit_goal)

            val currentGoal = viewModel.stepsData.value?.goal ?: 10000
            editGoal.setText(currentGoal.toString())

            AlertDialog.Builder(requireContext())
                .setTitle("Set Daily Goal")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val goalText = editGoal.text.toString().trim()
                    if (goalText.isNotEmpty()) {
                        val goal = goalText.toIntOrNull() ?: 10000
                        if (goal > 0) {
                            viewModel.updateStepsGoal(goal)
                            Toast.makeText(
                                requireContext(),
                                "Daily goal set to ${formatNumber(goal)} steps!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error showing dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResetConfirmation() {
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Steps")
                .setMessage("Are you sure you want to reset your step count for today?")
                .setPositiveButton("Reset") { _, _ ->
                    viewModel.updateSteps(0)
                    isFirstSensorReading = true
                    Toast.makeText(
                        requireContext(),
                        "Steps reset for today",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error showing dialog", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (!isListening && (stepDetectorSensor != null || stepCounterSensor != null)) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startRealTimeStepCounting()
                }
            }
            viewModel.refreshData()
        } catch (e: Exception) {
            // Handle resume errors gracefully
        }
    }

    override fun onPause() {
        super.onPause()
        // Keep counting in background for real-time detection
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRealTimeStepCounting()
    }
}
