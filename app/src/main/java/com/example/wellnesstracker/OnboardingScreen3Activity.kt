package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstracker.utils.TransitionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingScreen3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_screen_3)

        try {
            Log.d("Onboarding3", "Finding views")
            val skipButton = findViewById<TextView>(R.id.btn_skip)
            val getStartedButton = findViewById<FloatingActionButton>(R.id.fab_next)

            skipButton?.setOnClickListener {
                Log.d("Onboarding3", "Skip clicked")
                goToMainActivity()
            }

            getStartedButton?.setOnClickListener {
                Log.d("Onboarding3", "Get Started clicked")
                goToMainActivity()
            }

            Log.d("Onboarding3", "Setup complete")
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        try {
            Log.d("Onboarding3", "Navigating to MainActivity")
            TransitionUtils.navigateWithFadeTransition(this, MainActivity::class.java)
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error navigating to MainActivity: ${e.message}")
            finish()
        }
    }

    override fun onBackPressed() {
        try {
            Log.d("Onboarding3", "Back pressed, going to OnboardingScreen2")
            TransitionUtils.navigateWithSlideTransition(this, OnboardingScreen2Activity::class.java, false)
        } catch (e: Exception) {
            Log.e("Onboarding3", "Error navigating back: ${e.message}")
            goToMainActivity()
        }
    }
}