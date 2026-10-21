package com.example.macmovilcare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.model.Cotizacion
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdenesViewModel(application: Application) : AndroidViewModel(application) {

    private val db by lazy { AppDatabase.getDatabase(getApplication()) }

    private val _ordenes = MutableStateFlow<List<OrdenTrabajo>>(emptyList())
    val ordenes: StateFlow<List<OrdenTrabajo>> = _ordenes.asStateFlow()

    private val _ordenSeleccionada = MutableStateFlow<OrdenTrabajo?>(null)
    val ordenSeleccionada: StateFlow<OrdenTrabajo?> = _ordenSeleccionada.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = db.ordenDao()
                dao.obtenerTodasLasOrdenes().collect { entidades ->
                    if (entidades.isEmpty()) {
                        val datosPrueba = listOf(
                            OrdenEntity("101", "EQ-550", "CLI-01", "10-10-2026", "COTIZADO_PENDIENTE", "Falla en la tarjeta de video y suciedad interna."),
                            OrdenEntity("102", "EQ-720", "CLI-01", "14-10-2026", "EN_REPARACION", "Mantención preventiva y cambio de batería.")
                        )
                        dao.insertarOrdenes(datosPrueba)
                    } else {
                        _ordenes.value = entidades.map { entidad ->
                            OrdenTrabajo(
                                id = entidad.id,
                                equipoId = entidad.equipoId,
                                clienteId = entidad.clienteId,
                                fechaIngreso = entidad.fechaIngreso,
                                estado = runCatching { EstadoOrden.valueOf(entidad.estado) }.getOrDefault(EstadoOrden.INGRESADO),
                                diagnostico = entidad.diagnostico,
                                cotizacion = if (entidad.id == "101") {
                                    Cotizacion(
                                        id = "COT-101",
                                        montoTotal = 45000.0,
                                        detalleServicios = "Limpieza profunda + cambio de componente electrónico",
                                        requiereAutorizacion = true,
                                        estadoAutorizacion = "PENDIENTE"
                                    )
                                } else null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seleccionarOrden(ordenId: String) {
        _ordenSeleccionada.value = _ordenes.value.find { it.id == ordenId }
    }

    fun responderCotizacion(ordenId: String, aprobada: Boolean) {
        val nuevoEstado = if (aprobada) "APROBADO" else "RECHAZADO"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.ordenDao().actualizarEstado(ordenId, nuevoEstado)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}