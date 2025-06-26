package com.example.quranku.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TajwidApiService {
    @Multipart
    @POST("predict")
    suspend fun analyzeTajwid(
        @Part audioFile: MultipartBody.Part
    ): Response<TajwidApiResponse>
} 