package com.example.quranku

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.quranku.databinding.FragmentHomeBinding
import java.io.File
import java.io.IOException

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private var isRecording = false
    private var isRecorded = false

    private val RECORD_AUDIO_REQUEST_CODE = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        binding.btnRecord.setOnClickListener {
            if (checkPermissions()) {
                if (!isRecording) {
                    startRecording()
                } else {
                    stopRecording()
                }
            }
        }

        binding.btnSave.setOnClickListener {
            if (isRecorded) {
                Toast.makeText(requireContext(), "Recording saved: $outputFile", Toast.LENGTH_SHORT).show()
                resetRecorder()
            } else {
                Toast.makeText(requireContext(), "No recording to save", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDiscard.setOnClickListener {
            discardRecording()
        }
    }

    private fun startRecording() {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (dir != null && !dir.exists()) {
            dir.mkdirs()
        }

        outputFile = "${dir?.absolutePath}/recording_${System.currentTimeMillis()}.wav"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)
            try {
                prepare()
                start()
                isRecording = true
                isRecorded = false
                binding.tvStatus.text = "Recording..."
                binding.btnRecord.setBackgroundResource(R.drawable.ic_stop)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        isRecorded = true
        binding.tvStatus.text = "Recording Stopped"
        binding.btnRecord.setBackgroundResource(R.drawable.ic_record)
    }

    private fun discardRecording() {
        if (isRecorded) {
            val file = File(outputFile)
            if (file.exists()) {
                file.delete()
            }
            Toast.makeText(requireContext(), "Recording discarded", Toast.LENGTH_SHORT).show()
            resetRecorder()
        } else {
            Toast.makeText(requireContext(), "No recording to discard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetRecorder() {
        isRecorded = false
        outputFile = ""
        binding.tvStatus.text = "Status: Idle"
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}