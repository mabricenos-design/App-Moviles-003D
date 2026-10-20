package com.example.macmovilcare.model

data class Equipo(
    val id: String,
    val clienteId: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val accesorios: String = "Ninguno"
)