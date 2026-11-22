package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.model.LoginUiState
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.theme.AzulVariante
import com.example.macmovilcare.ui.theme.GrisBorde
import com.example.macmovilcare.ui.theme.GrisFondo
import com.example.macmovilcare.ui.theme.GrisSuperficie
import com.example.macmovilcare.ui.theme.TextoOscuro
import com.example.macmovilcare.ui.theme.TextoSecundario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onLoginExitoso: (rolDetectado: RolUsuario, nombreUsuario: String) -> Unit
) {
    var claveVisible by remember { mutableStateOf(false) }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    fun validarYEntrar() {
        val correoLimpio = state.correo.trim().lowercase()
        val claveLimpia = state.clave.trim()

        if (!correoLimpio.contains("@") || correoLimpio.substringBefore("@").isBlank()) {
            errorValidacion = "Ingresa un nombre válido antes del @"
            return
        }

        val nombreBruto = correoLimpio.substringBefore("@")
        val nombreCapitalizado = nombreBruto.replaceFirstChar { it.uppercase() }

        when {
            correoLimpio.endsWith("@cliente.com") -> {
                if (claveLimpia == "cliente123") {
                    errorValidacion = null
                    onLoginExitoso(RolUsuario.CLIENTE, nombreCapitalizado)
                } else {
                    errorValidacion = "Contraseña incorrecta para perfil Cliente"
                }
            }
            correoLimpio.endsWith("@tecnico.com") -> {
                if (claveLimpia == "tecnico123") {
                    errorValidacion = null
                    onLoginExitoso(RolUsuario.TECNICO, nombreCapitalizado)
                } else {
                    errorValidacion = "Contraseña incorrecta para perfil Técnico"
                }
            }
            correoLimpio.endsWith("@admin.com") -> {
                if (claveLimpia == "admin123") {
                    errorValidacion = null
                    onLoginExitoso(RolUsuario.ADMINISTRADOR, nombreCapitalizado)
                } else {
                    errorValidacion = "Contraseña incorrecta para perfil Admin"
                }
            }
            else -> {
                errorValidacion = "El correo debe terminar en @cliente.com, @tecnico.com o @admin.com"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF112A2E), Color(0xFF1F5D5B))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF193C3F),
                border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = "Logo MCmovil Care",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MCmovil Care",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Soporte Técnico Especializado",
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Text(
                        text = "Ingresa tus credenciales corporativas o de cliente",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    OutlinedTextField(
                        value = state.correo,
                        onValueChange = {
                            errorValidacion = null
                            onCorreoChange(it)
                        },
                        label = { Text("Correo electrónico", fontSize = 13.sp) },
                        placeholder = {
                            Text(
                                "nombre@cliente.com",
                                fontSize = 13.sp,
                                color = TextoSecundario.copy(alpha = 0.6f)
                            )
                        },
                        textStyle = TextStyle(fontSize = 14.sp, color = TextoOscuro),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = AzulVariante,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulVariante,
                            unfocusedBorderColor = GrisBorde,
                            focusedLabelColor = AzulVariante,
                            unfocusedLabelColor = TextoSecundario,
                            focusedContainerColor = Color(0xFF151F22),
                            unfocusedContainerColor = Color(0xFF151F22)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = state.clave,
                        onValueChange = {
                            errorValidacion = null
                            onClaveChange(it)
                        },
                        label = { Text("Contraseña", fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 14.sp, color = TextoOscuro),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = AzulVariante,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { claveVisible = !claveVisible }) {
                                Icon(
                                    imageVector = if (claveVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = AzulVariante,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulVariante,
                            unfocusedBorderColor = GrisBorde,
                            focusedLabelColor = AzulVariante,
                            unfocusedLabelColor = TextoSecundario,
                            focusedContainerColor = Color(0xFF151F22),
                            unfocusedContainerColor = Color(0xFF151F22)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorValidacion?.let { mensajeError ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensajeError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { validarYEntrar() },
                        enabled = !state.cargando,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2FA39E), Color(0xFF176664))
                                )
                            )
                    ) {
                        if (state.cargando) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Ingresar al Sistema",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}