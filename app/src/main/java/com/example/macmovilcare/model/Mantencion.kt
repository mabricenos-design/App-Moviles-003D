package com.example.macmovilcare.model

data class Mantencion(
    val id: String,
    val equipoId: String,
    val fechaSugerida: String,
    val tipoMantencion: String, // ej: "Limpieza interna y cambio de pasta térmica"
    val realizada: Boolean = false
)
