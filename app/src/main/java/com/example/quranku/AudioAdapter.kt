package com.example.quranku

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.ItemAudioBinding
import java.io.File
import java.io.IOException

class AudioAdapter(private val context: Context, private val audioFiles: List<File>) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var handler: Handler = Handler()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        holder.binding.tvAudioTitle.text = audioFile.name

        // Set duration for audio
        val duration = getAudioDuration(audioFile)
        holder.binding.tvAudioDuration.text = formatDuration(duration)

        // Set up SeekBar
        holder.binding.seekBar.max = duration
        holder.binding.seekBar.progress = 0

        // Set click listener on Play/Stop button
        holder.binding.btnPlay.setOnClickListener {
            if (mediaPlayer == null) {
                startAudio(holder, audioFile)
            } else {
                stopAudio(holder)
            }
        }

        holder.binding.btnDelete.setOnClickListener {
            audioFile.delete()
            Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show()
            // Update the list to remove the deleted file
            notifyDataSetChanged()
        }

        // Update SeekBar while playing
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer?.isPlaying == true) {
                    holder.binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
    }

    override fun getItemCount(): Int {
        return audioFiles.size
    }

    private fun startAudio(holder: AudioViewHolder, file: File) {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(file.absolutePath)
            mediaPlayer?.prepare()

            // Set the completion listener to stop the audio automatically when finished
            mediaPlayer?.setOnCompletionListener {
                stopAudio(holder)
            }

            mediaPlayer?.start()

            // Update UI on Play
            holder.binding.btnPlay.text = "Stop"
            holder.binding.seekBar.max = mediaPlayer?.duration ?: 0

            // Update SeekBar max value
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (mediaPlayer?.isPlaying == true) {
                        holder.binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
                        handler.postDelayed(this, 100)
                    }
                }
            }, 100)

        } catch (e: IOException) {
            Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAudio(holder: AudioViewHolder) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Update UI on Stop
        holder.binding.btnPlay.text = "Play"
        holder.binding.seekBar.progress = 0
    }

    private fun getAudioDuration(file: File): Int {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            return mediaPlayer.duration
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    class AudioViewHolder(val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root)
}


