package com.example.wellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.models.MoodEntry
import com.example.wellnesstracker.models.StepHistoryItem  // Add this missing import
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("WellnessTrackerPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today: String get() = dateFormat.format(Date())

    // Hydration tracking methods
    fun getHydrationCount(): Int {
        val lastUpdateDate = prefs.getString("hydration_last_update", "")

        // Reset count if last update was not today
        if (lastUpdateDate != today) {
            prefs.edit().putInt("hydration_count", 0).apply()
            prefs.edit().putString("hydration_last_update", today).apply()
            return 0
        }

        return prefs.getInt("hydration_count", 0)
    }

    fun getHydrationGoal(): Int = prefs.getInt("hydration_goal", 8)

    fun incrementHydrationCount() {
        val currentCount = getHydrationCount()
        prefs.edit().putInt("hydration_count", currentCount + 1).apply()
        prefs.edit().putString("hydration_last_update", today).apply()
    }

    // Hydration reminder methods
    fun isHydrationReminderEnabled(): Boolean = prefs.getBoolean("hydration_reminder_enabled", false)
    fun getHydrationReminderInterval(): Int = prefs.getInt("hydration_reminder_interval", 60)
    fun setHydrationReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("hydration_reminder_enabled", enabled).apply()
    }
    fun setHydrationReminderInterval(interval: Int) {
        prefs.edit().putInt("hydration_reminder_interval", interval).apply()
    }

    // Steps tracking methods
    fun getSteps(): Int {
        val lastUpdateDate = prefs.getString("steps_last_update", "")

        // Reset count if last update was not today
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

    // Step history methods
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

    // Store the initial step count when sensor is first registered
    fun getInitialStepCount(): Int = prefs.getInt("initial_step_count", 0)
    fun setInitialStepCount(count: Int) {
        prefs.edit().putInt("initial_step_count", count).apply()
    }

    fun isFirstTime(): Boolean = prefs.getBoolean("first_time", true)
    fun setFirstTime(firstTime: Boolean) {
        prefs.edit().putBoolean("first_time", firstTime).apply()
    }

    // User name methods
    fun getUserName(): String = prefs.getString("user_name", "User") ?: "User"
    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    // Daily goal achieved tracking
    fun isGoalAchievedToday(): Boolean = prefs.getBoolean("goal_achieved_$today", false)
    fun setGoalAchievedToday(achieved: Boolean) {
        prefs.edit().putBoolean("goal_achieved_$today", achieved).apply()
    }

    // Habits methods
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

    // Mood entries methods
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
}
