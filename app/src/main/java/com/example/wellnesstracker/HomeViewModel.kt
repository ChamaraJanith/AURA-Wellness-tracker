package com.example.wellnesstracker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StepsData(val current: Int, val goal: Int)
data class CaloriesData(val current: Int, val goal: Int)
data class WaterData(val current: Int, val goal: Int)
data class SleepData(val current: Float, val goal: Float)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("WellnessTrackerPrefs", Context.MODE_PRIVATE)

    // LiveData for UI
    private val _stepsData = MutableLiveData<StepsData>()
    val stepsData: LiveData<StepsData> = _stepsData

    private val _caloriesData = MutableLiveData<CaloriesData>()
    val caloriesData: LiveData<CaloriesData> = _caloriesData

    private val _waterData = MutableLiveData<WaterData>()
    val waterData: LiveData<WaterData> = _waterData

    private val _sleepData = MutableLiveData<SleepData>()
    val sleepData: LiveData<SleepData> = _sleepData

    private val _heartRateData = MutableLiveData<Int>()
    val heartRateData: LiveData<Int> = _heartRateData

    private val _activityMinutes = MutableLiveData<Int>()
    val activityMinutes: LiveData<Int> = _activityMinutes

    private val _distance = MutableLiveData<Float>()
    val distance: LiveData<Float> = _distance

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _completedHabitsToday = MutableLiveData<Int>()
    val completedHabitsToday: LiveData<Int> = _completedHabitsToday

    private val _totalHabits = MutableLiveData<Int>()
    val totalHabits: LiveData<Int> = _totalHabits

    private val _todayMood = MutableLiveData<String>()
    val todayMood: LiveData<String> = _todayMood

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            checkAndResetDailyData()

            val currentSteps = sharedPreferences.getInt("current_steps", 0)
            val stepsGoal = sharedPreferences.getInt("steps_goal", 10000)
            _stepsData.value = StepsData(currentSteps, stepsGoal)

            val currentCalories = calculateCaloriesFromSteps(currentSteps)
            val caloriesGoal = sharedPreferences.getInt("calories_goal", 2000)
            _caloriesData.value = CaloriesData(currentCalories, caloriesGoal)

            val currentWaterGlasses = sharedPreferences.getInt("current_water_glasses", 0)
            val waterGoal = sharedPreferences.getInt("water_goal_glasses", 8)
            _waterData.value = WaterData(currentWaterGlasses, waterGoal)

            val currentSleep = sharedPreferences.getFloat("current_sleep", 0f)
            val sleepGoal = sharedPreferences.getFloat("sleep_goal", 8f)
            _sleepData.value = SleepData(currentSleep, sleepGoal)

            val heartRate = sharedPreferences.getInt("heart_rate", 72)
            _heartRateData.value = heartRate

            val activityMin = calculateActivityMinutesFromSteps(currentSteps)
            _activityMinutes.value = activityMin

            val dist = calculateDistanceFromSteps(currentSteps)
            _distance.value = dist

            val name = sharedPreferences.getString("user_name", "User") ?: "User"
            _userName.value = name

            loadHabitsData()

            val todayMood = sharedPreferences.getString("today_mood", "Neutral") ?: "Neutral"
            _todayMood.value = todayMood
        }
    }

    private fun loadHabitsData() {
        val completedCount = sharedPreferences.getInt("completed_habits_today", 0)
        val totalCount = sharedPreferences.getInt("total_habits", 2)

        _completedHabitsToday.value = completedCount
        _totalHabits.value = totalCount
    }

    private fun checkAndResetDailyData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = sharedPreferences.getString("last_update_date", "")

        if (lastDate != today) {
            with(sharedPreferences.edit()) {
                putInt("current_steps", 0)
                putInt("current_water_glasses", 0)
                putInt("completed_habits_today", 0)
                putString("today_mood", "")
                putString("last_update_date", today)
                apply()
            }
        }
    }

    private fun calculateCaloriesFromSteps(steps: Int): Int {
        return (steps * 0.04).toInt()
    }

    private fun calculateActivityMinutesFromSteps(steps: Int): Int {
        return (steps / 100)
    }

    private fun calculateDistanceFromSteps(steps: Int): Float {
        return (steps * 0.000762f)
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun updateSteps(steps: Int) {
        sharedPreferences.edit().putInt("current_steps", steps).apply()
        loadDashboardData()
    }

    fun updateWaterGlasses(glasses: Int) {
        sharedPreferences.edit().putInt("current_water_glasses", glasses).apply()
        val goal = _waterData.value?.goal ?: 8
        _waterData.value = WaterData(glasses, goal)
        android.util.Log.d("HomeViewModel", "Water updated to: $glasses glasses")
    }

    fun addWaterGlass() {
        val current = sharedPreferences.getInt("current_water_glasses", 0)
        val newCount = current + 1

        sharedPreferences.edit().putInt("current_water_glasses", newCount).apply()

        val goal = _waterData.value?.goal ?: 8
        _waterData.value = WaterData(newCount, goal)

        android.util.Log.d("HomeViewModel", "Water glass added. Total: $newCount")
    }

    fun updateSleep(sleep: Float) {
        sharedPreferences.edit().putFloat("current_sleep", sleep).apply()
        _sleepData.value = SleepData(sleep, _sleepData.value?.goal ?: 8f)
    }

    fun updateHeartRate(heartRate: Int) {
        sharedPreferences.edit().putInt("heart_rate", heartRate).apply()
        _heartRateData.value = heartRate
    }

    fun updateMood(mood: String) {
        sharedPreferences.edit().putString("today_mood", mood).apply()
        _todayMood.value = mood
    }

    fun updateHabitsCompletion(completed: Int, total: Int) {
        with(sharedPreferences.edit()) {
            putInt("completed_habits_today", completed)
            putInt("total_habits", total)
            apply()
        }
        _completedHabitsToday.value = completed
        _totalHabits.value = total
    }

    fun setUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun initializeSampleData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        with(sharedPreferences.edit()) {
            putInt("current_steps", 5234)
            putInt("steps_goal", 10000)
            putInt("current_water_glasses", 5)
            putInt("water_goal_glasses", 8)
            putFloat("current_sleep", 6.5f)
            putFloat("sleep_goal", 8f)
            putInt("heart_rate", 75)
            putInt("completed_habits_today", 1)
            putInt("total_habits", 2)
            putString("today_mood", "Very Happy")
            putString("user_name", "User")
            putInt("calories_goal", 2000)
            putString("last_update_date", today)
            apply()
        }
        loadDashboardData()
    }

    fun debugWaterData() {
        val current = sharedPreferences.getInt("current_water_glasses", -1)
        val goal = sharedPreferences.getInt("water_goal_glasses", -1)
        android.util.Log.d("HomeViewModel", "Water Debug - Current: $current, Goal: $goal")

        val allEntries = sharedPreferences.all
        android.util.Log.d("HomeViewModel", "All SharedPreferences: $allEntries")
    }

    fun updateStepsGoal(goal: Int) {
        sharedPreferences.edit().putInt("steps_goal", goal).apply()
        val current = _stepsData.value?.current ?: 0
        _stepsData.value = StepsData(current, goal)
        android.util.Log.d("HomeViewModel", "Steps goal updated to: $goal")
    }

}
