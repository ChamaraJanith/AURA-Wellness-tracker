package com.example.wellnesstracker.models

data class StepsData(
    val current: Int,
    val goal: Int
)

data class CaloriesData(
    val current: Int,
    val goal: Int
)

data class WaterData(
    val current: Int, // glasses consumed
    val goal: Int     // goal in glasses (8 glasses typically)
)

data class SleepData(
    val current: Float,
    val goal: Float
)

data class HabitData(
    val habitName: String,
    val isCompleted: Boolean,
    val dayStreak: Int
)

data class MoodData(
    val moodLevel: String, // "Very Happy", "Happy", "Neutral", "Sad", "Very Sad"
    val timestamp: Long
)
