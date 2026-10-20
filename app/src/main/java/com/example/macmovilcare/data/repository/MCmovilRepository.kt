package com.example.macmovilcare.data.repository

import com.example.macmovilcare.data.local.dao.OrdenDao
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class MCmovilRepository(
    private val ordenDao: OrdenDao,
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
                    estado = dto.estado,
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
}