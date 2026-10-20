package com.example.macmovilcare.data.remote

import com.example.macmovilcare.data.remote.dto.OrdenDto
import com.example.macmovilcare.data.remote.dto.UsuarioDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("ordenes")
    suspend fun obtenerOrdenes(): List<OrdenDto>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: String): UsuarioDto
}