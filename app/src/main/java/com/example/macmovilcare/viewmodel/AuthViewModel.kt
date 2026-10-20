package com.example.macmovilcare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macmovilcare.data.local.UserPreferences
import com.example.macmovilcare.model.LoginErrores
import com.example.macmovilcare.model.LoginUiState
import com.example.macmovilcare.model.RolUsuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userPreferences: UserPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Estado del Rol del Usuario Activo (Por defecto CLIENTE para pruebas)
    private val _rolActivo = MutableStateFlow(RolUsuario.CLIENTE)
    val rolActivo: StateFlow<RolUsuario> = _rolActivo.asStateFlow()

    fun onCorreoChange(nuevoCorreo: String) {
        _uiState.update { estado ->
            estado.copy(correo = nuevoCorreo, errores = estado.errores.copy(correo = null))
        }
    }

    fun onClaveChange(nuevaClave: String) {
        _uiState.update { estado ->
            estado.copy(clave = nuevaClave, errores = estado.errores.copy(clave = null))
        }
    }

    // Permite seleccionar el rol de prueba manualmente desde la pantalla de Login
    fun seleccionarRolPrueba(rol: RolUsuario) {
        _rolActivo.value = rol
    }

    fun iniciarSesion(onSuccess: () -> Unit) {
        val correo = _uiState.value.correo
        val clave = _uiState.value.clave

        val errorCorreo = if (correo.isBlank()) "El correo no puede estar vacío" else null
        val errorClave = if (clave.isBlank()) "La contraseña no puede estar vacía" else null

        if (errorCorreo != null || errorClave != null) {
            _uiState.update { it.copy(errores = LoginErrores(correo = errorCorreo, clave = errorClave)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            delay(1000) // Simulación de autenticación API

            // Guardado en DataStore (DSY1105)
            userPreferences?.guardarSesion("USER-101", _rolActivo.value.name)

            _uiState.update { it.copy(cargando = false) }
            onSuccess()
        }
    }
}