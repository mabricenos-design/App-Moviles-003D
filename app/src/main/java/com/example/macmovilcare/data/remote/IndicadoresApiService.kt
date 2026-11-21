package com.example.macmovilcare.data.remote

import retrofit2.http.GET

interface IndicadoresApiService {
    @GET("api")
    suspend fun obtenerIndicadores(): IndicadoresResponse
}