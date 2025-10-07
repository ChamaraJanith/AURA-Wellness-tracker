package com.example.wellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.models.MoodEntry
import com.example.wellnesstracker.models.StepHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("WellnessTrackerPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today: String get() = dateFormat.format(Date())

    // ============ USER AUTHENTICATION ============

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun getUserName(): String = prefs.getString("user_name", "User") ?: "User"

    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""

    fun setUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getUserPassword(): String = prefs.getString("user_password", "") ?: ""

    fun setUserPassword(password: String) {
        prefs.edit().putString("user_password", password).apply()
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    // ============ HYDRATION TRACKING ============

    fun getHydrationCount(): Int {
        val lastUpdateDate = prefs.getString("hydration_last_update", "")

        if (lastUpdateDate != today) {
            prefs.edit().putInt("hydration_count", 0).apply()
            prefs.edit().putString("hydration_last_update", today).apply()
            return 0
        }

        return prefs.getInt("hydration_count", 0)
    }

    fun saveHydrationCount(count: Int) {
        prefs.edit().putInt("hydration_count", count).apply()
        prefs.edit().putString("hydration_last_update", today).apply()
    }

    fun incrementHydrationCount() {
        val currentCount = getHydrationCount()
        saveHydrationCount(currentCount + 1)
    }

    fun getHydrationGoal(): Int = prefs.getInt("hydration_goal", 8)

    fun saveHydrationGoal(goal: Int) {
        prefs.edit().putInt("hydration_goal", goal).apply()
    }

    // ============ HYDRATION REMINDER ============

    fun isHydrationReminderEnabled(): Boolean =
        prefs.getBoolean("hydration_reminder_enabled", false)

    fun setHydrationReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("hydration_reminder_enabled", enabled).apply()
    }

    fun getHydrationReminderInterval(): Int =
        prefs.getInt("hydration_reminder_interval", 60)

    fun setHydrationReminderInterval(interval: Int) {
        prefs.edit().putInt("hydration_reminder_interval", interval).apply()
    }

    // ============ STEPS TRACKING ============

    fun getSteps(): Int {
        val lastUpdateDate = prefs.getString("steps_last_update", "")

        if (lastUpdateDate != today) {
            prefs.edit().putInt("steps_count", 0).apply()
            prefs.edit().putString("steps_last_update", today).apply()
            return 0
        }

        return prefs.getInt("steps_count", 0)
    }

    fun setSteps(steps: Int) {
        prefs.edit().putInt("steps_count", steps).apply()
        prefs.edit().putString("steps_last_update", today).apply()
    }

    fun incrementSteps(steps: Int) {
        val currentSteps = getSteps()
        setSteps(currentSteps + steps)
    }

    fun getStepsGoal(): Int = prefs.getInt("steps_goal", 10000)

    fun setStepsGoal(goal: Int) {
        prefs.edit().putInt("steps_goal", goal).apply()
    }

    fun resetSteps() {
        setSteps(0)
    }

    // ============ STEP HISTORY ============

    fun getStepHistory(): List<StepHistoryItem> {
        val json = prefs.getString("step_history", null)
        return if (json != null) {
            val type = object : TypeToken<List<StepHistoryItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveStepHistory(history: List<StepHistoryItem>) {
        val json = gson.toJson(history)
        prefs.edit().putString("step_history", json).apply()
    }

    fun addStepHistoryEntry(entry: StepHistoryItem) {
        val currentHistory = getStepHistory().toMutableList()
        currentHistory.add(entry)
        saveStepHistory(currentHistory)
    }

    // ============ STEP SENSOR ============

    fun getInitialStepCount(): Int = prefs.getInt("initial_step_count", 0)

    fun setInitialStepCount(count: Int) {
        prefs.edit().putInt("initial_step_count", count).apply()
    }

    fun isFirstTime(): Boolean = prefs.getBoolean("first_time", true)

    fun setFirstTime(firstTime: Boolean) {
        prefs.edit().putBoolean("first_time", firstTime).apply()
    }

    fun hasShownSensorWarning(): Boolean =
        prefs.getBoolean("shown_sensor_warning", false)

    fun setShownSensorWarning(shown: Boolean) {
        prefs.edit().putBoolean("shown_sensor_warning", shown).apply()
    }

    fun isStepCounterPaused(): Boolean =
        prefs.getBoolean("step_counter_paused", false)

    fun setStepCounterPaused(paused: Boolean) {
        prefs.edit().putBoolean("step_counter_paused", paused).apply()
    }

    // ============ DAILY GOALS ============

    fun isGoalAchievedToday(): Boolean =
        prefs.getBoolean("goal_achieved_$today", false)

    fun setGoalAchievedToday(achieved: Boolean) {
        prefs.edit().putBoolean("goal_achieved_$today", achieved).apply()
    }

    // ============ HABITS ============

    fun getHabits(): List<Habit> {
        val json = prefs.getString("habits_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits_list", json).apply()
    }

    // ============ MOOD ENTRIES ============

    fun getMoodEntries(): List<MoodEntry> {
        val json = prefs.getString("mood_entries", null)
        return if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        val json = gson.toJson(moodEntries)
        prefs.edit().putString("mood_entries", json).apply()
    }

    // ============ ONBOARDING ============

    fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean("onboarding_completed", false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    // ============ DAILY RESET ============

    fun getLastResetDate(): String = prefs.getString("last_reset_date", "") ?: ""

    fun saveLastResetDate(date: String) {
        prefs.edit().putString("last_reset_date", date).apply()
    }

    fun resetDailyData() {
        prefs.edit()
            .putInt("steps_count", 0)
            .putInt("hydration_count", 0)
            .putString("steps_last_update", today)
            .putString("hydration_last_update", today)
            .apply()
    }

    // ============ CLEAR DATA ============

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
