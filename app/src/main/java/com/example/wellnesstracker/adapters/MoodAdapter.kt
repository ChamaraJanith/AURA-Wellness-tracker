package com.example.wellnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.models.MoodEntry
import com.example.wellnesstracker.utils.DateUtils

class MoodAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textEmoji: TextView = itemView.findViewById(R.id.text_emoji)
        private val textMood: TextView = itemView.findViewById(R.id.text_mood)
        private val textNote: TextView = itemView.findViewById(R.id.text_note)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val buttonDelete: TextView = itemView.findViewById(R.id.button_delete)

        fun bind(moodEntry: MoodEntry) {
            textEmoji.text = moodEntry.emoji
            textMood.text = moodEntry.mood
            textNote.text = moodEntry.note.ifEmpty { "No note" }
            textNote.visibility = if (moodEntry.note.isEmpty()) View.GONE else View.VISIBLE
            textDate.text = DateUtils.formatDateTime(moodEntry.timestamp)

            buttonDelete.setOnClickListener {
                onDeleteClick(moodEntry)
            }
        }
    }
}