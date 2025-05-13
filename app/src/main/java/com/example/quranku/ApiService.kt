package com.example.quranku

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class PredictRequest(val features: List<Float>)
data class PredictResponse(val predicted_class: String)

interface ApiService {
    @POST("/predict")
    fun predict(@Body request: PredictRequest): Call<PredictResponse>
}
