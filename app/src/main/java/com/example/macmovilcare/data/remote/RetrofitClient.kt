package com.example.macmovilcare.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    // Si usas un servidor local en tu PC con el emulador de Android, usa "http://10.0.2.2:8080/api/"
    // Si usas Mocky o una API en la nube, reemplaza esta URL por la tuya (debe terminar en '/')
    private const val BASE_URL = "https://6af76dc8e844c7ff741c72ee.mockapi.io/api/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}