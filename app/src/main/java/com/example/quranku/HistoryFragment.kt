package com.example.quranku

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quranku.databinding.FragmentHistoryBinding
import java.io.File

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var audioAdapter: AudioAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        recyclerView = binding.rvRecordings

        // Fetch audio files from external directory
        val audioDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val audioFiles = audioDir?.listFiles { file -> file.extension == "mp3" || file.extension == "wav" }?.toList() ?: emptyList()

        audioAdapter = AudioAdapter(requireContext(), audioFiles)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = audioAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
