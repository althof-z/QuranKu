package com.example.quranku

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.ItemAudioBinding
import java.io.File
import java.io.IOException

class AudioAdapter(
    private val context: Context,
    private val audioFiles: List<File>,
    private val onPlayClicked: (File, SeekBar, ImageButton) -> Unit,
    private val onDeleteClicked: (File) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        val binding = holder.binding

        binding.tvAudioTitle.text = audioFile.name
        binding.seekBar.progress = 0
        binding.btnPlay.setBackgroundResource(R.drawable.ic_play)

        // Set audio duration
        val duration = getAudioDuration(audioFile)
        binding.seekBar.max = duration
        binding.tvAudioDuration.text = formatDuration(duration)

        // Play button handled by AudioPlayerHelper via callback
        binding.btnPlay.setOnClickListener {
            onPlayClicked(audioFile, binding.seekBar, binding.btnPlay)
        }

        binding.btnDelete.setOnClickListener {
            onDeleteClicked(audioFile)
        }
    }

    override fun getItemCount(): Int = audioFiles.size

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



