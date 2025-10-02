// Habit.kt
package com.example.wellnesstracker.models

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: List<Long> = emptyList()
) {
    val streak: Int
        get() = calculateStreak()

    fun markAsCompleted(): Habit {
        val today = getTodayDate()
        val newCompletedDates = if (completedDates.contains(today)) {
            completedDates
        } else {
            (completedDates + today).sorted()
        }
        return copy(completedDates = newCompletedDates)
    }

    fun markAsIncomplete(): Habit {
        val today = getTodayDate()
        val newCompletedDates = completedDates.filter { it != today }
        return copy(completedDates = newCompletedDates)
    }

    fun isCompletedToday(): Boolean {
        val today = getTodayDate()
        return completedDates.contains(today)
    }

    private fun calculateStreak(): Int {
        if (completedDates.isEmpty()) return 0

        val sortedDates = completedDates.sortedDescending()
        var streak = 0
        var currentDate = getTodayDate()

        for (date in sortedDates) {
            if (date == currentDate) {
                streak++
                currentDate = getPreviousDay(currentDate)
            } else if (date < currentDate) {
                break
            }
        }

        return streak
    }

    private fun getTodayDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getPreviousDay(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}