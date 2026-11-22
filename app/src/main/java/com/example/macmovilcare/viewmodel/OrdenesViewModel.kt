package com.example.macmovilcare.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.data.remote.RetiroQrApiService
import com.example.macmovilcare.data.remote.RetrofitClient
import com.example.macmovilcare.data.remote.RetrofitIndicadoresClient
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

typealias MapaQrOrdenes = Map<String, String>

class OrdenesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MCmovilRepository by lazy {
        val dao = AppDatabase.getDatabase(getApplication()).ordenDao()
        MCmovilRepository(ordenDao = dao, apiService = RetrofitClient.apiService)
    }

    private val prefs = application.getSharedPreferences("mcmovil_cache", Context.MODE_PRIVATE)

    private val _ordenes = MutableStateFlow<List<OrdenTrabajo>>(emptyList())
    val ordenes: StateFlow<List<OrdenTrabajo>> = _ordenes.asStateFlow()

    private val _ordenSeleccionada = MutableStateFlow<OrdenTrabajo?>(null)
    val ordenSeleccionada: StateFlow<OrdenTrabajo?> = _ordenSeleccionada.asStateFlow()

    private val _notificacionTecnico = MutableStateFlow<String?>(null)
    val notificacionTecnico: StateFlow<String?> = _notificacionTecnico.asStateFlow()

    private val _valorDolar = MutableStateFlow<Double>(
        prefs.getFloat("ultimo_dolar", 950.0f).toDouble()
    )
    val valorDolar: StateFlow<Double> = _valorDolar.asStateFlow()

    private val _urlsQrPorOrden = MutableStateFlow<MapaQrOrdenes>(emptyMap())
    val urlsQrPorOrden: StateFlow<MapaQrOrdenes> = _urlsQrPorOrden.asStateFlow()

    private val _resultadoEscaneoQr = MutableStateFlow<String?>(null)
    val resultadoEscaneoQr: StateFlow<String?> = _resultadoEscaneoQr.asStateFlow()

    private val cotizacionesMap = mutableMapOf<String, Cotizacion>(
        "101" to Cotizacion(
            id = "COT-101",
            montoTotal = 45000.0,
            detalleServicios = "Limpieza profunda + cambio de componente electrónico",
            requiereAutorizacion = true,
            estadoAutorizacion = "PENDIENTE"
        )
    )

    private val evidenciasMap = mutableMapOf<String, List<Evidencia>>()

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            cargarValorDolar()
        }
    }

    init {
        sincronizarYObservarOrdenes()
        cargarValorDolar()
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                        val listaMapeada = entidades.map { entidad ->
                            val estadoOrden = runCatching { EstadoOrden.valueOf(entidad.estado) }
                                .getOrDefault(EstadoOrden.INGRESADO)

                            // Si la orden ya está aprobada, aseguramos su URL QR en línea inmediatamente
                            if (estadoOrden == EstadoOrden.APROBADO ||
                                estadoOrden == EstadoOrden.LISTO_PARA_ENTREGA ||
                                estadoOrden == EstadoOrden.ENTREGADO
                            ) {
                                if (!_urlsQrPorOrden.value.containsKey(entidad.id)) {
                                    val urlQr = RetiroQrApiService.construirUrlImagenQr(entidad.id)
                                    _urlsQrPorOrden.value = _urlsQrPorOrden.value + (entidad.id to urlQr)
                                }
                            }

                            OrdenTrabajo(
                                id = entidad.id,
                                equipoId = entidad.equipoId,
                                clienteId = entidad.clienteId,
                                fechaIngreso = entidad.fechaIngreso,
                                estado = estadoOrden,
                                diagnostico = entidad.diagnostico,
                                cotizacion = cotizacionesMap[entidad.id],
                                evidencias = evidenciasMap[entidad.id] ?: emptyList()
                            )
                        }
                        _ordenes.value = listaMapeada
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
        val encontrada = _ordenes.value.find { it.id == ordenId }
        _ordenSeleccionada.value = encontrada

        if (encontrada != null && (
                    encontrada.estado == EstadoOrden.APROBADO ||
                            encontrada.estado == EstadoOrden.LISTO_PARA_ENTREGA ||
                            encontrada.estado == EstadoOrden.ENTREGADO
                    )
        ) {
            val urlQr = RetiroQrApiService.construirUrlImagenQr(ordenId)
            _urlsQrPorOrden.value = _urlsQrPorOrden.value + (ordenId to urlQr)
        }

        sincronizarQrDesdeNube(ordenId)
    }

    fun sincronizarQrDesdeNube(ordenId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ticket = RetiroQrApiService.consultarTicketRemoto(ordenId)
            if (ticket != null && ticket.aprobadoPorCliente) {
                val urlQr = RetiroQrApiService.construirUrlImagenQr(ordenId, ticket.tokenRetiro)
                _urlsQrPorOrden.value = _urlsQrPorOrden.value + (ordenId to urlQr)
                if (ticket.retiradoConfirmado) {
                    repository.actualizarEstadoOrden(ordenId, EstadoOrden.ENTREGADO.name)
                }
            }
        }
    }

    fun responderCotizacion(ordenId: String, aprobada: Boolean) {
        val nuevoEstado = if (aprobada) "APROBADO" else "RECHAZADO"
        val tokenPrueba = "token-academico-simulacion"

        cotizacionesMap[ordenId]?.let { cot ->
            cotizacionesMap[ordenId] = cot.copy(estadoAutorizacion = nuevoEstado)
        }

        // Generar y mostrar el QR en pantalla de inmediato al aprobar
        if (aprobada) {
            val urlQrInmediata = RetiroQrApiService.construirUrlImagenQr(ordenId)
            _urlsQrPorOrden.value = _urlsQrPorOrden.value + (ordenId to urlQrInmediata)
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

            if (aprobada) {
                val ticket = RetiroQrApiService.registrarAprobacionYGenerarTicket(ordenId)
                val urlQr = RetiroQrApiService.construirUrlImagenQr(ordenId, ticket.tokenRetiro)
                _urlsQrPorOrden.value = _urlsQrPorOrden.value + (ordenId to urlQr)
            }
        }
    }

    fun procesarQrEscaneadoPorTecnico(qrContenido: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = RetiroQrApiService.validarYConfirmarRetiroPorQr(qrContenido)
            resultado.onSuccess { ordenIdConfirmada ->
                repository.actualizarEstadoOrden(ordenIdConfirmada, EstadoOrden.ENTREGADO.name)
                _resultadoEscaneoQr.value = "Retiro aprobado con éxito para Orden #$ordenIdConfirmada (Estado: ENTREGADO)."
            }.onFailure { error ->
                _resultadoEscaneoQr.value = "Error: ${error.message}"
            }
        }
    }

    fun limpiarResultadoEscaneoQr() {
        _resultadoEscaneoQr.value = null
    }

    fun eliminarOrden(ordenId: String) {
        val tokenPrueba = "token-academico-simulacion"
        cotizacionesMap.remove(ordenId)
        evidenciasMap.remove(ordenId)

        if (_ordenSeleccionada.value?.id == ordenId) {
            _ordenSeleccionada.value = null
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarOrden(token = tokenPrueba, ordenId = ordenId)
        }
    }

    fun cambiarEstadoOrdenAdmin(ordenId: String, nuevoEstado: EstadoOrden) {
        val tokenPrueba = "token-academico-simulacion"
        viewModelScope.launch(Dispatchers.IO) {
            val exitoRemoto = repository.responderCotizacionRemota(
                token = tokenPrueba,
                ordenId = ordenId,
                nuevoEstado = nuevoEstado.name
            )
            if (!exitoRemoto) {
                repository.actualizarEstadoOrden(ordenId, nuevoEstado.name)
            }
        }
    }

    fun dispararNotificacionNuevaSolicitud(equipoId: String, descripcion: String) {
        _notificacionTecnico.value = "Nueva solicitud recibida para equipo $equipoId: $descripcion"
    }

    fun limpiarNotificacion() {
        _notificacionTecnico.value = null
    }

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

    fun cargarValorDolar() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitIndicadoresClient.apiService.obtenerIndicadores()
                val nuevoValor = response.dolar.valor
                _valorDolar.value = nuevoValor
                prefs.edit().putFloat("ultimo_dolar", nuevoValor.toFloat()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}