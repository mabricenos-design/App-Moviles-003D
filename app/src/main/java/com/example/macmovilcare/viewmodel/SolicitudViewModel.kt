package com.example.macmovilcare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.AppDatabase
import com.example.macmovilcare.data.local.entity.OrdenEntity
import com.example.macmovilcare.model.SolicitudErrores
import com.example.macmovilcare.model.SolicitudUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val db by lazy { AppDatabase.getDatabase(getApplication()) }

    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    fun onEquipoIdChange(nuevoEquipoId: String) {
        _uiState.update {
            it.copy(
                equipoId = nuevoEquipoId,
                errores = it.errores.copy(equipoId = null)
            )
        }
    }

    fun onDescripcionChange(nuevaDescripcion: String) {
        _uiState.update {
            it.copy(
                descripcionProblema = nuevaDescripcion,
                errores = it.errores.copy(descripcionProblema = null)
            )
        }
    }

    fun enviarSolicitud(onExito: () -> Unit = {}) {
        val equipoId = _uiState.value.equipoId
        val descripcion = _uiState.value.descripcionProblema

        val errorEquipo = if (equipoId.isBlank()) "Debe ingresar el ID o N° de serie del equipo" else null
        val errorDescripcion = if (descripcion.isBlank()) "Debe detallar el problema reportado" else null

        if (errorEquipo != null || errorDescripcion != null) {
            _uiState.update {
                it.copy(errores = SolicitudErrores(equipoId = errorEquipo, descripcionProblema = errorDescripcion))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }

            val nuevoId = (103..999).random().toString()
            val fechaActual = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

            val nuevaOrden = OrdenEntity(
                id = nuevoId,
                equipoId = equipoId,
                clienteId = "CLI-01",
                fechaIngreso = fechaActual,
                estado = "INGRESADO",
                diagnostico = descripcion
            )

            try {
                withContext(Dispatchers.IO) {
                    db.ordenDao().insertarOrden(nuevaOrden)
                }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        equipoId = "",
                        descripcionProblema = ""
                    )
                }
                onExito()
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false) }
                e.printStackTrace()
            }
        }
    }
}