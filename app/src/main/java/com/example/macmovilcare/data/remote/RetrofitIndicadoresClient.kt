package com.example.macmovilcare.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitIndicadoresClient {

    private const val BASE_URL = "https://mindicador.cl/"

    val apiService: IndicadoresApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApiService::class.java)
    }
}