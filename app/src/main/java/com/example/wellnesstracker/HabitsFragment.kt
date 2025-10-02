// HabitsFragment.kt
package com.example.wellnesstracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.wellnesstracker.adapters.HabitsAdapter
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.utils.SharedPreferencesHelper

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitsAdapter
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var emptyView: View
    private lateinit var progressCard: View
    private lateinit var textProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonQuickAdd: Button
    private lateinit var buttonViewAnalytics: Button

    private val habits = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = SharedPreferencesHelper(requireContext())

        initializeViews(view)
        setupRecyclerView()
        loadHabits()
        updateProgress()

        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }

        buttonQuickAdd.setOnClickListener {
            showQuickAddDialog()
        }

        buttonViewAnalytics.setOnClickListener {
            showAnalyticsDialog()
        }
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_habits)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)
        emptyView = view.findViewById(R.id.empty_view)
        progressCard = view.findViewById(R.id.progress_card)
        textProgress = view.findViewById(R.id.text_progress)
        progressBar = view.findViewById(R.id.progress_bar)
        buttonQuickAdd = view.findViewById(R.id.button_quick_add)
        buttonViewAnalytics = view.findViewById(R.id.button_view_analytics)
    }

    private fun setupRecyclerView() {
        adapter = HabitsAdapter(habits) { habit, isChecked ->
            toggleHabit(habit, isChecked)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadHabits() {
        habits.clear()
        habits.addAll(prefsHelper.getHabits())
        adapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (habits.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            progressCard.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            progressCard.visibility = View.VISIBLE
        }
    }

    private fun updateProgress() {
        if (habits.isEmpty()) {
            textProgress.text = "0 of 0 habits completed today"
            progressBar.progress = 0
            return
        }

        val completedCount = habits.count { it.isCompletedToday() }
        val totalCount = habits.size
        val progressPercentage = if (totalCount > 0) {
            (completedCount * 100) / totalCount
        } else {
            0
        }

        textProgress.text = "$completedCount of $totalCount habits completed today"
        progressBar.progress = progressPercentage

        // Check if all habits are completed
        if (completedCount == totalCount && totalCount > 0) {
            android.widget.Toast.makeText(requireContext(), "Amazing! You've completed all your habits for today!", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleHabit(habit: Habit, isChecked: Boolean) {
        val updatedHabit = if (isChecked) {
            habit.markAsCompleted()
        } else {
            habit.markAsIncomplete()
        }

        val index = habits.indexOf(habit)
        if (index != -1) {
            habits[index] = updatedHabit
            adapter.notifyItemChanged(index)

            // Save to preferences
            prefsHelper.saveHabits(habits)
            updateProgress()

            // Show feedback
            val message = if (isChecked) {
                "Great! Keep up the streak! 🔥"
            } else {
                "Habit unmarked for today"
            }
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)

        val editHabitName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_habit_name)
        val editHabitDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_habit_description)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editHabitName.text.toString().trim()
                val description = editHabitDescription.text.toString().trim()

                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        name = name,
                        description = description
                    )

                    habits.add(0, newHabit)
                    adapter.notifyItemInserted(0)
                    recyclerView.scrollToPosition(0)

                    // Save to preferences
                    prefsHelper.saveHabits(habits)

                    updateEmptyView()
                    updateProgress()
                    android.widget.Toast.makeText(requireContext(), "Habit added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please enter a habit name", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showQuickAddDialog() {
        // Simple dialog to quickly mark habits as done
        val incompleteHabits = habits.filter { !it.isCompletedToday() }
        if (incompleteHabits.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "All habits completed for today!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val habitNames = incompleteHabits.map { it.name }.toTypedArray()
        var selectedHabit: Habit? = null

        AlertDialog.Builder(requireContext())
            .setTitle("Quick Add")
            .setSingleChoiceItems(habitNames, -1) { _, which ->
                selectedHabit = incompleteHabits[which]
            }
            .setPositiveButton("Mark Done") { _, _ ->
                selectedHabit?.let {
                    toggleHabit(it, true)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAnalyticsDialog() {
        // Show simple analytics about habit completion
        val totalHabits = habits.size
        val completedToday = habits.count { it.isCompletedToday() }
        val completionRate = if (totalHabits > 0) {
            (completedToday * 100 / totalHabits)
        } else {
            0
        }

        val message = """
            Habit Analytics:
            
            Total Habits: $totalHabits
            Completed Today: $completedToday
            Completion Rate: $completionRate%
            
            Longest Streak: ${habits.maxOfOrNull { it.streak } ?: 0} days
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Habit Analytics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}