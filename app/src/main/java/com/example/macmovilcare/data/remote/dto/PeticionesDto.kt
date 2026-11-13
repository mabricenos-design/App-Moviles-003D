package com.example.macmovilcare.data.remote.dto

// Lo que envías a la API desde la app
data class LoginRequest(
    val correo: String,
    val clave: String
)

data class NuevaSolicitudRequest(
    val equipoId: String,
    val clienteId: String = "CLI-01",
    val fechaIngreso: String,
    val estado: String = "INGRESADO",
    val diagnostico: String
)

data class NuevaCotizacionRequest(
    val diagnostico: String,
    val montoTotal: Double,
    val detallesServicios: String
)

data class ActualizarEstadoRequest(
    val nuevoEstado: String
)

// Lo que la API te responde (además de tus DTOs actuales)
data class LoginResponse(
    val token: String,
    val usuario: UsuarioDto
)