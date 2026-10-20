package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.LoginUiState
import com.example.macmovilcare.model.RolUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginUiState,
    rolSeleccionado: RolUsuario,
    onRolSeleccionado: (RolUsuario) -> Unit,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("MCmovil Care - Acceso") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo electrónico") },
                isError = state.errores.correo != null,
                supportingText = {
                    state.errores.correo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.clave,
                onValueChange = onClaveChange,
                label = { Text("Contraseña") },
                isError = state.errores.clave != null,
                supportingText = {
                    state.errores.clave?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- INTEGRACIÓN DEL SELECTOR DE ROL DE PRUEBA ---
            Text(
                text = "Seleccione Perfil de Prueba:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                RolUsuario.values().forEach { rol ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (rolSeleccionado == rol),
                            onClick = { onRolSeleccionado(rol) }
                        )
                        Text(
                            text = rol.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            // --------------------------------------------------

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                } else {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}