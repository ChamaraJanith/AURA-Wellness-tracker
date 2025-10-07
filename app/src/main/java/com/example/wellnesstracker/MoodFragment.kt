package com.example.wellnesstracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.wellnesstracker.adapters.MoodAdapter
import com.example.wellnesstracker.models.MoodEntry
import com.example.wellnesstracker.utils.SharedPreferencesHelper
import com.example.wellnesstracker.utils.DateUtils

class MoodFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodAdapter
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var moodChart: LineChart
    private lateinit var emptyView: LinearLayout
    private lateinit var buttonShare: Button

    private val moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = SharedPreferencesHelper(requireContext())

        initializeViews(view)
        setupRecyclerView()
        setupChart()
        loadMoodEntries()

        fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }

        buttonShare.setOnClickListener {
            shareMoodSummary()
        }
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_moods)
        fabAddMood = view.findViewById(R.id.fab_add_mood)
        moodChart = view.findViewById(R.id.mood_chart)
        emptyView = view.findViewById(R.id.empty_view)
        buttonShare = view.findViewById(R.id.button_share)
    }

    private fun setupRecyclerView() {
        adapter = MoodAdapter(moodEntries) { moodEntry ->
            showDeleteMoodConfirmation(moodEntry)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupChart() {
        moodChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textSize = 12f
                textColor = resources.getColor(R.color.colorPrimaryDark, null)
            }

            axisLeft.apply {
                axisMinimum = 1f
                axisMaximum = 5f
                granularity = 1f
                setDrawGridLines(true)
                textSize = 12f
                textColor = resources.getColor(R.color.colorPrimaryDark, null)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            1 -> "Very Sad"
                            2 -> "Sad"
                            3 -> "Neutral"
                            4 -> "Happy"
                            5 -> "Very Happy"
                            else -> ""
                        }
                    }
                }
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = resources.getColor(R.color.colorPrimaryDark, null)

            setBackgroundColor(resources.getColor(android.R.color.white, null))
            setPadding(10, 10, 10, 10)
        }
    }

    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(prefsHelper.getMoodEntries().sortedByDescending { it.timestamp })
        adapter.notifyDataSetChanged()
        updateChart()
        updateEmptyView()
        updateTodayMood()
    }

    private fun updateTodayMood() {
        // Get today's most recent mood
        val today = DateUtils.formatDate(System.currentTimeMillis())
        val todayMoods = moodEntries.filter { it.date == today }

        if (todayMoods.isNotEmpty()) {
            val latestMood = todayMoods.first() // Already sorted by timestamp descending
            viewModel.updateMood(latestMood.mood)
        } else {
            viewModel.updateMood("")
        }
    }

    private fun updateChart() {
        val lastSevenDays = DateUtils.getLastSevenDays()
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        lastSevenDays.forEachIndexed { index, date ->
            val dayMoods = moodEntries.filter { it.date == date }
            val avgMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { MoodEntry.getMoodValue(it.emoji) }.average().toFloat()
            } else {
                Float.NaN
            }

            if (!avgMood.isNaN()) {
                entries.add(Entry(index.toFloat(), avgMood))
            }
            labels.add(date.substring(5))
        }

        moodChart.clear()

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Mood Trend").apply {
                color = resources.getColor(R.color.colorPrimary, null)
                setCircleColor(resources.getColor(R.color.colorPrimary, null))
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 9f
                setDrawValues(true)
            }

            val lineData = LineData(dataSet)
            moodChart.data = lineData
            moodChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            moodChart.notifyDataSetChanged()
            moodChart.invalidate()
        } else {
            moodChart.clear()
            moodChart.invalidate()
        }
    }

    private fun updateEmptyView() {
        if (moodEntries.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            moodChart.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            moodChart.visibility = View.VISIBLE
        }
    }

    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_mood, null)

        val spinnerMood = dialogView.findViewById<Spinner>(R.id.spinner_mood)
        val editNote = dialogView.findViewById<EditText>(R.id.edit_mood_note)

        val moodOptions = MoodEntry.MOOD_OPTIONS.keys.toList()
        val moodAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            moodOptions
        )
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMood.adapter = moodAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selectedEmoji = moodOptions[spinnerMood.selectedItemPosition]
                val moodName = MoodEntry.MOOD_OPTIONS[selectedEmoji] ?: "Unknown"
                val note = editNote.text.toString().trim()

                val moodEntry = MoodEntry(
                    mood = moodName,
                    emoji = selectedEmoji,
                    note = note
                )

                moodEntries.add(0, moodEntry)
                val allMoods = prefsHelper.getMoodEntries().toMutableList()
                allMoods.add(moodEntry)
                prefsHelper.saveMoodEntries(allMoods)

                adapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)
                updateChart()
                updateEmptyView()
                updateTodayMood()

                // Check if this is the first mood entry of the day
                val todayMoods = allMoods.filter {
                    it.date == DateUtils.formatDate(System.currentTimeMillis())
                }
                if (todayMoods.size == 1) {
                    Toast.makeText(
                        requireContext(),
                        "Great start! You've logged your first mood of the day.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteMoodConfirmation(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                val index = moodEntries.indexOf(moodEntry)
                if (index != -1) {
                    moodEntries.removeAt(index)
                    val allMoods = prefsHelper.getMoodEntries().toMutableList()
                    allMoods.removeAll { it.id == moodEntry.id }
                    prefsHelper.saveMoodEntries(allMoods)

                    adapter.notifyItemRemoved(index)
                    updateChart()
                    updateEmptyView()
                    updateTodayMood()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareMoodSummary() {
        if (moodEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No mood data to share", Toast.LENGTH_SHORT).show()
            return
        }

        val recentMoods = moodEntries.take(7)
        val summary = buildString {
            appendLine("My Mood Summary 😊")
            appendLine("Recent entries:")
            appendLine()

            recentMoods.forEach { mood ->
                appendLine("${mood.emoji} ${mood.mood} - ${DateUtils.formatDateTime(mood.timestamp)}")
                if (mood.note.isNotEmpty()) {
                    appendLine("   Note: ${mood.note}")
                }
                appendLine()
            }

            appendLine("Shared from AURA Wellness Tracker")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Summary")
        }

        startActivity(Intent.createChooser(shareIntent, "Share mood summary"))
    }

    override fun onResume() {
        super.onResume()
        loadMoodEntries()
    }
}
