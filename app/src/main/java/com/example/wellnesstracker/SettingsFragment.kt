package com.example.wellnesstracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class SettingsFragment : Fragment() {

    // ADD THIS: Shared ViewModel to communicate with HomeFragment
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnDailyGoals: LinearLayout
    private lateinit var btnNotifications: LinearLayout
    private lateinit var btnResetOnboarding: LinearLayout
    private lateinit var btnClearData: LinearLayout
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = SharedPreferencesHelper(requireContext())

        initializeViews(view)
        loadUserData()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        btnDailyGoals = view.findViewById(R.id.btn_daily_goals)
        btnNotifications = view.findViewById(R.id.btn_notifications)
        btnResetOnboarding = view.findViewById(R.id.btn_reset_onboarding)
        btnClearData = view.findViewById(R.id.btn_clear_data)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun loadUserData() {
        tvUserName.text = prefsHelper.getUserName()
        tvUserEmail.text = prefsHelper.getUserEmail()
    }

    private fun setupListeners() {
        btnDailyGoals.setOnClickListener {
            showDailyGoalsDialog()
        }

        btnNotifications.setOnClickListener {
            showNotificationSettings()
        }

        btnResetOnboarding.setOnClickListener {
            showResetOnboardingDialog()
        }

        btnClearData.setOnClickListener {
            showClearDataDialog()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showDailyGoalsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null)

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val stepsInput = EditText(requireContext()).apply {
            hint = "Steps Goal"
            setText(prefsHelper.getStepsGoal().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val hydrationInput = EditText(requireContext()).apply {
            hint = "Hydration Goal (glasses)"
            setText(prefsHelper.getHydrationGoal().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(0, 20, 0, 0)
        }

        layout.addView(stepsInput)
        layout.addView(hydrationInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Daily Goals")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val steps = stepsInput.text.toString().toIntOrNull() ?: 10000
                val hydration = hydrationInput.text.toString().toIntOrNull() ?: 8

                // Save to SharedPreferences
                prefsHelper.setStepsGoal(steps)
                prefsHelper.saveHydrationGoal(hydration)

                // CRITICAL FIX: Refresh HomeViewModel to update HomeFragment immediately
                homeViewModel.refreshData()

                Toast.makeText(requireContext(), "✓ Goals updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationSettings() {
        val options = arrayOf(
            "Enable Hydration Reminders",
            "Enable Goal Achievement Notifications",
            "Enable Daily Summary"
        )

        val checkedItems = booleanArrayOf(
            prefsHelper.isHydrationReminderEnabled(),
            true,
            true
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> prefsHelper.setHydrationReminderEnabled(isChecked)
                }
            }
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(requireContext(), "✓ Settings saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetOnboardingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Onboarding")
            .setMessage("This will show the onboarding screens again on next app launch. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                prefsHelper.setOnboardingCompleted(false)
                Toast.makeText(requireContext(), "✓ Onboarding reset!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Clear All Data")
            .setMessage("This will delete all your wellness data including:\n\n• Steps tracking\n• Mood entries\n• Habits\n• Hydration records\n\nThis cannot be undone!")
            .setPositiveButton("Clear") { _, _ ->
                // Clear all data except user credentials
                prefsHelper.resetDailyData()
                prefsHelper.saveMoodEntries(emptyList())
                prefsHelper.saveHabits(emptyList())

                // CRITICAL FIX: Refresh HomeViewModel after clearing data
                homeViewModel.refreshData()

                Toast.makeText(requireContext(), "✓ All data cleared!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        prefsHelper.logout()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    // ADD THIS: Refresh data when fragment resumes to show latest settings
    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}
