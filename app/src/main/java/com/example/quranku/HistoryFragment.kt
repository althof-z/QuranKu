package com.example.quranku

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quranku.databinding.FragmentHistoryBinding
import java.io.File

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var audioAdapter: AudioAdapter
    private var currentHelper: AudioPlayerHelper? = null
    private var audioFiles: List<File> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAudioFiles()
        updateStatistics()
    }

    private fun setupRecyclerView() {
        audioAdapter = AudioAdapter(
            requireContext(),
            audioFiles,
            onPlayClicked = { file, seekBar, playButton ->
                // Stop and release previous audio if playing
                currentHelper?.release()
                audioAdapter.stopCurrentPlayback()

                currentHelper = AudioPlayerHelper(requireContext(), seekBar, playButton).also {
                    it.togglePlayPause(file.absolutePath)
                }
            },
            onDeleteClicked = { file ->
                if (file.exists()) {
                    // Stop playback if this file is currently playing
                    currentHelper?.release()
                    audioAdapter.stopCurrentPlayback()
                    
                    file.delete()
                    Toast.makeText(requireContext(), "Recording deleted", Toast.LENGTH_SHORT).show()
                    loadAudioFiles()
                    updateStatistics()
                }
            }
        )

        binding.rvRecordings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }
    }

    private fun loadAudioFiles() {
        val audioDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        audioFiles = audioDir?.listFiles { file ->
            file.extension == "mp3" || file.extension == "wav"
        }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()

        // Update adapter with new files
        audioAdapter.updateAudioFiles(audioFiles)
        
        // Show/hide empty state
        if (audioFiles.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvRecordings.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvRecordings.visibility = View.VISIBLE
        }
    }

    private fun updateStatistics() {
        // Update total recordings count
        binding.tvTotalRecordings.text = audioFiles.size.toString()

        // Calculate total duration
        val totalDuration = audioFiles.sumOf { file ->
            getAudioDuration(file).toLong()
        }
        binding.tvTotalDuration.text = formatDuration(totalDuration.toInt())
    }

    private fun getAudioDuration(file: File): Int {
        return try {
            android.media.MediaPlayer().apply {
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

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning to the fragment
        loadAudioFiles()
        updateStatistics()
    }

    override fun onPause() {
        super.onPause()
        // Stop playback when leaving the fragment
        currentHelper?.release()
        audioAdapter.stopCurrentPlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentHelper?.release()
        _binding = null
    }
}

