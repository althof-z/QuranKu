package com.example.quranku

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val retrofit: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://iris-prediction-app-420-3e797f12e225.herokuapp.com") // localhost for emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
