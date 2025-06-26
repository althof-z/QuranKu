package com.example.quranku.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_recordings")
data class AudioRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val fileName: String,
    val filePath: String,
    val duration: Long, // in milliseconds
    val createdAt: Long, // timestamp
    
    // Tajwid analysis results
    val ikhfa: Boolean? = null, // null means still loading
    val idgham: Boolean? = null, // null means still loading
    val mad: Boolean? = null, // null means still loading
    
    // Loading state
    val isAnalyzing: Boolean = true // true means waiting for API response
) 