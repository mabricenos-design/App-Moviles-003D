package com.example.macmovilcare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.data.remote.RetrofitClient
import com.example.macmovilcare.data.remote.RetrofitIndicadoresClient // <-- IMPORT AGREGADO
import com.example.macmovilcare.data.repository.MCmovilRepository
import com.example.macmovilcare.model.Cotizacion
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.Evidencia
import com.example.macmovilcare.model.OrdenTrabajo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class OrdenesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MCmovilRepository by lazy {
        val dao = AppDatabase.getDatabase(getApplication()).ordenDao()
        MCmovilRepository(ordenDao = dao, apiService = RetrofitClient.apiService)
    }

    private val _ordenes = MutableStateFlow<List<OrdenTrabajo>>(emptyList())
    val ordenes: StateFlow<List<OrdenTrabajo>> = _ordenes.asStateFlow()

    private val _ordenSeleccionada = MutableStateFlow<OrdenTrabajo?>(null)
    val ordenSeleccionada: StateFlow<OrdenTrabajo?> = _ordenSeleccionada.asStateFlow()

    // Estados restaurados para el flujo del Técnico y Notificaciones
    private val _notificacionTecnico = MutableStateFlow<String?>(null)
    val notificacionTecnico: StateFlow<String?> = _notificacionTecnico.asStateFlow()

    // Estado para el valor del dólar desde la API externa
    private val _valorDolar = MutableStateFlow<Double>(950.0) // Valor respaldo offline
    val valorDolar: StateFlow<Double> = _valorDolar.asStateFlow()

    // Mapa local en memoria para conservar cotizaciones generadas en la sesión
    private val cotizacionesMap = mutableMapOf<String, Cotizacion>(
        "101" to Cotizacion(
            id = "COT-101",
            montoTotal = 45000.0,
            detalleServicios = "Limpieza profunda + cambio de componente electrónico",
            requiereAutorizacion = true,
            estadoAutorizacion = "PENDIENTE"
        )
    )

    // 1. AGREGADO: Mapa local en memoria para conservar las evidencias (fotos) subidas por el técnico
    private val evidenciasMap = mutableMapOf<String, List<Evidencia>>()

    init {
        sincronizarYObservarOrdenes()
        cargarValorDolar()
    }

    private fun sincronizarYObservarOrdenes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sincronizarOrdenes()
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.todasLasOrdenes.collect { entidades ->
                    if (entidades.isEmpty()) {
                        val dao = AppDatabase.getDatabase(getApplication()).ordenDao()
                        val datosPrueba = listOf(
                            OrdenEntity(
                                id = "101",
                                equipoId = "EQ-550",
                                clienteId = "CLI-01",
                                fechaIngreso = "10-10-2026",
                                estado = "COTIZADO_PENDIENTE",
                                diagnostico = "Falla en la tarjeta de video y suciedad interna."
                            ),
                            OrdenEntity(
                                id = "102",
                                equipoId = "EQ-720",
                                clienteId = "CLI-01",
                                fechaIngreso = "14-10-2026",
                                estado = "EN_REPARACION",
                                diagnostico = "Mantención preventiva y cambio de batería."
                            )
                        )
                        dao.insertarOrdenes(datosPrueba)
                    } else {
                        _ordenes.value = entidades.map { entidad ->
                            OrdenTrabajo(
                                id = entidad.id,
                                equipoId = entidad.equipoId,
                                clienteId = entidad.clienteId,
                                fechaIngreso = entidad.fechaIngreso,
                                estado = runCatching { EstadoOrden.valueOf(entidad.estado) }
                                    .getOrDefault(EstadoOrden.INGRESADO),
                                diagnostico = entidad.diagnostico,
                                cotizacion = cotizacionesMap[entidad.id],
                                // 2. AGREGADO: Se asignan las evidencias guardadas
                                evidencias = evidenciasMap[entidad.id] ?: emptyList()
                            )
                        }
                        _ordenSeleccionada.value?.let { actual ->
                            _ordenSeleccionada.value = _ordenes.value.find { it.id == actual.id }
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
        val tokenPrueba = "token-academico-simulacion"

        cotizacionesMap[ordenId]?.let { cot ->
            cotizacionesMap[ordenId] = cot.copy(estadoAutorizacion = nuevoEstado)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val exitoRemoto = repository.responderCotizacionRemota(
                token = tokenPrueba,
                ordenId = ordenId,
                nuevoEstado = nuevoEstado
            )
            if (!exitoRemoto) {
                repository.actualizarEstadoOrden(ordenId, nuevoEstado)
            }
        }
    }

    // Funciones restauradas para AppNavigation
    fun dispararNotificacionNuevaSolicitud(equipoId: String, descripcion: String) {
        _notificacionTecnico.value = "Nueva solicitud recibida para equipo $equipoId: $descripcion"
    }

    fun limpiarNotificacion() {
        _notificacionTecnico.value = null
    }

    // 3. ACTUALIZADO: Firma con los 5 parámetros que pide AppNavigation.kt
    fun responderSolicitudComoTecnico(
        ordenId: String,
        diagnostico: String,
        monto: Double,
        detalles: String,
        uriFoto: String? = null
    ) {
        val tokenPrueba = "token-academico-simulacion"
        cotizacionesMap[ordenId] = Cotizacion(
            id = "COT-$ordenId",
            montoTotal = monto,
            detalleServicios = detalles,
            requiereAutorizacion = true,
            estadoAutorizacion = "PENDIENTE"
        )

        if (!uriFoto.isNullOrBlank()) {
            val nuevaEvidencia = Evidencia(
                id = "EV-${System.currentTimeMillis()}",
                ordenId = ordenId,
                urlImagenOPathLocal = uriFoto,
                descripcion = "Evidencia técnica adjunta",
                fechaRegistro = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            )
            val listaActual = evidenciasMap[ordenId]?.toMutableList() ?: mutableListOf()
            listaActual.add(nuevaEvidencia)
            evidenciasMap[ordenId] = listaActual
        }

        viewModelScope.launch(Dispatchers.IO) {
            val exitoRemoto = repository.enviarCotizacionTecnico(
                token = tokenPrueba,
                ordenId = ordenId,
                diagnostico = diagnostico,
                montoTotal = monto,
                detallesServicios = detalles
            )
            if (!exitoRemoto) {
                // Respaldo local en Room
                val dao = AppDatabase.getDatabase(getApplication()).ordenDao()
                val actual = _ordenes.value.find { it.id == ordenId }
                if (actual != null) {
                    dao.insertarOrden(
                        OrdenEntity(
                            id = actual.id,
                            equipoId = actual.equipoId,
                            clienteId = actual.clienteId,
                            fechaIngreso = actual.fechaIngreso,
                            estado = "COTIZADO_PENDIENTE",
                            diagnostico = diagnostico
                        )
                    )
                }
            }
        }
    }

    // <-- FUNCIÓN AGREGADA AL FINAL DE LA CLASE
    fun cargarValorDolar() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitIndicadoresClient.apiService.obtenerIndicadores()
                _valorDolar.value = response.dolar.valor
            } catch (e: Exception) {
                // Si el dispositivo está offline, mantiene el valor de respaldo sin caer
                e.printStackTrace()
            }
        }
    }
}