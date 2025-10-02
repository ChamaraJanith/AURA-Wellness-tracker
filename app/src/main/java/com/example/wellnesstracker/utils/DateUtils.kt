package com.example.wellnesstracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DATE_TIME_FORMAT = "MMM dd, yyyy 'at' hh:mm a"

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(Date(timestamp))
    }

    fun getLastSevenDays(): List<String> {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val days = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        // Start from today and go back 6 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            days.add(dateFormat.format(calendar.time))
        }

        return days
    }
}