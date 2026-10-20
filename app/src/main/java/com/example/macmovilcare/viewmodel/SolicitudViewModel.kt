package com.example.macmovilcare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.model.SolicitudErrores
import com.example.macmovilcare.model.SolicitudUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudViewModel : ViewModel() {

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

    fun enviarSolicitud() {
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
            delay(1200) // Simulación de persistencia / consumo de API
            _uiState.update {
                it.copy(
                    cargando = false,
                    mensajeExito = "¡Solicitud registrada con éxito! Código de seguimiento asignado.",
                    equipoId = "",
                    descripcionProblema = ""
                )
            }
        }
    }
}