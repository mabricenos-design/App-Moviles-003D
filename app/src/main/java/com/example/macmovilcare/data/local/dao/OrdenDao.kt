package com.example.macmovilcare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.macmovilcare.data.local.entity.OrdenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenDao {
    @Query("SELECT * FROM ordenes_trabajo")
    fun obtenerTodasLasOrdenes(): Flow<List<OrdenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOrdenes(ordenes: List<OrdenEntity>)

    @Query("UPDATE ordenes_trabajo SET estado = :nuevoEstado WHERE id = :ordenId")
    suspend fun actualizarEstado(ordenId: String, nuevoEstado: String)
}