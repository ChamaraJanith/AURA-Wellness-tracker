package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.SharedPreferencesHelper
import com.example.wellnesstracker.utils.TransitionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingScreen1Activity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen_1)

        prefsHelper = SharedPreferencesHelper(this)

        try {
            Log.d("Onboarding1", "Finding views")
            val skipButton = findViewById<TextView>(R.id.btn_skip)
            val nextButton = findViewById<FloatingActionButton>(R.id.fab_next)

            skipButton?.setOnClickListener {
                Log.d("Onboarding1", "Skip clicked")
                skipToLogin()
            }

            nextButton?.setOnClickListener {
                Log.d("Onboarding1", "Next clicked")
                goToOnboardingScreen2()
            }

            Log.d("Onboarding1", "Setup complete")
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            skipToLogin()
        }
    }

    private fun goToOnboardingScreen2() {
        try {
            Log.d("Onboarding1", "Navigating to OnboardingScreen2")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen2Activity::class.java)
            finish()
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error navigating to OnboardingScreen2: ${e.message}")
            skipToLogin()
        }
    }

    private fun skipToLogin() {
        try {
            Log.d("Onboarding1", "Skipping to Login")

            // Mark onboarding as completed
            prefsHelper.setOnboardingCompleted(true)

            // Navigate to Login
            TransitionUtils.navigateWithFadeTransition(this, LoginActivity::class.java)
            finish()
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error navigating to Login: ${e.message}")

            // Fallback navigation
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Go back to splash or exit
        finish()
    }
}
