package com.example.quranku.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quranku.data.repository.AudioRepository
import com.example.quranku.databinding.FragmentHistoryBinding
import com.example.quranku.util.FirstTimeSetupHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding: FragmentHistoryBinding by lazy { _binding!! }

    private lateinit var audioAdapter: AudioAdapter
    private var currentHelper: AudioPlayerHelper? = null
    private var currentPlayingPath: String? = null
    private lateinit var audioRepository: AudioRepository
    private lateinit var setupHelper: FirstTimeSetupHelper

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
        setupHelper = FirstTimeSetupHelper(requireContext())
        setupRecyclerView()
        observeRecordings()
        updateStatistics()
        
        // Check if demo files need to be analyzed
        if (!setupHelper.areDemoFilesAnalyzed() && setupHelper.checkDemoFilesExist()) {
            setupHelper.manuallyAnalyzeDemoFiles()
        }
        
        // Setup FAB for demo files (only show if demo files exist but not analyzed)
        setupDemoFilesFab()
    }

    private fun setupRecyclerView() {
        audioAdapter = AudioAdapter(
            requireContext(),
            emptyList(),
            onPlayClicked = { recording, seekBar, playButton ->
                val clickedPath = recording.filePath
                if (currentHelper != null && currentPlayingPath == clickedPath) {
                    // Toggle play/pause for the same item
                    currentHelper?.togglePlayPause(clickedPath)
                } else {
                    // Different item: release previous, start new
                    currentHelper?.release()
                    audioAdapter.stopCurrentPlayback()
                    currentHelper = AudioPlayerHelper(requireContext(), seekBar, playButton).also {
                        it.togglePlayPause(clickedPath)
                    }
                    currentPlayingPath = clickedPath
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
                
                // Log demo files status for debugging
                android.util.Log.d("HistoryFragment", "Demo files status: ${setupHelper.getDemoFilesStatus()}")
                android.util.Log.d("HistoryFragment", "Demo files exist: ${setupHelper.checkDemoFilesExist()}")
                android.util.Log.d("HistoryFragment", "Raw resources exist: ${setupHelper.checkRawResourcesExist()}")
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
        currentPlayingPath = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentHelper?.release()
        currentPlayingPath = null
        _binding = null
    }
    
    private fun setupDemoFilesFab() {
        // Show FAB if demo files don't exist or haven't been analyzed
        val demoFilesExist = setupHelper.checkDemoFilesExist()
        val areAnalyzed = setupHelper.areDemoFilesAnalyzed()
        
        android.util.Log.d("HistoryFragment", "Demo files exist: $demoFilesExist, analyzed: $areAnalyzed")
        
        if (!demoFilesExist || !areAnalyzed) {
            binding.fabDemoFiles.visibility = View.VISIBLE
            
            binding.fabDemoFiles.setOnClickListener {
                if (!demoFilesExist) {
                    // Reset and perform first-time setup
                    setupHelper.resetFirstTimeSetup()
                    setupHelper.performFirstTimeSetup()
                    Toast.makeText(requireContext(), "Setting up demo files...", Toast.LENGTH_SHORT).show()
                } else {
                    // Just analyze existing files
                    setupHelper.manuallyAnalyzeDemoFiles()
                    Toast.makeText(requireContext(), "Analyzing demo files...", Toast.LENGTH_SHORT).show()
                }
                binding.fabDemoFiles.visibility = View.GONE
            }
        } else {
            binding.fabDemoFiles.visibility = View.GONE
        }
    }
}

