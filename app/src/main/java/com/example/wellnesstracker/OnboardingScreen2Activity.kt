package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.TransitionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingScreen2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen_2)

        try {
            Log.d("Onboarding2", "Finding views")
            val skipButton = findViewById<TextView>(R.id.btn_skip)
            val nextButton = findViewById<FloatingActionButton>(R.id.fab_next)

            skipButton?.setOnClickListener {
                Log.d("Onboarding2", "Skip clicked")
                goToMainActivity()
            }

            nextButton?.setOnClickListener {
                Log.d("Onboarding2", "Next clicked")
                goToOnboardingScreen3()
            }

            Log.d("Onboarding2", "Setup complete")
        } catch (e: Exception) {
            Log.e("Onboarding2", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            goToMainActivity()
        }
    }

    private fun goToOnboardingScreen3() {
        try {
            Log.d("Onboarding2", "Navigating to OnboardingScreen3")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen3Activity::class.java)
        } catch (e: Exception) {
            Log.e("Onboarding2", "Error navigating to OnboardingScreen3: ${e.message}")
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        try {
            Log.d("Onboarding2", "Navigating to MainActivity")
            TransitionUtils.navigateWithFadeTransition(this, MainActivity::class.java)
        } catch (e: Exception) {
            Log.e("Onboarding2", "Error navigating to MainActivity: ${e.message}")
            finish()
        }
    }

    override fun onBackPressed() {
        try {
            Log.d("Onboarding2", "Back pressed, going to OnboardingScreen1")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen1Activity::class.java, false)
        } catch (e: Exception) {
            Log.e("Onboarding2", "Error navigating back: ${e.message}")
            goToMainActivity()
        }
    }
}