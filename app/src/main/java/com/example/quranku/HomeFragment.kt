package com.example.quranku

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
    private var startTime = 0L
    private val timerHandler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val minutes = (elapsed / 1000 / 60).toInt()
            val seconds = (elapsed / 1000 % 60).toInt()
            binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupListeners()
        updateUI("Status: Idle", R.drawable.ic_mic)
        return binding.root
    }

    private fun setupListeners() = binding.run {
        btnRecord.setOnClickListener {
            if (checkPermissions()) {
                if (!isRecording) startRecording() else stopRecording()
            }
        }

        btnSave.setOnClickListener {
            if (isRecording) stopRecording() // Stop before saving
            if (isRecorded) {
                showToast("Recording saved: $outputFile")
                resetRecorder()
            } else {
                showToast("No recording to save")
            }
        }

        btnDiscard.setOnClickListener {
           discardRecording()
        }
    }

    private fun discardRecording(){
        if (isRecording) stopRecording() // Stop before discard
        if (isRecorded) {
            File(outputFile).takeIf { it.exists() }?.delete()
            showToast("Recording discarded")
            resetRecorder()
        } else {
            showToast("No recording to discard")
        }
    }


    private fun startRecording() {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.apply { mkdirs() }

        outputFile = "${dir?.absolutePath}/recording_${System.currentTimeMillis()}.wav"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)
            try {
                prepare()
                start()
                startTime = System.currentTimeMillis()
                timerHandler.post(timerRunnable)
                isRecording = true
                isRecorded = false
                updateUI("Recording...", R.drawable.ic_stop)
            } catch (e: IOException) {
                showToast("Failed to start recording")
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        timerHandler.removeCallbacks(timerRunnable)
        isRecording = false
        isRecorded = true
        updateUI("Recording Stopped", R.drawable.ic_mic)
    }

    private fun resetRecorder() {
        isRecording = false
        isRecorded = false
        outputFile = ""
        timerHandler.removeCallbacks(timerRunnable)
        binding.tvTimer.text = "00:00"
        updateUI("Status: Idle", R.drawable.ic_mic)
    }


    private fun updateUI(status: String, iconRes: Int) {
        binding.tvStatus.text = status
        binding.btnRecord.setBackgroundResource(iconRes)

        val showSaveDiscard = isRecording || isRecorded
        binding.btnSave.visibility = if (showSaveDiscard) View.VISIBLE else View.GONE
        binding.btnDiscard.visibility = if (showSaveDiscard) View.VISIBLE else View.GONE
    }


    private fun checkPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE
            )
            false
        } else true
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            val msg = if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                "Permission Granted"
            } else {
                "Permission Denied"
            }
            showToast(msg)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopRecording()
            discardRecording()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
