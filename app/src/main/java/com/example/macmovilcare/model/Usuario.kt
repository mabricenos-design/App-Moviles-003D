package com.example.macmovilcare.model

enum class RolUsuario {
    CLIENTE,
    TECNICO,
    ADMINISTRADOR
}

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario,
    val empresa: String? = null
)