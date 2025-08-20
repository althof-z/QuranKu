package com.example.quranku.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.example.quranku.data.repository.AudioRepository
import com.example.quranku.data.repository.TajwidRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FirstTimeSetupHelper(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "QuranKuPrefs"
        private const val KEY_FIRST_TIME_SETUP = "first_time_setup_completed"
        private const val KEY_DEMO_FILES_ANALYZED = "demo_files_analyzed"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val audioRepository = AudioRepository(context)
    private val tajwidRepository = TajwidRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    fun isFirstTimeSetup(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_TIME_SETUP, false)
    }
    
    fun markFirstTimeSetupCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_TIME_SETUP, true).apply()
    }
    
    fun areDemoFilesAnalyzed(): Boolean {
        return prefs.getBoolean(KEY_DEMO_FILES_ANALYZED, false)
    }
    
    fun markDemoFilesAnalyzed() {
        prefs.edit().putBoolean(KEY_DEMO_FILES_ANALYZED, true).apply()
    }
    
    fun resetFirstTimeSetup() {
        prefs.edit()
            .putBoolean(KEY_FIRST_TIME_SETUP, false)
            .putBoolean(KEY_DEMO_FILES_ANALYZED, false)
            .apply()
    }
    
    fun getDemoFilesStatus(): String {
        val isFirstTime = isFirstTimeSetup()
        val areAnalyzed = areDemoFilesAnalyzed()
        
        return when {
            isFirstTime -> "First time setup not completed"
            areAnalyzed -> "Demo files analyzed"
            else -> "Demo files copied but not analyzed"
        }
    }
    
    fun checkDemoFilesExist(): Boolean {
        val demoFiles = listOf(
            "Demo_Sound_93_3.wav",
            "Demo_Sound_11_2.wav",
            "Demo_Sound_10_10.wav"
        )
        
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        
        return demoFiles.all { fileName ->
            val file = File(outputDir, fileName)
            file.exists()
        }
    }
    
    fun checkRawResourcesExist(): Boolean {
        val rawFiles = listOf("s93_3.wav", "s11_2.wav", "s10_10.wav")
        
        return rawFiles.all { fileName ->
            val resourceId = context.resources.getIdentifier(fileName, "raw", context.packageName)
            resourceId != 0
        }
    }
    
    fun performFirstTimeSetup() {
        if (!isFirstTimeSetup()) return
        
        coroutineScope.launch {
            try {
                // Copy demo WAV files to app storage
                copyDemoFiles()
                
                // Mark first time setup as completed
                markFirstTimeSetupCompleted()
                
                // Analyze demo files in background
                analyzeDemoFiles()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun manuallyAnalyzeDemoFiles() {
        coroutineScope.launch {
            try {
                analyzeDemoFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun copyDemoFiles() {
        val demoFiles = listOf(
            "s93_3.wav" to "Demo_Sound_93_3.wav",
            "s11_2.wav" to "Demo_Sound_11_2.wav",
            "s10_10.wav" to "Demo_Sound_10_10.wav"
        )
        
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.apply { mkdirs() }
        android.util.Log.d("FirstTimeSetupHelper", "Output directory: $outputDir")
        
        demoFiles.forEach { (rawFileName, outputFileName) ->
            try {
                // Try multiple approaches to get the resource
                var resourceId = context.resources.getIdentifier(rawFileName, "raw", context.packageName)
                android.util.Log.d("FirstTimeSetupHelper", "Looking for resource: $rawFileName, ID: $resourceId")
                
                // If not found, try without extension
                if (resourceId == 0) {
                    val nameWithoutExtension = rawFileName.substringBeforeLast(".")
                    resourceId = context.resources.getIdentifier(nameWithoutExtension, "raw", context.packageName)
                    android.util.Log.d("FirstTimeSetupHelper", "Trying without extension: $nameWithoutExtension, ID: $resourceId")
                }
                
                if (resourceId != 0) {
                    val inputStream = context.resources.openRawResource(resourceId)
                    
                    val outputFile = File(outputDir, outputFileName)
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    inputStream.close()
                    android.util.Log.d("FirstTimeSetupHelper", "Successfully copied $rawFileName to $outputFile")
                } else {
                    android.util.Log.e("FirstTimeSetupHelper", "Resource not found: $rawFileName")
                    // Try to list all raw resources to debug
                    try {
                        val rawResources = context.resources.assets.list("") ?: emptyArray()
                        android.util.Log.d("FirstTimeSetupHelper", "Available assets: ${rawResources.joinToString(", ")}")
                    } catch (e: Exception) {
                        android.util.Log.e("FirstTimeSetupHelper", "Could not list assets: ${e.message}")
                    }
                }
                
            } catch (e: IOException) {
                android.util.Log.e("FirstTimeSetupHelper", "Error copying file $rawFileName: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun analyzeDemoFiles() {
        if (areDemoFilesAnalyzed()) return
        
        val demoFiles = listOf(
            "Demo_Sound_93_3.wav",
            "Demo_Sound_11_2.wav",
            "Demo_Sound_10_10.wav"
        )
        
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        
        demoFiles.forEach { fileName ->
            try {
                val filePath = File(outputDir, fileName).absolutePath
                val file = File(filePath)
                
                if (file.exists()) {
                    android.util.Log.d("FirstTimeSetupHelper", "Analyzing demo file: $fileName")
                    val duration = audioRepository.getAudioDuration(filePath)
                    
                    // Save to database with loading state
                    val recordingId = audioRepository.insertRecording(
                        fileName = fileName,
                        filePath = filePath,
                        duration = duration
                    )
                    android.util.Log.d("FirstTimeSetupHelper", "Demo file saved to database: $fileName with ID: $recordingId")
                } else {
                    android.util.Log.w("FirstTimeSetupHelper", "Demo file not found: $filePath")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("FirstTimeSetupHelper", "Error analyzing demo file $fileName: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Mark demo files as analyzed
        markDemoFilesAnalyzed()
        android.util.Log.d("FirstTimeSetupHelper", "Demo files analysis completed")
    }
} 