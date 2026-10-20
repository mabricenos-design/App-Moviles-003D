package com.example.macmovilcare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.model.LoginErrores
import com.example.macmovilcare.model.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(nuevoCorreo: String) {
        _uiState.update { estado ->
            estado.copy(
                correo = nuevoCorreo,
                errores = estado.errores.copy(correo = null)
            )
        }
    }

    fun onClaveChange(nuevaClave: String) {
        _uiState.update { estado ->
            estado.copy(
                clave = nuevaClave,
                errores = estado.errores.copy(clave = null)
            )
        }
    }

    fun iniciarSesion(onSuccess: () -> Unit) {
        val correo = _uiState.value.correo
        val clave = _uiState.value.clave

        val errorCorreo = if (correo.isBlank()) "El correo no puede estar vacío" else if (!correo.contains("@")) "Formato de correo inválido" else null
        val errorClave = if (clave.isBlank()) "La contraseña no puede estar vacía" else null

        if (errorCorreo != null || errorClave != null) {
            _uiState.update {
                it.copy(errores = LoginErrores(correo = errorCorreo, clave = errorClave))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            delay(1500) // Simulación de petición asíncrona a API REST
            _uiState.update { it.copy(cargando = false) }
            onSuccess()
        }
    }
}