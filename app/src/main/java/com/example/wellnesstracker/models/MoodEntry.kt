package com.example.wellnesstracker.models

import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val mood: String,
    val emoji: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val date: String
        get() = com.example.wellnesstracker.utils.DateUtils.formatDate(timestamp)

    companion object {
        val MOOD_OPTIONS = mapOf(
            "😢" to "Very Sad",
            "😔" to "Sad",
            "😐" to "Neutral",
            "😊" to "Happy",
            "😄" to "Very Happy"
        )

        fun getMoodValue(emoji: String): Float {
            return when (emoji) {
                "😢" -> 1f
                "😔" -> 2f
                "😐" -> 3f
                "😊" -> 4f
                "😄" -> 5f
                else -> 3f
            }
        }
    }
}