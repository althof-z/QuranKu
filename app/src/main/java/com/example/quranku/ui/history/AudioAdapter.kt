package com.example.quranku.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.R
import com.example.quranku.data.entity.AudioRecording
import com.example.quranku.databinding.ItemAudioBinding
import com.google.android.material.button.MaterialButton

class AudioAdapter(
    private val context: Context,
    private var recordings: List<AudioRecording>,
    private val onPlayClicked: (AudioRecording, SeekBar, MaterialButton) -> Unit,
    private val onDeleteClicked: (AudioRecording) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var currentlyPlayingPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val recording = recordings[position]
        val binding = holder.binding

        // Set file name
        binding.tvAudioTitle.text = recording.fileName
        
        // Reset seekbar and play button
        binding.seekBar.progress = 0
        updatePlayButton(binding.btnPlay, false)

        // Set audio duration from database
        binding.seekBar.max = recording.duration.toInt()
        binding.tvAudioDuration.text = formatDuration(recording.duration.toInt())

        // Set tajwid analysis results from database with loading state
        if (recording.isAnalyzing) {
            // Show loading state
            binding.tvMad.text = "Mad: 🔄"
            binding.tvIdgham.text = "Idgham: 🔄"
            binding.tvIkhfa.text = "Ikhfa: 🔄 "
        } else {
            // Show results or error state
            binding.tvMad.text = if (recording.mad != null) {
                if (recording.mad) "Mad: - " else "Mad: -"
            } else {
                "Mad: ⚠ Error"
            }
            
            binding.tvIdgham.text = if (recording.idgham != null) {
                if (recording.idgham) "Idgham: -" else "Idgham: -"
            } else {
                "Idgham: ⚠ Error"
            }
            
            binding.tvIkhfa.text = if (recording.ikhfa != null) {
                if (recording.ikhfa) "Ikhfa: -" else "Ikhfa: -"
            } else {
                "Ikhfa: ⚠ Error"
            }
        }

        // Play button click handler
        binding.btnPlay.setOnClickListener {
            val currentPosition = holder.getAdapterPosition()
            if (currentPosition != RecyclerView.NO_POSITION) {
                if (currentlyPlayingPosition == currentPosition) {
                    // Stop current playback
                    currentlyPlayingPosition = -1
                    updatePlayButton(binding.btnPlay, false)
                } else {
                    // Start new playback
                    currentlyPlayingPosition = currentPosition
                    updatePlayButton(binding.btnPlay, true)
                    onPlayClicked(recording, binding.seekBar, binding.btnPlay)
                }
            }
        }

        // Delete button click handler
        binding.btnDelete.setOnClickListener {
            val currentPosition = holder.getAdapterPosition()
            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(recording)
            }
        }

        // Update visual state based on playing status
        if (position == currentlyPlayingPosition) {
            updatePlayButton(binding.btnPlay, true)
        } else {
            updatePlayButton(binding.btnPlay, false)
        }
    }

    override fun getItemCount(): Int = recordings.size

    fun updateRecordings(newRecordings: List<AudioRecording>) {
        recordings = newRecordings
        currentlyPlayingPosition = -1 // Reset playing state
        notifyDataSetChanged()
    }

    fun stopCurrentPlayback() {
        currentlyPlayingPosition = -1
        notifyDataSetChanged()
    }

    private fun updatePlayButton(button: MaterialButton, isPlaying: Boolean) {
        if (isPlaying) {
            button.setBackgroundResource(R.drawable.placeholder_box)
        } else {
            button.setBackgroundResource(R.drawable.placeholder_box)
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    class AudioViewHolder(val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root)
}



