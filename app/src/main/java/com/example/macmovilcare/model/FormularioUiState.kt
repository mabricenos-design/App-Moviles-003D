package com.example.macmovilcare.model

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val errores: LoginErrores = LoginErrores()
)

data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
)