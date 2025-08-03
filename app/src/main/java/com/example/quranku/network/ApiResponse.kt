package com.example.quranku.network

import com.google.gson.annotations.SerializedName

data class TajwidApiResponse(
    @SerializedName("mad")
    val mad: Boolean,
    
    @SerializedName("idgham")
    val idgham: Boolean,
    
    @SerializedName("ikhfa")
    val ikhfa: Boolean,
    
    @SerializedName("mad_prob")
    val madProb: Double? = null,
    
    @SerializedName("idgham_prob")
    val idghamProb: Double? = null,
    
    @SerializedName("ikhfa_prob")
    val ikhfaProb: Double? = null,
    
    @SerializedName("file")
    val file: String? = null
) 