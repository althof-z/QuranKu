package com.example.quranku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var audioAdapter: AudioAdapter
    private var currentHelper: AudioPlayerHelper? = null
    private lateinit var audioRepository: AudioRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioRepository = AudioRepository(requireContext())
        setupRecyclerView()
        observeRecordings()
        updateStatistics()
    }

    private fun setupRecyclerView() {
        audioAdapter = AudioAdapter(
            requireContext(),
            emptyList(),
            onPlayClicked = { recording, seekBar, playButton ->
                // Stop and release previous audio if playing
                currentHelper?.release()
                audioAdapter.stopCurrentPlayback()

                currentHelper = AudioPlayerHelper(requireContext(), seekBar, playButton).also {
                    it.togglePlayPause(recording.filePath)
                }
            },
            onDeleteClicked = { recording ->
                lifecycleScope.launch {
                    try {
                        audioRepository.deleteRecording(recording)
                        Toast.makeText(requireContext(), "Recording deleted", Toast.LENGTH_SHORT).show()
                        updateStatistics()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to delete recording", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvRecordings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }
    }

    private fun observeRecordings() {
        lifecycleScope.launch {
            audioRepository.getAllRecordings().collectLatest { recordings ->
                audioAdapter.updateRecordings(recordings)
                
                // Show/hide empty state
                if (recordings.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvRecordings.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvRecordings.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateStatistics() {
        lifecycleScope.launch {
            try {
                val recordingsCount = audioRepository.getRecordingsCount()
                val totalDuration = audioRepository.getTotalDuration()
                
                binding.tvTotalRecordings.text = recordingsCount.toString()
                binding.tvTotalDuration.text = formatDuration(totalDuration.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        // Statistics will be updated automatically through Flow
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

