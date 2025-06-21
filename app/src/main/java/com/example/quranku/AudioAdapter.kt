package com.example.quranku

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.ItemAudioBinding
import com.google.android.material.button.MaterialButton
import java.io.File

class AudioAdapter(
    private val context: Context,
    private var audioFiles: List<File>,
    private val onPlayClicked: (File, SeekBar, MaterialButton) -> Unit,
    private val onDeleteClicked: (File) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var currentlyPlayingPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        val binding = holder.binding

        // Set file name (remove extension for cleaner display)
        val fileName = audioFile.nameWithoutExtension
        binding.tvAudioTitle.text = if (fileName.isNotEmpty()) fileName else "Recording ${position + 1}"
        
        // Reset seekbar and play button
        binding.seekBar.progress = 0
        updatePlayButton(binding.btnPlay, false)

        // Set audio duration
        val duration = getAudioDuration(audioFile)
        binding.seekBar.max = duration
        binding.tvAudioDuration.text = formatDuration(duration)

        // Set tajwid analysis results (placeholder for now)
        binding.tvMad.text = "Mad: -"
        binding.tvIdgham.text = "Idgham: -"
        binding.tvIkhfa.text = "Ikhfa: -"

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
                    onPlayClicked(audioFile, binding.seekBar, binding.btnPlay)
                }
            }
        }

        // Delete button click handler
        binding.btnDelete.setOnClickListener {
            val currentPosition = holder.getAdapterPosition()
            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(audioFile)
            }
        }

        // Update visual state based on playing status
        if (position == currentlyPlayingPosition) {
            updatePlayButton(binding.btnPlay, true)
        } else {
            updatePlayButton(binding.btnPlay, false)
        }
    }

    override fun getItemCount(): Int = audioFiles.size

    fun updateAudioFiles(newAudioFiles: List<File>) {
        audioFiles = newAudioFiles
        currentlyPlayingPosition = -1 // Reset playing state
        notifyDataSetChanged()
    }

    fun stopCurrentPlayback() {
        currentlyPlayingPosition = -1
        notifyDataSetChanged()
    }

    private fun updatePlayButton(button: MaterialButton, isPlaying: Boolean) {
        if (isPlaying) {
            button.setBackgroundResource(R.drawable.ic_pause)
        } else {
            button.setBackgroundResource(R.drawable.ic_play)
        }
    }

    private fun getAudioDuration(file: File): Int {
        return try {
            MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
            }.duration
        } catch (e: Exception) {
            0
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    class AudioViewHolder(val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root)
}



