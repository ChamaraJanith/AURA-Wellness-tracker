package com.example.wellnesstracker.models

data class StepHistoryItem(
    val date: String,
    val steps: Int,
    val timestamp: Long = System.currentTimeMillis()
)
