package com.example.macmovilcare.model

data class Cotizacion(
    val id: String,
    val montoTotal: Double,
    val detalleServicios: String,
    val requiereAutorizacion: Boolean = true,
    val estadoAutorizacion: String = "PENDIENTE"
)
