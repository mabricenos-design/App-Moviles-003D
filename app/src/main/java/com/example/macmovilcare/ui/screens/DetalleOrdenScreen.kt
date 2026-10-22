package com.example.macmovilcare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.CampoTextoCustom
import com.example.macmovilcare.ui.components.EstadoBadge
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    orden: OrdenTrabajo?,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    onAprobarCotizacion: () -> Unit,
    onRechazarCotizacion: () -> Unit,
    onResponderComoTecnico: (ordenId: String, comentarios: String, monto: Double, detalleCotizacion: String, uriFoto: String?) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var respuestaTecnica by remember { mutableStateOf("") }
    var montoTexto by remember { mutableStateOf("") }
    var detalleCotizacionTexto by remember { mutableStateOf("") }

    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var mensajeExitoTecnico by remember { mutableStateOf<String?>(null) }

    val formatoMoneda = remember { NumberFormat.getIntegerInstance(Locale("es", "CL")) }

    // 1. Lanzador de la Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
            fotoUri = null
        }
    }

    // 2. Solicitud interactiva del permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                errorGeneral = "No se pudo iniciar la cámara en este equipo."
            }
        } else {
            errorGeneral = "Debes autorizar el permiso de la cámara para tomar fotos."
        }
    }

    // 3. Lanzador de la Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
            fotoBitmap = null
        }
    }

    // Función segura para pedir permiso y abrir la cámara
    fun abrirCamaraConPermiso() {
        val permiso = Manifest.permission.CAMERA
        val tienePermiso = ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                errorGeneral = "No se pudo abrir la cámara."
            }
        } else {
            permissionLauncher.launch(permiso)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la Orden") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (orden == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando información...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Orden #${orden.id}", style = MaterialTheme.typography.headlineSmall)
                    EstadoBadge(estado = orden.estado)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Diagnóstico y Comentarios Técnicos", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = orden.diagnostico ?: "Pendiente de revisión técnica.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Panel para que el Técnico elabore la cotización y tome fotos
                if (rolUsuario == RolUsuario.TECNICO && (orden.estado == EstadoOrden.INGRESADO || orden.estado == EstadoOrden.EN_DIAGNOSTICO)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🛠️ Elaborar Cotización y Diagnóstico", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Ingresa el presupuesto y la evidencia fotográfica:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CampoTextoCustom(
                                valor = respuestaTecnica,
                                onValueChange = {
                                    respuestaTecnica = it
                                    errorGeneral = null
                                },
                                label = "Diagnóstico técnico de la falla",
                                singleLine = false,
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = montoTexto,
                                onValueChange = {
                                    montoTexto = it
                                    errorGeneral = null
                                },
                                label = { Text("Monto de la Cotización ($ CLP)") },
                                placeholder = { Text("Ej: 89000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CampoTextoCustom(
                                valor = detalleCotizacionTexto,
                                onValueChange = {
                                    detalleCotizacionTexto = it
                                    errorGeneral = null
                                },
                                label = "Detalle de repuestos y servicios",
                                singleLine = false,
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Adjuntar Foto de Evidencia:", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { abrirCamaraConPermiso() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📷 Cámara")
                                }

                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🖼️ Galería")
                                }
                            }

                            fotoBitmap?.let { bmp ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Foto capturada:", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
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

                            fotoUri?.let { uri ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Foto de galería: ${uri.lastPathSegment}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            errorGeneral?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val montoLimpio = montoTexto
                                        .replace(".", "")
                                        .replace(",", "")
                                        .replace("$", "")
                                        .trim()
                                    val montoNum = montoLimpio.toDoubleOrNull()

                                    if (respuestaTecnica.isBlank() || detalleCotizacionTexto.isBlank() || montoNum == null || montoNum <= 0) {
                                        errorGeneral = "Debe completar el diagnóstico, el detalle y un monto válido."
                                    } else {
                                        val fotoReferencia = when {
                                            fotoBitmap != null -> "camara_captura_${System.currentTimeMillis()}.jpg"
                                            fotoUri != null -> fotoUri.toString()
                                            else -> null
                                        }

                                        onResponderComoTecnico(
                                            orden.id,
                                            respuestaTecnica,
                                            montoNum,
                                            detalleCotizacionTexto,
                                            fotoReferencia
                                        )
                                        mensajeExitoTecnico = "¡Cotización enviada al cliente con éxito!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar Cotización al Cliente")
                            }

                            mensajeExitoTecnico?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tarjeta de Cotización
                orden.cotizacion?.let { cotizacion ->
                    val montoFormateado = formatoMoneda.format(cotizacion.montoTotal)

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cotización de Reparación", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Monto Total: $$montoFormateado CLP")
                            Text("Detalle: ${cotizacion.detalleServicios}")

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Estado Autorización: ${cotizacion.estadoAutorizacion}")

                            if (rolUsuario == RolUsuario.CLIENTE && cotizacion.estadoAutorizacion == "PENDIENTE") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onAprobarCotizacion) { Text("Aprobar Cotización") }
                                    OutlinedButton(onClick = onRechazarCotizacion) { Text("Rechazar") }
                                }
                            } else if (rolUsuario != RolUsuario.CLIENTE) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ℹ️ Vista Informativa (Solo el Cliente puede autorizar cotizaciones)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tarjeta de Garantía
                orden.garantia?.let { garantia ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🛡️ Garantía del Servicio", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cobertura: ${garantia.coberturaDetalle}")
                            Text("Vigencia: ${garantia.fechaInicio} al ${garantia.fechaVencimiento}")
                            Text(
                                text = if (garantia.estaActiva) "Estado: ACTIVA" else "Estado: VENCIDA",
                                color = if (garantia.estaActiva) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tarjeta de Mantención
                orden.proximaMantencion?.let { mantencion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔧 Próxima Mantención Sugerida", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tipo: ${mantencion.tipoMantencion}")
                            Text("Fecha Sugerida: ${mantencion.fechaSugerida}")
                        }
                    }
                }
            }
        }
    }
}