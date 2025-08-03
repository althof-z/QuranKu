package com.example.quranku.data.repository

import android.content.Context
import android.media.MediaPlayer
import com.example.quranku.data.entity.AudioRecording
import com.example.quranku.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

class AudioRepository(context: Context) {
    
    private val audioRecordingDao = AppDatabase.getDatabase(context).audioRecordingDao()
    private val tajwidRepository = TajwidRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Get all recordings as Flow
    fun getAllRecordings(): Flow<List<AudioRecording>> {
        return audioRecordingDao.getAllRecordings()
    }
    
    // Insert new recording with loading state, then analyze in background
    suspend fun insertRecording(
        fileName: String,
        filePath: String,
        duration: Long
    ): Long {
        // First, save recording with loading state
        val recording = AudioRecording(
            fileName = fileName,
            filePath = filePath,
            duration = duration,
            createdAt = System.currentTimeMillis(),
            ikhfa = null,
            idgham = null,
            mad = null,
            isAnalyzing = true
        )
        
        val recordingId = audioRecordingDao.insertRecording(recording)
        
        // Start background analysis
        coroutineScope.launch {
            analyzeTajwidInBackground(recordingId, filePath)
        }
        
        return recordingId
    }
    
    // Analyze tajwid in background and update database
    private suspend fun analyzeTajwidInBackground(recordingId: Long, filePath: String) {
        try {
            // Get real tajwid analysis from API
            val tajwidResult = tajwidRepository.analyzeTajwidFromFile(filePath)
            
            if (tajwidResult.isSuccess) {
                val response = tajwidResult.getOrNull()!!
                android.util.Log.d("AudioRepository", "Saving tajwid results to DB: ikhfa=${response.ikhfa}, idgham=${response.idgham}, mad=${response.mad}")
                // Update database with real results
                audioRecordingDao.updateTajwidResults(
                    id = recordingId,
                    ikhfa = response.ikhfa,
                    idgham = response.idgham,
                    mad = response.mad,
                    isAnalyzing = false
                )
                android.util.Log.d("AudioRepository", "Successfully saved tajwid results to DB for recording $recordingId")
            } else {
                android.util.Log.e("AudioRepository", "API failed for recording $recordingId: ${tajwidResult.exceptionOrNull()?.message}")
                // API failed, mark as not analyzing but keep null results
                audioRecordingDao.updateAnalyzingState(recordingId, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Mark as not analyzing on error
            audioRecordingDao.updateAnalyzingState(recordingId, false)
        }
    }
    
    // Delete recording
    suspend fun deleteRecording(recording: AudioRecording) {
        // Delete the actual file first
        val file = File(recording.filePath)
        if (file.exists()) {
            file.delete()
        }
        
        // Then delete from database
        audioRecordingDao.deleteRecording(recording)
    }
    
    // Delete recording by ID
    suspend fun deleteRecordingById(id: Long) {
        val recording = audioRecordingDao.getRecordingById(id)
        recording?.let { deleteRecording(it) }
    }
    
    // Get recordings count
    suspend fun getRecordingsCount(): Int {
        return audioRecordingDao.getRecordingsCount()
    }
    
    // Get total duration
    suspend fun getTotalDuration(): Long {
        return audioRecordingDao.getTotalDuration() ?: 0L
    }
    
    // Get audio duration from file
    fun getAudioDuration(filePath: String): Long {
        return try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration.toLong()
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
    
    // Generate random tajwid results (fallback)
    fun generateRandomTajwidResults(): Triple<Boolean, Boolean, Boolean> {
        val random = java.util.Random()
        return Triple(
            random.nextBoolean(), // ikhfa
            random.nextBoolean(), // idgham
            random.nextBoolean()  // mad
        )
    }
} 