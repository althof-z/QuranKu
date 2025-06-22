package com.example.quranku

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class TajwidRepository(private val context: Context) {
    
    // Set this to true to use the real API
    private val useRealApi = true
    
    private val apiService: TajwidApiService by lazy {
        if (useRealApi) {
            RealTajwidApiService.apiService
        } else {
            MockTajwidApiService()
        }
    }
    
    suspend fun analyzeTajwidFromUri(audioUri: Uri): Result<TajwidApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert Uri to File
                val audioFile = uriToFile(audioUri)
                
                // Create MultipartBody.Part
                val requestBody = audioFile.asRequestBody(ApiConfig.AUDIO_MIME_TYPE.toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData(ApiConfig.AUDIO_FIELD_NAME, audioFile.name, requestBody)
                
                // Make API call
                val response = apiService.analyzeTajwid(audioPart)
                
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("API call failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun analyzeTajwidFromFile(filePath: String): Result<TajwidApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val audioFile = File(filePath)
                if (!audioFile.exists()) {
                    return@withContext Result.failure(Exception("Audio file not found"))
                }
                
                // Create MultipartBody.Part
                val requestBody = audioFile.asRequestBody(ApiConfig.AUDIO_MIME_TYPE.toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData(ApiConfig.AUDIO_FIELD_NAME, audioFile.name, requestBody)
                
                // Make API call
                val response = apiService.analyzeTajwid(audioPart)
                
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("API call failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.wav")
        val outputStream = FileOutputStream(file)
        
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        return file
    }
    
    // Mock API Service for simulation (fallback)
    private class MockTajwidApiService : TajwidApiService {
        override suspend fun analyzeTajwid(audioFile: MultipartBody.Part): retrofit2.Response<TajwidApiResponse> {
            // Simulate network delay
            kotlinx.coroutines.delay(ApiConfig.MOCK_DELAY_MS)
            
            // Simulate response with realistic probabilities
            val random = java.util.Random()
            val response = TajwidApiResponse(
                ikhfa = random.nextFloat() < ApiConfig.IKHFAA_PROBABILITY,
                idgham = random.nextFloat() < ApiConfig.IDGHAM_PROBABILITY,
                mad = random.nextFloat() < ApiConfig.MAD_PROBABILITY
            )
            
            return retrofit2.Response.success(response)
        }
    }
} 