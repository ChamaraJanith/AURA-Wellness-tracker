package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.SharedPreferencesHelper
import com.example.wellnesstracker.utils.TransitionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingScreen3Activity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen_3)

        prefsHelper = SharedPreferencesHelper(this)

        try {
            Log.d("Onboarding3", "Finding views")
            val skipButton = findViewById<TextView>(R.id.btn_skip)
            val getStartedButton = findViewById<FloatingActionButton>(R.id.fab_next)

            skipButton?.setOnClickListener {
                Log.d("Onboarding3", "Skip clicked")
                completeOnboarding()
            }

            getStartedButton?.setOnClickListener {
                Log.d("Onboarding3", "Get Started clicked")
                completeOnboarding()
            }

            Log.d("Onboarding3", "Setup complete")
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        try {
            Log.d("Onboarding3", "Completing onboarding and navigating to Login")

            // Mark onboarding as completed
            prefsHelper.setOnboardingCompleted(true)

            // Navigate to Login screen
            TransitionUtils.navigateWithFadeTransition(this, LoginActivity::class.java)
            finish()
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error completing onboarding: ${e.message}")

            // Fallback navigation
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        try {
            Log.d("Onboarding3", "Back pressed, going to OnboardingScreen2")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen2Activity::class.java, false)
            finish()
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error navigating back: ${e.message}")
            super.onBackPressed()
        }
    }
}
