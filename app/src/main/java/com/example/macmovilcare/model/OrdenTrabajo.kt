package com.example.macmovilcare.model

enum class EstadoOrden {
    INGRESADO,
    EN_DIAGNOSTICO,
    COTIZADO_PENDIENTE,
    APROBADO,
    RECHAZADO,
    EN_REPARACION,
    LISTO_PARA_ENTREGA,
    ENTREGADO
}

data class OrdenTrabajo(
    val id: String,
    val equipoId: String,
    val clienteId: String,
    val fechaIngreso: String,
    val estado: EstadoOrden,
    val diagnostico: String? = null,
    val cotizacion: Cotizacion? = null,
    val evidencias: List<Evidencia> = emptyList(),
    val garantia: Garantia? = null,
    val proximaMantencion: Mantencion? = null
)