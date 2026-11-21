package com.example.macmovilcare.data.remote

data class IndicadoresResponse(
    val dolar: IndicadorDetalle,
    val uf: IndicadorDetalle
)

data class IndicadorDetalle(
    val codigo: String,
    val nombre: String,
    val valor: Double
)