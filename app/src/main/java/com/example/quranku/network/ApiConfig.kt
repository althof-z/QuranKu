package com.example.quranku.network

object ApiConfig {
    
    // API Configuration
    const val BASE_URL = "https://quranku-fastapi-e13b92394c46.herokuapp.com/"
    const val ENDPOINT_ANALYZE_TAJWID = "predict"
    
    // Timeout settings (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // File upload settings
    const val AUDIO_MIME_TYPE = "audio/wav"
    const val AUDIO_FIELD_NAME = "audio_file"
    
    // Mock settings (no longer needed for real API)
    const val MOCK_DELAY_MS = 2000L // Simulate network delay
    
    // Response simulation probabilities (for mock service - no longer used)
    const val IKHFAA_PROBABILITY = 0.7f
    const val IDGHAM_PROBABILITY = 0.6f
    const val MAD_PROBABILITY = 0.8f
} 