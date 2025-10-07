package com.example.wellnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class HydrationFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var textWaterProgress: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var tvWaterGoal: TextView
    private lateinit var buttonAddWater: Button
    private lateinit var buttonSetReminder: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        initializeViews(view)
        setupObservers()
        setupClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        textWaterProgress = view.findViewById(R.id.text_water_progress)
        progressWater = view.findViewById(R.id.progress_water)
        tvWaterGoal = view.findViewById(R.id.tv_water_goal)
        buttonAddWater = view.findViewById(R.id.button_add_water)
        buttonSetReminder = view.findViewById(R.id.button_set_reminder)
    }

    private fun setupObservers() {
        viewModel.waterData.observe(viewLifecycleOwner) { water ->
            textWaterProgress.text = "${water.current} / ${water.goal} glasses today"
            tvWaterGoal.text = "Goal: ${water.goal} glasses"
            progressWater.max = water.goal
            progressWater.progress = water.current
        }
    }

    private fun setupClickListeners() {
        buttonAddWater.setOnClickListener {
            android.util.Log.d("HydrationFragment", "Add water clicked")
            viewModel.addWaterGlass()
            val current = viewModel.waterData.value?.current ?: 0
            Toast.makeText(
                requireContext(),
                "Glass added! Total: $current glasses",
                Toast.LENGTH_SHORT
            ).show()
        }

        buttonSetReminder.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Reminder settings coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}
