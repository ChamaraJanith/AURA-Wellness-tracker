package com.example.wellnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.models.Habit

class HabitsAdapter(
    private val habits: List<Habit>,
    private val onHabitToggle: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val habitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val habitStreak: TextView = itemView.findViewById(R.id.tv_habit_streak)
        private val habitCheckbox: CheckBox = itemView.findViewById(R.id.cb_habit_done)

        fun bind(habit: Habit) {
            habitName.text = habit.name
            habitStreak.text = "${habit.streak} day streak"

            // Check if habit is completed today
            val isCompletedToday = habit.isCompletedToday()
            habitCheckbox.isChecked = isCompletedToday

            habitCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onHabitToggle(habit, isChecked)
            }
        }
    }
}
