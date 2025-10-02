package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.TransitionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingScreen1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen_1)

        try {
            Log.d("Onboarding1", "Finding views")
            val skipButton = findViewById<TextView>(R.id.btn_skip)
            val nextButton = findViewById<FloatingActionButton>(R.id.fab_next)

            skipButton?.setOnClickListener {
                Log.d("Onboarding1", "Skip clicked")
                goToMainActivity()
            }

            nextButton?.setOnClickListener {
                Log.d("Onboarding1", "Next clicked")
                goToOnboardingScreen2()
            }

            Log.d("Onboarding1", "Setup complete")
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            goToMainActivity()
        }
    }

    private fun goToOnboardingScreen2() {
        try {
            Log.d("Onboarding1", "Navigating to OnboardingScreen2")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen2Activity::class.java)
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error navigating to OnboardingScreen2: ${e.message}")
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        try {
            Log.d("Onboarding1", "Navigating to MainActivity")
            TransitionUtils.navigateWithFadeTransition(this, MainActivity::class.java)
        } catch (e: Exception) {
            Log.e("Onboarding1", "Error navigating to MainActivity: ${e.message}")
            finish()
        }
    }

    override fun onBackPressed() {
        // If on first screen, go to main activity instead of closing app
        goToMainActivity()
    }
}