package com.example.macmovilcare.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.ui.components.CampoTextoCustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargarEvidenciaScreen(
    ordenId: String,
    onGuardarEvidencia: (ordenId: String, uri: String, descripcion: String) -> Unit,
    onVolver: () -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para seleccionar imagen de la galería nativa
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjuntar Evidencia Nativa") },
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
                text = "Registro de Foto/Evidencia para la Orden #$ordenId",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón nativo para abrir galería o cámara
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imagenUri == null) "📷 Seleccionar Imagen/Evidencia" else "🔄 Cambiar Imagen")
            }

            imagenUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Imagen seleccionada: ${uri.lastPathSegment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CampoTextoCustom(
                valor = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción de la evidencia (ej: Falla en pantalla)",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    imagenUri?.let { uri ->
                        onGuardarEvidencia(ordenId, uri.toString(), descripcion)
                    }
                },
                enabled = imagenUri != null && descripcion.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Evidencia")
            }
        }
    }
}