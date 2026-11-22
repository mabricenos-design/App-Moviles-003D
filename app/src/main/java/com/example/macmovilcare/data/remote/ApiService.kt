package com.example.macmovilcare.data.remote

import com.example.macmovilcare.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // --- TUS FUNCIONES ACTUALES ---
    @GET("ordenes")
    suspend fun obtenerOrdenes(): List<OrdenDto>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: String): UsuarioDto


    // --- TUS NUEVAS FUNCIONES ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("ordenes")
    suspend fun crearSolicitud(
        @Header("Authorization") token: String,
        @Body request: NuevaSolicitudRequest
    ): Response<OrdenDto>

    @POST("ordenes/{id}/cotizacion")
    suspend fun generarCotizacion(
        @Header("Authorization") token: String,
        @Path("id") idOrden: String,
        @Body request: NuevaCotizacionRequest
    ): Response<OrdenDto>

    @PUT("ordenes/{id}/estado")
    suspend fun actualizarEstadoOrden(
        @Header("Authorization") token: String,
        @Path("id") idOrden: String,
        @Body request: ActualizarEstadoRequest
    ): Response<OrdenDto>

    @DELETE("ordenes/{id}")
    suspend fun eliminarOrden(
        @Header("Authorization") token: String,
        @Path("id") idOrden: String
    ): Response<Unit>
}