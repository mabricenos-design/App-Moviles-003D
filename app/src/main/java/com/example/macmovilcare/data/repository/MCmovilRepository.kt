package com.example.macmovilcare.data.repository

import com.example.macmovilcare.data.local.dao.OrdenesDao
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.data.remote.ApiService
import com.example.macmovilcare.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class MCmovilRepository(
    private val ordenDao: OrdenesDao,
    private val apiService: ApiService
) {

    // Obtiene las órdenes desde la BD local (fuente única de verdad)
    val todasLasOrdenes: Flow<List<OrdenEntity>> = ordenDao.obtenerTodasLasOrdenes()

    // Sincroniza datos remotos de Retrofit a Room local
    suspend fun sincronizarOrdenes() {
        try {
            val ordenesRemotas = apiService.obtenerOrdenes()
            val entidades = ordenesRemotas.map { dto ->
                OrdenEntity(
                    id = dto.id,
                    equipoId = dto.equipoId,
                    clienteId = dto.clienteId,
                    fechaIngreso = dto.fechaIngreso,
                    estado = dto.estado.uppercase(),
                    diagnostico = dto.diagnostico
                )
            }
            ordenDao.insertarOrdenes(entidades)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun actualizarEstadoOrden(ordenId: String, nuevoEstado: String) {
        ordenDao.actualizarEstado(ordenId, nuevoEstado)
    }
    // --- NUEVAS FUNCIONES PARA CONSUMIR LA API REST ---

    suspend fun login(correo: String, clave: String): Response<LoginResponse> {
        val request = LoginRequest(correo = correo, clave = clave)
        return apiService.login(request)
    }

    suspend fun enviarNuevaSolicitud(
        token: String,
        equipoId: String,
        descripcionProblema: String
    ): Boolean {
        return try {
            val fechaActual = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())

            val request = NuevaSolicitudRequest(
                equipoId = equipoId,
                clienteId = "CLI-01",
                fechaIngreso = fechaActual,
                estado = "INGRESADO",
                diagnostico = descripcionProblema
            )
            val response = apiService.crearSolicitud("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                ordenDao.insertarOrden(
                    OrdenEntity(
                        id = dto.id,
                        equipoId = dto.equipoId,
                        clienteId = dto.clienteId,
                        fechaIngreso = dto.fechaIngreso,
                        estado = dto.estado.uppercase(),
                        diagnostico = dto.diagnostico
                    )
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun responderCotizacionRemota(
        token: String,
        ordenId: String,
        nuevoEstado: String
    ): Boolean {
        return try {
            val request = ActualizarEstadoRequest(nuevoEstado = nuevoEstado)
            val response = apiService.actualizarEstadoOrden("Bearer $token", ordenId, request)

            if (response.isSuccessful) {
                // Actualizamos el estado localmente en Room
                ordenDao.actualizarEstado(ordenId, nuevoEstado)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun enviarCotizacionTecnico(
        token: String,
        ordenId: String,
        diagnostico: String,
        montoTotal: Double,
        detallesServicios: String
    ): Boolean {
        return try {
            val request = NuevaCotizacionRequest(
                diagnostico = diagnostico,
                montoTotal = montoTotal,
                detallesServicios = detallesServicios
            )
            val response = apiService.generarCotizacion("Bearer $token", ordenId, request)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                ordenDao.insertarOrden(
                    OrdenEntity(
                        id = dto.id,
                        equipoId = dto.equipoId,
                        clienteId = dto.clienteId,
                        fechaIngreso = dto.fechaIngreso,
                        estado = dto.estado,
                        diagnostico = dto.diagnostico
                    )
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

