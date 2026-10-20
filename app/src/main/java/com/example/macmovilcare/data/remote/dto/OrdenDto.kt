package com.example.macmovilcare.data.remote.dto

data class OrdenDto(
    val id: String,
    val equipoId: String,
    val clienteId: String,
    val fechaIngreso: String,
    val estado: String,
    val diagnostico: String?
)
