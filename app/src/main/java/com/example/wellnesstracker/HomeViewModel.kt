package com.example.wellnesstracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wellnesstracker.utils.SharedPreferencesHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsHelper = SharedPreferencesHelper(application)

    // LiveData properties
    private val _stepsData = MutableLiveData<StepsData>()
    val stepsData: LiveData<StepsData> = _stepsData

    private val _caloriesData = MutableLiveData<CaloriesData>()
    val caloriesData: LiveData<CaloriesData> = _caloriesData

    private val _waterData = MutableLiveData<WaterData>()
    val waterData: LiveData<WaterData> = _waterData

    private val _activityMinutes = MutableLiveData<Int>()
    val activityMinutes: LiveData<Int> = _activityMinutes

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

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

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun loadDashboardData() {
        try {
            val todayString = getTodayDateString()
            val todayTimestamp = getTodayTimestamp()

            _userName.value = prefsHelper.getUserName()

            val currentSteps = prefsHelper.getSteps()
            val stepsGoal = prefsHelper.getStepsGoal()
            _stepsData.value = StepsData(currentSteps, stepsGoal)

            val distanceKm = (currentSteps * 0.762) / 1000.0
            _distance.value = distanceKm

            val minutes = currentSteps / 100
            _activityMinutes.value = minutes

            val currentWater = prefsHelper.getHydrationCount()
            val waterGoal = prefsHelper.getHydrationGoal()
            _waterData.value = WaterData(currentWater, waterGoal)

            val currentCalories = (currentSteps * 0.04).toInt()
            val caloriesGoal = 2000
            _caloriesData.value = CaloriesData(currentCalories, caloriesGoal)

            val habits = prefsHelper.getHabits()
            val completedToday = habits.count { habit ->
                habit.completedDates.contains(todayTimestamp)
            }
            _completedHabitsToday.value = completedToday
            _totalHabits.value = habits.size

            val moodEntries = prefsHelper.getMoodEntries()
            val todayMoodEntry = moodEntries.firstOrNull { it.date == todayString }
            _todayMood.value = todayMoodEntry?.mood ?: ""

        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error loading dashboard data: ${e.message}")
            _stepsData.value = StepsData(0, 10000)
            _caloriesData.value = CaloriesData(0, 2000)
            _waterData.value = WaterData(0, 8)
            _completedHabitsToday.value = 0
            _totalHabits.value = 0
            _todayMood.value = ""
        }
    }

    fun updateHabitsCompletion(completed: Int, total: Int) {
        _completedHabitsToday.value = completed
        _totalHabits.value = total
    }

    fun updateMood(mood: String) {
        _todayMood.value = mood
    }

    fun refreshData() {
        loadDashboardData()
    }

    data class StepsData(val current: Int, val goal: Int)
    data class CaloriesData(val current: Int, val goal: Int)
    data class WaterData(val current: Int, val goal: Int)
}
