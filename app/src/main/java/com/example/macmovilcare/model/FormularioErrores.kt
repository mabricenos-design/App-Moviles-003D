package com.example.macmovilcare.model

data class SolicitudUiState(
    val equipoId: String = "",
    val descripcionProblema: String = "",
    val cargando: Boolean = false,
    val mensajeExito: String? = null,
    val errores: SolicitudErrores = SolicitudErrores()
)

data class SolicitudErrores(
    val equipoId: String? = null,
    val descripcionProblema: String? = null
)