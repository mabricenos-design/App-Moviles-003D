package com.example.macmovilcare.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.model.*
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

    private val _notificacionTecnico = MutableStateFlow<String?>(null)
    val notificacionTecnico: StateFlow<String?> = _notificacionTecnico.asStateFlow()

    init {
        crearCanalNotificaciones()
        cargarDatos()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones Técnico MCmovil"
            val descriptionText = "Avisos de solicitudes en taller"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CANAL_TECNICO_SOLICITUDES", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun dispararNotificacionNuevaSolicitud(equipoId: String, descripcion: String) {
        val mensaje = "Nueva solicitud para equipo #$equipoId: $descripcion"
        _notificacionTecnico.value = mensaje

        try {
            val context = getApplication<Application>()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, "CANAL_TECNICO_SOLICITUDES")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🔔 Nueva Solicitud en Taller")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun limpiarNotificacion() {
        _notificacionTecnico.value = null
    }

    private fun cargarDatos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = db.ordenDao()
                dao.obtenerTodasLasOrdenes().collect { entidades ->
                    if (entidades.isEmpty()) {
                        val datosPrueba = listOf(
                            OrdenEntity(
                                id = "101",
                                equipoId = "EQ-550",
                                clienteId = "CLI-01",
                                fechaIngreso = "10-10-2026",
                                estado = "COTIZADO_PENDIENTE",
                                diagnostico = "Falla en la tarjeta de video y suciedad interna.",
                                montoCotizacion = 45000.0,
                                detalleCotizacion = "Revisión técnica de circuitos y limpieza con ultrasonido."
                            ),
                            OrdenEntity(
                                id = "102",
                                equipoId = "EQ-720",
                                clienteId = "CLI-01",
                                fechaIngreso = "14-10-2026",
                                estado = "EN_REPARACION",
                                diagnostico = "Mantención preventiva y cambio de batería.",
                                montoCotizacion = 28000.0,
                                detalleCotizacion = "Cambio de batería original y pasta térmica."
                            )
                        )
                        dao.insertarOrdenes(datosPrueba)
                    } else {
                        _ordenes.value = entidades.map { entidad ->
                            val estadoEnum = runCatching { EstadoOrden.valueOf(entidad.estado) }.getOrDefault(EstadoOrden.INGRESADO)

                            OrdenTrabajo(
                                id = entidad.id,
                                equipoId = entidad.equipoId,
                                clienteId = entidad.clienteId,
                                fechaIngreso = entidad.fechaIngreso,
                                estado = estadoEnum,
                                diagnostico = entidad.diagnostico,
                                cotizacion = if (entidad.montoCotizacion != null && entidad.detalleCotizacion != null) {
                                    Cotizacion(
                                        id = "COT-${entidad.id}",
                                        montoTotal = entidad.montoCotizacion,
                                        detalleServicios = entidad.detalleCotizacion,
                                        requiereAutorizacion = true,
                                        estadoAutorizacion = if (estadoEnum == EstadoOrden.APROBADO) "APROBADO" else if (estadoEnum == EstadoOrden.RECHAZADO) "RECHAZADO" else "PENDIENTE"
                                    )
                                } else null,
                                garantia = Garantia(
                                    id = "GAR-${entidad.id}",
                                    fechaInicio = "10-10-2026",
                                    fechaVencimiento = "10-04-2027",
                                    coberturaDetalle = "Servicio técnico y componentes reemplazados",
                                    estaActiva = true
                                ),
                                proximaMantencion = Mantencion(
                                    id = "MAN-${entidad.id}",
                                    equipoId = entidad.equipoId,
                                    fechaSugerida = "15-01-2027",
                                    tipoMantencion = "Limpieza interna y cambio de pasta térmica",
                                    realizada = false
                                )
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

    // Guarda la cotización personalizada escrita por el técnico en Room
    fun responderSolicitudComoTecnico(
        ordenId: String,
        comentariosTecnico: String,
        monto: Double,
        detalleCotizacion: String,
        uriImagen: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detalleFinal = if (!uriImagen.isNullOrBlank()) {
                    "$comentariosTecnico [📷 Evidencia adjunta: ${uriImagen.substringAfterLast("/")}]"
                } else {
                    comentariosTecnico
                }

                db.ordenDao().responderTecnicoConCotizacion(
                    ordenId = ordenId,
                    nuevoEstado = "COTIZADO_PENDIENTE",
                    diagnostico = detalleFinal,
                    monto = monto,
                    detalle = detalleCotizacion
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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