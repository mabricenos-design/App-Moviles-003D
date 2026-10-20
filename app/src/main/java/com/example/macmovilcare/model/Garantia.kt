package com.example.macmovilcare.model

data class Garantia(
    val id: String,
    val fechaInicio: String,
    val fechaVencimiento: String,
    val coberturaDetalle: String,
    val estaActiva: Boolean = true
)
