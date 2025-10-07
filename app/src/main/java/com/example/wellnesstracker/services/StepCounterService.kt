package com.example.wellnesstracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.wellnesstracker.MainActivity
import com.example.wellnesstracker.R
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var vibrator: Vibrator
    private lateinit var notificationManager: NotificationManager
    private var initialStepCount = 0
    private var lastGoalAchievedSteps = 0
    private var isEmulator = false
    private var lastStepCount = 0
    private var lastVibrationTime = 0L

    companion object {
        const val CHANNEL_ID = "step_counter_service_channel"
        const val NOTIFICATION_ID = 1002
        private const val TAG = "StepCounterService"
        private const val VIBRATION_INTERVAL = 1000L

        // Broadcast action
        const val STEP_UPDATE_ACTION = "com.example.wellnesstracker.STEP_UPDATE"
        const val EXTRA_STEP_COUNT = "step_count"

        fun startService(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefsHelper = SharedPreferencesHelper(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        isEmulator = isRunningOnEmulator()
        createNotificationChannel()

        if (stepCounterSensor == null) {
            Log.w(TAG, "Step counter sensor not available (likely emulator)")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createServiceNotification())

        if (stepCounterSensor != null && !isEmulator) {
            sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Step counter sensor registered")
        } else {
            Log.w(TAG, "Running in emulator mode - sensor simulation active")
            showEmulatorModeNotification()
        }

        return START_STICKY
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    private fun showEmulatorModeNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Emulator Mode")
            .setContentText("Step counter sensor not available. Using manual mode.")
            .setSmallIcon(R.drawable.ic_steps)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(3001, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            try {
                val totalSteps = event.values[0].toInt()

                if (prefsHelper.isFirstTime()) {
                    prefsHelper.setInitialStepCount(totalSteps)
                    prefsHelper.setFirstTime(false)
                    initialStepCount = totalSteps
                } else {
                    initialStepCount = prefsHelper.getInitialStepCount()
                }

                val currentSteps = totalSteps - initialStepCount

                // Vibrate when new step is detected
                if (currentSteps > lastStepCount) {
                    vibrateOnStep()
                    lastStepCount = currentSteps
                }

                prefsHelper.setSteps(currentSteps)

                // Broadcast step update to fragment
                broadcastStepUpdate(currentSteps)

                // Update notification in real-time
                updateNotification(currentSteps)

                // Check goal achievement
                checkGoalAchievement(currentSteps)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onSensorChanged: ${e.message}")
            }
        }
    }

    private fun broadcastStepUpdate(steps: Int) {
        try {
            val intent = Intent(STEP_UPDATE_ACTION).apply {
                putExtra(EXTRA_STEP_COUNT, steps)
            }
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting step update: ${e.message}")
        }
    }

    private fun vibrateOnStep() {
        try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastVibrationTime < VIBRATION_INTERVAL) {
                return
            }
            lastVibrationTime = currentTime

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating: ${e.message}")
        }
    }

    private fun checkGoalAchievement(steps: Int) {
        try {
            val goal = prefsHelper.getStepsGoal()

            if (steps >= goal && !prefsHelper.isGoalAchievedToday() && steps != lastGoalAchievedSteps) {
                prefsHelper.setGoalAchievedToday(true)
                lastGoalAchievedSteps = steps
                showGoalAchievedNotification(steps)
                vibrateOnGoalAchieved()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking goal: ${e.message}")
        }
    }

    private fun vibrateOnGoalAchieved() {
        try {
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(3000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating on goal: ${e.message}")
        }
    }

    private fun showGoalAchievedNotification(steps: Int) {
        try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "goal_achievement_channel")
                .setContentTitle("🎉 Goal Achieved!")
                .setContentText("Congratulations! You reached $steps steps today!")
                .setSmallIcon(R.drawable.ic_trophy)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "goal_achievement_channel",
                    "Goal Achievements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(2002, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing goal notification: ${e.message}")
        }
    }

    private fun updateNotification(steps: Int) {
        try {
            val notification = createServiceNotification(steps)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification: ${e.message}")
        }
    }

    private fun createServiceNotification(steps: Int = 0): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val goal = prefsHelper.getStepsGoal()
        val progress = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0

        val contentText = if (isEmulator) {
            "Emulator mode - Use manual add"
        } else {
            "$progress% of daily goal • Keep moving!"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Steps: $steps / $goal")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing step counting"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
