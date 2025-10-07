package com.example.wellnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.cardview.widget.CardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    // UI Components
    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStepsCount: TextView
    private lateinit var tvStepsGoal: TextView
    private lateinit var tvStepsPercentage: TextView
    private lateinit var progressSteps: CircularProgressIndicator
    private lateinit var tvCaloriesCount: TextView
    private lateinit var tvWaterCount: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var tvMoodStatus: TextView
    private lateinit var progressCalories: ProgressBar
    private lateinit var progressWater: ProgressBar
    private lateinit var progressHabits: ProgressBar
    private lateinit var tvActivityMinutes: TextView
    private lateinit var tvDistance: TextView
    private lateinit var cardSteps: CardView
    private lateinit var cardCalories: CardView
    private lateinit var cardWater: CardView
    private lateinit var cardHabits: CardView
    private lateinit var cardMood: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupObservers()
        displayCurrentDate()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDashboardData()
        setupCardClickListeners()
    }

    private fun initializeViews(view: View) {
        try {
            tvWelcome = view.findViewById(R.id.tvWelcome)
            tvDate = view.findViewById(R.id.tvDate)
            tvStepsCount = view.findViewById(R.id.tvStepsCount)
            tvStepsGoal = view.findViewById(R.id.tvStepsGoal)
            tvStepsPercentage = view.findViewById(R.id.tvStepsPercentage)
            progressSteps = view.findViewById(R.id.progressSteps)
            tvCaloriesCount = view.findViewById(R.id.tvCaloriesCount)
            tvWaterCount = view.findViewById(R.id.tvWaterCount)
            tvHabitsCount = view.findViewById(R.id.tvHabitsCount)
            tvMoodStatus = view.findViewById(R.id.tvMoodStatus)
            progressCalories = view.findViewById(R.id.progressCalories)
            progressWater = view.findViewById(R.id.progressWater)
            progressHabits = view.findViewById(R.id.progressHabits)
            tvActivityMinutes = view.findViewById(R.id.tvActivityMinutes)
            tvDistance = view.findViewById(R.id.tvDistance)
            cardSteps = view.findViewById(R.id.cardSteps)
            cardCalories = view.findViewById(R.id.cardCalories)
            cardWater = view.findViewById(R.id.cardWater)
            cardHabits = view.findViewById(R.id.cardHabits)
            cardMood = view.findViewById(R.id.cardMood)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error initializing views: ${e.message}")
        }
    }

    private fun setupObservers() {
        // Observe steps data
        viewModel.stepsData.observe(viewLifecycleOwner) { steps ->
            updateStepsUI(steps.current, steps.goal)
        }

        // Observe calories data
        viewModel.caloriesData.observe(viewLifecycleOwner) { calories ->
            updateCaloriesUI(calories.current, calories.goal)
        }

        // Observe water data
        viewModel.waterData.observe(viewLifecycleOwner) { water ->
            updateWaterUI(water.current, water.goal)
        }

        // Observe activity minutes
        viewModel.activityMinutes.observe(viewLifecycleOwner) { minutes ->
            tvActivityMinutes.text = "$minutes min"
        }

        // Observe distance
        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            tvDistance.text = String.format("%.2f km", distance)
        }

        // Observe user name
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            tvWelcome.text = "Hello, $name!"
        }

        // Observe habits completion
        viewModel.completedHabitsToday.observe(viewLifecycleOwner) { completed ->
            val total = viewModel.totalHabits.value ?: 2
            updateHabitsUI(completed, total)
        }

        viewModel.totalHabits.observe(viewLifecycleOwner) { total ->
            val completed = viewModel.completedHabitsToday.value ?: 0
            updateHabitsUI(completed, total)
        }

        // Observe mood
        viewModel.todayMood.observe(viewLifecycleOwner) { mood ->
            updateMoodUI(mood)
        }
    }

    private fun updateStepsUI(current: Int, goal: Int) {
        try {
            tvStepsCount.text = current.toString()
            tvStepsGoal.text = "of ${formatNumber(goal)} steps"

            val progress = if (goal > 0) {
                ((current.toFloat() / goal) * 100).toInt()
            } else {
                0
            }

            progressSteps.progress = progress.coerceIn(0, 100)
            tvStepsPercentage.text = "$progress%"
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating steps UI: ${e.message}")
        }
    }

    private fun updateCaloriesUI(current: Int, goal: Int) {
        try {
            tvCaloriesCount.text = current.toString()

            val progress = if (goal > 0) {
                ((current.toFloat() / goal) * 100).toInt()
            } else {
                0
            }

            progressCalories.progress = progress.coerceIn(0, 100)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating calories UI: ${e.message}")
        }
    }

    private fun updateWaterUI(current: Int, goal: Int) {
        try {
            tvWaterCount.text = "$current/$goal"

            val progress = if (goal > 0) {
                ((current.toFloat() / goal) * 100).toInt()
            } else {
                0
            }

            progressWater.progress = progress.coerceIn(0, 100)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating water UI: ${e.message}")
        }
    }

    private fun updateHabitsUI(completed: Int, total: Int) {
        try {
            tvHabitsCount.text = "$completed/$total"

            val progress = if (total > 0) {
                ((completed.toFloat() / total) * 100).toInt()
            } else {
                0
            }

            progressHabits.progress = progress.coerceIn(0, 100)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating habits UI: ${e.message}")
        }
    }

    private fun updateMoodUI(mood: String) {
        try {
            if (mood.isNotEmpty()) {
                tvMoodStatus.text = getMoodEmoji(mood)
            } else {
                tvMoodStatus.text = "😐"
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating mood UI: ${e.message}")
        }
    }

    private fun getMoodEmoji(mood: String): String {
        return when (mood) {
            "Very Happy" -> "😄"
            "Happy" -> "😊"
            "Neutral" -> "😐"
            "Sad" -> "😔"
            "Very Sad" -> "😢"
            else -> "😐"
        }
    }

    private fun displayCurrentDate() {
        try {
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            tvDate.text = currentDate
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error displaying date: ${e.message}")
        }
    }

    private fun setupCardClickListeners() {
        try {
            cardSteps.setOnClickListener {
                // Navigate using fragment transaction
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, StepsFragment())
                    .addToBackStack(null)
                    .commit()
            }

            cardWater.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HydrationFragment())
                    .addToBackStack(null)
                    .commit()
            }

            cardHabits.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HabitsFragment())
                    .addToBackStack(null)
                    .commit()
            }

            cardMood.setOnClickListener {
                // Use the actual class directly
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MoodFragment())
                    .addToBackStack(null)
                    .commit()
            }

            cardCalories.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Calories tracking",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up click listeners: ${e.message}")
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.refreshData()
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error refreshing data: ${e.message}")
        }
    }
}
