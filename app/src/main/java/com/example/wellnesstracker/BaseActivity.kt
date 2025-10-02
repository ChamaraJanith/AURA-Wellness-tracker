// BaseActivity.kt
package com.example.wellnesstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.Settings

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Initialize activity-specific components
            initializeActivity()
        } catch (e: Exception) {
            handleError(e, "Error initializing activity")
        }
    }

    abstract fun initializeActivity()

    protected fun handleError(exception: Exception, message: String = "An error occurred") {
        // Log the error
        android.util.Log.e("BaseActivity", message, exception)

        // Show a toast message
        Toast.makeText(this, "$message: ${exception.message}", Toast.LENGTH_SHORT).show()

        // You can add additional error handling here
    }

    protected fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            handleError(e, "Error opening app settings")
        }
    }

    protected fun safeStartActivity(intent: Intent, errorMessage: String = "Error starting activity") {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            handleError(e, errorMessage)
        }
    }
}