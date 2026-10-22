package com.example.macmovilcare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ordenes_trabajo")
data class OrdenEntity(
    @PrimaryKey val id: String,
    val equipoId: String,
    val clienteId: String,
    val fechaIngreso: String,
    val estado: String,
    val diagnostico: String?,
    val montoCotizacion: Double? = null,
    val detalleCotizacion: String? = null
)