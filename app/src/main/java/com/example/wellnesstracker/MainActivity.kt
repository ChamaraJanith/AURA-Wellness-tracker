package com.example.wellnesstracker

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var headerContainer: LinearLayout
    private lateinit var textGreeting: TextView
    private lateinit var textUserName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        headerContainer = findViewById(R.id.header_container)
        textGreeting = findViewById(R.id.text_greeting)
        textUserName = findViewById(R.id.text_user_name)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set greeting
        updateGreeting()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            showHeader(true)
        }

        // Handle bottom navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    showHeader(true)
                    true
                }
                R.id.nav_steps -> {
                    loadFragment(StepsFragment())
                    showHeader(false)
                    true
                }
                R.id.nav_hydration -> {
                    loadFragment(HydrationFragment())
                    showHeader(false)
                    true
                }
                R.id.nav_habits -> {
                    loadFragment(HabitsFragment())
                    showHeader(false)
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodFragment())
                    showHeader(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showHeader(show: Boolean) {
        headerContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }

        textGreeting.text = greeting
    }
}
