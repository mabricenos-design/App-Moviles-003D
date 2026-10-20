package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.SolicitudUiState
import com.example.macmovilcare.ui.components.CampoTextoCustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(
    state: SolicitudUiState,
    onEquipoIdChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onEnviarSolicitud: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud de Atención") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("< Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ingrese los datos del equipo y la falla reportada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTextoCustom(
                valor = state.equipoId,
                onValueChange = onEquipoIdChange,
                label = "ID o N° de Serie del Equipo",
                error = state.errores.equipoId
            )

            Spacer(modifier = Modifier.height(12.dp))

            CampoTextoCustom(
                valor = state.descripcionProblema,
                onValueChange = onDescripcionChange,
                label = "Descripción del problema o requerimiento",
                error = state.errores.descripcionProblema,
                singleLine = false,
                minLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onEnviarSolicitud,
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Enviar Solicitud")
                }
            }

            state.mensajeExito?.let { mensaje ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}