package com.example.quranku.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.quranku.R
import android.widget.ImageButton
import android.widget.SeekBar
import com.example.quranku.ui.history.AudioPlayerHelper
import com.example.quranku.databinding.FragmentTajwidBinding

class TajwidFragment : Fragment() {
    private var madPlayer: AudioPlayerHelper? = null
    private var ikhfaPlayer: AudioPlayerHelper? = null
    private var idghamPlayer: AudioPlayerHelper? = null
    private var _binding: FragmentTajwidBinding? = null
    private val binding: FragmentTajwidBinding by lazy { _binding!! }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTajwidBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Mad
        madPlayer = AudioPlayerHelper(requireContext(), binding.sbMad, binding.btnPlayMad)
        binding.btnPlayMad.setOnClickListener {
            madPlayer?.togglePlayPause(R.raw.mad_audio)
        }
        // Ikhfa
        ikhfaPlayer = AudioPlayerHelper(requireContext(), binding.sbIkh, binding.btnPlayikh)
        binding.btnPlayikh.setOnClickListener {
            ikhfaPlayer?.togglePlayPause(R.raw.ikhfa_audio)
        }
        // Idgham
        idghamPlayer = AudioPlayerHelper(requireContext(), binding.sbIdg1, binding.btnPlaySurah)
        binding.btnPlaySurah.setOnClickListener {
            idghamPlayer?.togglePlayPause(R.raw.idgham_audio)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        madPlayer?.release()
        ikhfaPlayer?.release()
        idghamPlayer?.release()
        _binding = null
    }
}