package com.example.macmovilcare.viewmodel

import androidx.lifecycle.ViewModel
import com.example.macmovilcare.model.Cotizacion
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrdenesViewModel : ViewModel() {

    // Lista de órdenes simuladas para el MVP académico
    private val ordenesIniciales = listOf(
        OrdenTrabajo(
            id = "101",
            equipoId = "EQ-550",
            clienteId = "CLI-01",
            fechaIngreso = "10-10-2026",
            estado = EstadoOrden.COTIZADO_PENDIENTE,
            diagnostico = "Falla en la tarjeta de video y suciedad interna.",
            cotizacion = Cotizacion(
                id = "COT-101",
                montoTotal = 45000.0,
                detalleServicios = "Limpieza profunda + cambio de componente electrónico",
                requiereAutorizacion = true,
                estadoAutorizacion = "PENDIENTE"
            )
        ),
        OrdenTrabajo(
            id = "102",
            equipoId = "EQ-720",
            clienteId = "CLI-01",
            fechaIngreso = "14-10-2026",
            estado = EstadoOrden.EN_REPARACION,
            diagnostico = "Mantención preventiva y cambio de batería."
        )
    )

    private val _ordenes = MutableStateFlow(ordenesIniciales)
    val ordenes: StateFlow<List<OrdenTrabajo>> = _ordenes.asStateFlow()

    private val _ordenSeleccionada = MutableStateFlow<OrdenTrabajo?>(null)
    val ordenSeleccionada: StateFlow<OrdenTrabajo?> = _ordenSeleccionada.asStateFlow()

    fun seleccionarOrden(ordenId: String) {
        _ordenSeleccionada.value = _ordenes.value.find { it.id == ordenId }
    }

    fun responderCotizacion(ordenId: String, aprobada: Boolean) {
        val nuevoEstadoAutorizacion = if (aprobada) "APROBADO" else "RECHAZADO"
        val nuevoEstadoOrden = if (aprobada) EstadoOrden.APROBADO else EstadoOrden.RECHAZADO

        _ordenes.update { lista ->
            lista.map { orden ->
                if (orden.id == ordenId && orden.cotizacion != null) {
                    orden.copy(
                        estado = nuevoEstadoOrden,
                        cotizacion = orden.cotizacion.copy(estadoAutorizacion = nuevoEstadoAutorizacion)
                    )
                } else {
                    orden
                }
            }
        }
        seleccionarOrden(ordenId)
    }
}