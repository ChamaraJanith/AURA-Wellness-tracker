// WellnessApplication.kt
package com.example.wellnesstracker

import android.app.Application
import android.content.Context
import android.util.Log
import kotlin.system.exitProcess

class WellnessApplication : Application() {

    companion object {
        private const val TAG = "WellnessApplication"
        lateinit var instance: WellnessApplication
            private set

        fun getContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            handleUncaughtException(throwable)
        }
    }

    private fun handleUncaughtException(throwable: Throwable) {
        try {
            // Log the exception
            Log.e(TAG, "Application crashed", throwable)

            // You can add additional error handling here, like:
            // 1. Sending crash reports to a server
            // 2. Showing a crash dialog
            // 3. Saving app state before crash

            // Exit the app
            exitProcess(1)
        } catch (e: Exception) {
            Log.e(TAG, "Error while handling uncaught exception", e)
            exitProcess(1)
        }
    }
}