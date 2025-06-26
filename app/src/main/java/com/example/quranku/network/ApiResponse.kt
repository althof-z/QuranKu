package com.example.quranku.network

import com.google.gson.annotations.SerializedName

data class TajwidApiResponse(
    @SerializedName("mad")
    val mad: Boolean,
    
    @SerializedName("idgham")
    val idgham: Boolean,
    
    @SerializedName("ikfha")
    val ikhfa: Boolean
) 