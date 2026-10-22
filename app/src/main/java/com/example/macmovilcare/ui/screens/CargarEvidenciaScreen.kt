package com.example.macmovilcare.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    var imagenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Captura Real con Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imagenBitmap = bitmap
            imagenUri = null
        }
    }

    // 2. Selección de Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
            imagenBitmap = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjuntar Evidencia Nativa") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
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

            // Botones nativos duales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📷 Tomar Foto")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🖼️ Galería")
                }
            }

            // Vista previa de la foto
            imagenBitmap?.let { bmp ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Foto capturada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            imagenUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Foto de galería: ${uri.lastPathSegment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CampoTextoCustom(
                valor = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción de la evidencia técnica",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val identificadorFoto = when {
                        imagenBitmap != null -> "camara_${System.currentTimeMillis()}.jpg"
                        imagenUri != null -> imagenUri.toString()
                        else -> "sin_foto"
                    }
                    onGuardarEvidencia(ordenId, identificadorFoto, descripcion)
                },
                enabled = (imagenBitmap != null || imagenUri != null) && descripcion.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Evidencia")
            }
        }
    }
}