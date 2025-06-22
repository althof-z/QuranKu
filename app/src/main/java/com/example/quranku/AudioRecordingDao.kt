package com.example.quranku

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordingDao {
    
    @Query("SELECT * FROM audio_recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<AudioRecording>>
    
    @Query("SELECT * FROM audio_recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): AudioRecording?
    
    @Insert
    suspend fun insertRecording(recording: AudioRecording): Long
    
    @Delete
    suspend fun deleteRecording(recording: AudioRecording)
    
    @Query("DELETE FROM audio_recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)
    
    @Query("DELETE FROM audio_recordings WHERE filePath = :filePath")
    suspend fun deleteRecordingByPath(filePath: String)
    
    @Query("SELECT COUNT(*) FROM audio_recordings")
    suspend fun getRecordingsCount(): Int
    
    @Query("SELECT SUM(duration) FROM audio_recordings")
    suspend fun getTotalDuration(): Long?
    
    // Update tajwid analysis results
    @Query("UPDATE audio_recordings SET ikhfa = :ikhfa, idgham = :idgham, mad = :mad, isAnalyzing = :isAnalyzing WHERE id = :id")
    suspend fun updateTajwidResults(id: Long, ikhfa: Boolean, idgham: Boolean, mad: Boolean, isAnalyzing: Boolean = false)
    
    // Update loading state
    @Query("UPDATE audio_recordings SET isAnalyzing = :isAnalyzing WHERE id = :id")
    suspend fun updateAnalyzingState(id: Long, isAnalyzing: Boolean)
} 