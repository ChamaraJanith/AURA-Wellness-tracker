package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.TransitionUtils

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Log.d("SplashActivity", "Splash screen started")

        // Navigate after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToOnboardingScreen1()
        }, 3000)
    }

    private fun navigateToOnboardingScreen1() {
        try {
            Log.d("SplashActivity", "Attempting to navigate to OnboardingScreen1Activity")
            TransitionUtils.navigateWithFadeTransition(this, OnboardingScreen1Activity::class.java)
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error navigating to OnboardingScreen1Activity: ${e.message}")
            // Fallback to MainActivity
            try {
                Log.d("SplashActivity", "Falling back to MainActivity")
                TransitionUtils.navigateWithFadeTransition(this, MainActivity::class.java)
            } catch (e2: Exception) {
                Log.e("SplashActivity", "Error navigating to MainActivity: ${e2.message}")
                finish()
            }
        }
    }
}