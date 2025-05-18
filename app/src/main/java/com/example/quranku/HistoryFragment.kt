package com.example.quranku

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.FragmentHistoryBinding
import java.io.File

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var audioAdapter: AudioAdapter
    private lateinit var audioPlayerHelper: AudioPlayerHelper
    private var currentHelper: AudioPlayerHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        val audioDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val audioFiles = audioDir?.listFiles { file ->
            file.extension == "mp3" || file.extension == "wav"
        }?.toList() ?: emptyList()

        audioAdapter = AudioAdapter(
            requireContext(),
            audioFiles,
            onPlayClicked = { file, seekBar, playButton ->
                // Stop and release previous audio if playing
                currentHelper?.release()

                currentHelper = AudioPlayerHelper(requireContext(), seekBar, playButton).also {
                    it.togglePlayPause(file.absolutePath)
                }
            },
            onDeleteClicked = { file ->
                if (file.exists()) {
                    file.delete()
                    Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show()
                    refreshAudioList()
                }
            }
        )

        binding.rvRecordings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }

        return binding.root
    }

    private fun refreshAudioList() {
        val audioDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val updatedFiles = audioDir?.listFiles { file ->
            file.extension == "mp3" || file.extension == "wav"
        }?.toList() ?: emptyList()

        audioAdapter = AudioAdapter(
            requireContext(),
            updatedFiles,
            onPlayClicked = { file, seekBar, playButton ->
                currentHelper?.release()
                currentHelper = AudioPlayerHelper(requireContext(), seekBar, playButton).also {
                    it.togglePlayPause(file.absolutePath)
                }
            },
            onDeleteClicked = { fileToDelete ->
                fileToDelete.delete()
                Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show()
                refreshAudioList()
            }
        )
        binding.rvRecordings.adapter = audioAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentHelper?.release()
        _binding = null
    }
}

