package com.example.wellnesstracker

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wellnesstracker.services.StepCounterService
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class StepsFragment : Fragment() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var textStepCount: TextView
    private lateinit var textProgressPercentage: TextView
    private lateinit var textGoalInfo: TextView
    private lateinit var textDistance: TextView
    private lateinit var textCalories: TextView
    private lateinit var progressSteps: ProgressBar
    private lateinit var buttonAddSteps: Button
    private lateinit var buttonSetGoal: Button
    private lateinit var buttonReset: Button
    private lateinit var achievementBanner: androidx.cardview.widget.CardView
    private lateinit var textRealtimeStatus: TextView
    private lateinit var statusIndicator: View

    private var stepUpdateReceiver: BroadcastReceiver? = null
    private var updateHandler: android.os.Handler? = null
    private var updateRunnable: Runnable? = null

    private val NOTIFICATION_PERMISSION_CODE = 1001
    private var hasSensorSupport = false

    companion object {
        const val STEP_CHANNEL_ID = "step_counter_channel"
        const val GOAL_CHANNEL_ID = "goal_achievement_channel"
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

        try {
            prefsHelper = SharedPreferencesHelper(requireContext())
            checkSensorSupport()
            createNotificationChannels()
            initializeViews(view)
            setupStepCounter()
            updateUI()
            updateSensorStatus()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkSensorSupport() {
        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        hasSensorSupport = stepSensor != null
    }

    private fun initializeViews(view: View) {
        try {
            textStepCount = view.findViewById(R.id.text_step_count)
            textProgressPercentage = view.findViewById(R.id.text_progress_percentage)
            textGoalInfo = view.findViewById(R.id.text_goal_info)
            textDistance = view.findViewById(R.id.text_distance)
            textCalories = view.findViewById(R.id.text_calories)
            progressSteps = view.findViewById(R.id.progress_steps)
            buttonAddSteps = view.findViewById(R.id.button_add_steps)
            buttonSetGoal = view.findViewById(R.id.button_set_goal)
            buttonReset = view.findViewById(R.id.button_reset)
            achievementBanner = view.findViewById(R.id.achievement_banner)
            textRealtimeStatus = view.findViewById(R.id.text_realtime_status)
            statusIndicator = view.findViewById(R.id.status_indicator)

            buttonAddSteps.setOnClickListener { showAddStepsDialog() }
            buttonSetGoal.setOnClickListener { showSetGoalDialog() }
            buttonReset.setOnClickListener { showResetDialog() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSensorStatus() {
        try {
            if (hasSensorSupport) {
                textRealtimeStatus.text = "Real-time counting active"
                textRealtimeStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_accent))
                statusIndicator.setBackgroundResource(R.drawable.status_indicator_active)
            } else {
                textRealtimeStatus.text = "Sensor unavailable - Use manual mode"
                textRealtimeStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_accent))
                statusIndicator.setBackgroundResource(R.drawable.status_indicator_inactive)

                if (!prefsHelper.hasShownSensorWarning()) {
                    showSensorUnavailableDialog()
                    prefsHelper.setShownSensorWarning(true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSensorUnavailableDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Step Sensor Unavailable")
            .setMessage("Your device doesn't have a step counter sensor. You can still track steps manually using the 'Add Steps' button.\n\nThis is common on emulators and some devices.")
            .setPositiveButton("Got it") { _, _ -> }
            .setIcon(R.drawable.ic_steps)
            .show()
    }

    private fun setupStepCounter() {
        if (hasSensorSupport) {
            checkNotificationPermissionAndStartService()
        } else {
            showManualModeNotification()
        }
    }

    private fun showManualModeNotification() {
        try {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (notificationManager != null) {
                val notification = NotificationCompat.Builder(requireContext(), STEP_CHANNEL_ID)
                    .setContentTitle("📱 Manual Mode Active")
                    .setContentText("Sensor not available. Use 'Add Steps' button to track.")
                    .setSmallIcon(R.drawable.ic_steps)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(2001, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkNotificationPermissionAndStartService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                } else {
                    startStepCounterService()
                }
            } else {
                startStepCounterService()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounterService()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission required for step tracking",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startStepCounterService() {
        try {
            StepCounterService.startService(requireContext())
            showStepCounterActivatedNotification()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Service error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStepCounterActivatedNotification() {
        try {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (notificationManager != null) {
                val notification = NotificationCompat.Builder(requireContext(), STEP_CHANNEL_ID)
                    .setContentTitle("✓ Step Counter Activated")
                    .setContentText("Tracking your steps in the background")
                    .setSmallIcon(R.drawable.ic_steps)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(2001, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateUI()
            registerStepUpdateReceiver()
            startPeriodicUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterStepUpdateReceiver()
            stopPeriodicUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerStepUpdateReceiver() {
        try {
            stepUpdateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == StepCounterService.STEP_UPDATE_ACTION) {
                        val steps = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0)
                        updateUIWithSteps(steps)
                    }
                }
            }

            val filter = IntentFilter(StepCounterService.STEP_UPDATE_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(stepUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                requireContext().registerReceiver(stepUpdateReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterStepUpdateReceiver() {
        try {
            stepUpdateReceiver?.let {
                requireContext().unregisterReceiver(it)
                stepUpdateReceiver = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPeriodicUpdate() {
        updateHandler = android.os.Handler(android.os.Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                updateUI()
                updateHandler?.postDelayed(this, 1000)
            }
        }
        updateHandler?.post(updateRunnable!!)
    }

    private fun stopPeriodicUpdate() {
        updateRunnable?.let {
            updateHandler?.removeCallbacks(it)
        }
        updateHandler = null
        updateRunnable = null
    }

    private fun updateUI() {
        try {
            val steps = prefsHelper.getSteps()
            updateUIWithSteps(steps)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUIWithSteps(steps: Int) {
        try {
            val goal = prefsHelper.getStepsGoal()
            val percentage = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0

            textStepCount.text = steps.toString()
            textProgressPercentage.text = "$percentage%"
            textGoalInfo.text = "Goal: ${goal.formatNumber()} steps"

            progressSteps.max = goal
            progressSteps.progress = steps

            val distance = steps * 0.0008
            textDistance.text = String.format("%.2f km", distance)

            val calories = (steps * 0.04).toInt()
            textCalories.text = "$calories cal"

            if (prefsHelper.isGoalAchievedToday()) {
                achievementBanner.visibility = View.VISIBLE
            } else {
                achievementBanner.visibility = View.GONE
            }

            checkGoalAchievement(steps, goal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkGoalAchievement(steps: Int, goal: Int) {
        try {
            if (steps >= goal && !prefsHelper.isGoalAchievedToday()) {
                prefsHelper.setGoalAchievedToday(true)
                showGoalAchievedNotification(steps)
                vibratePhone()
                achievementBanner.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showGoalAchievedNotification(steps: Int) {
        try {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (notificationManager != null) {
                val notification = NotificationCompat.Builder(requireContext(), GOAL_CHANNEL_ID)
                    .setContentTitle("🎉 Goal Achieved!")
                    .setContentText("Congratulations! You reached $steps steps today!")
                    .setSmallIcon(R.drawable.ic_trophy)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                    .build()

                notificationManager.notify(2002, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibratePhone() {
        try {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(3000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateOnSuccess() {
        try {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 100, 100, 200)
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    val pattern = longArrayOf(0, 100, 100, 200)
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannels() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                if (notificationManager != null) {
                    val stepChannel = NotificationChannel(
                        STEP_CHANNEL_ID,
                        "Step Counter",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notifications for step counter activation"
                        enableVibration(false)
                    }

                    val goalChannel = NotificationChannel(
                        GOAL_CHANNEL_ID,
                        "Goal Achievements",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications when you achieve your daily goal"
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    }

                    notificationManager.createNotificationChannel(stepChannel)
                    notificationManager.createNotificationChannel(goalChannel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAddStepsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_steps, null)

        val editSteps = dialogView.findViewById<EditText>(R.id.edit_steps)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Steps Manually")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val steps = editSteps.text.toString().toIntOrNull() ?: 0
                if (steps > 0) {
                    val currentSteps = prefsHelper.getSteps()
                    prefsHelper.setSteps(currentSteps + steps)
                    updateUI()
                    vibrateOnSuccess()
                    Toast.makeText(
                        requireContext(),
                        "✓ $steps steps added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_set_goal, null)

        val editGoal = dialogView.findViewById<EditText>(R.id.edit_goal)
        editGoal.setText(prefsHelper.getStepsGoal().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Set Daily Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val goal = editGoal.text.toString().toIntOrNull() ?: 10000
                if (goal > 0) {
                    prefsHelper.setStepsGoal(goal)
                    updateUI()
                    Toast.makeText(
                        requireContext(),
                        "Goal set to $goal steps",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Steps")
            .setMessage("Are you sure you want to reset today's steps?")
            .setPositiveButton("Reset") { _, _ ->
                prefsHelper.resetSteps()
                prefsHelper.setGoalAchievedToday(false)
                prefsHelper.setFirstTime(true)

                updateUI()
                updateStepNotification(0)

                Toast.makeText(requireContext(), "✓ Steps reset!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStepNotification(steps: Int) {
        try {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (notificationManager != null) {
                val goal = prefsHelper.getStepsGoal()

                val intent = Intent(requireContext(), MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    requireContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(requireContext(), StepCounterService.CHANNEL_ID)
                    .setContentTitle("Steps: $steps / $goal")
                    .setContentText("0% of daily goal • Keep moving!")
                    .setSmallIcon(R.drawable.ic_steps)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, 0, false)
                    .build()

                notificationManager.notify(StepCounterService.NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Int.formatNumber(): String {
        return String.format("%,d", this)
    }
}
