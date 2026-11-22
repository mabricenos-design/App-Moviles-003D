package com.example.macmovilcare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.CampoTextoCustom
import com.example.macmovilcare.ui.components.EstadoBadge
import com.example.macmovilcare.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    orden: OrdenTrabajo?,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    valorDolar: Double = 950.0,
    urlQrRetiro: String? = null,
    onAprobarCotizacion: () -> Unit,
    onRechazarCotizacion: () -> Unit,
    onResponderComoTecnico: (ordenId: String, comentarios: String, monto: Double, detalleCotizacion: String, uriFoto: String?) -> Unit,
    onCambiarEstadoAdmin: (ordenId: String, nuevoEstado: EstadoOrden) -> Unit = { _, _ -> },
    onEliminarOrdenAdmin: (ordenId: String) -> Unit = {},
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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
            fotoUri = null
        }
    }

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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
            fotoBitmap = null
        }
    }

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
        containerColor = GrisFondo,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF122326)),
                title = {
                    Text(
                        text = if (orden != null) "Orden #${orden.id}" else "Detalle de la Orden",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                CircularProgressIndicator(color = AzulVariante)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta principal de la Orden
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrisBorde),
                    colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Equipo #${orden.equipoId}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro
                                )
                                Text(
                                    text = "Ingreso: ${orden.fechaIngreso}",
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )
                            }
                            EstadoBadge(estado = orden.estado)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = GrisBorde.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Diagnóstico y Comentarios",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoSecundario
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = orden.diagnostico ?: "Pendiente de revisión técnica.",
                            fontSize = 14.sp,
                            color = TextoOscuro
                        )
                    }
                }

                // TARJETA QR EN TIEMPO REAL PARA EL CLIENTE (Tras aprobar la solicitud/cotización)
                if (urlQrRetiro != null && (orden.estado == EstadoOrden.APROBADO || orden.estado == EstadoOrden.LISTO_PARA_ENTREGA || orden.estado == EstadoOrden.ENTREGADO)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, AzulVariante),
                        colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCode2,
                                    contentDescription = null,
                                    tint = Color(0xFF7CE8E2),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pase Digital QR de Retiro",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7CE8E2)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (orden.estado == EstadoOrden.ENTREGADO) {
                                    "Equipo retirado y validado exitosamente por el técnico."
                                } else {
                                    "Presenta este código QR en el taller para que el técnico autorice el retiro de tu equipo."
                                },
                                fontSize = 12.sp,
                                color = TextoSecundario,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.size(200.dp)
                            ) {
                                AsyncImage(
                                    model = urlQrRetiro,
                                    contentDescription = "Código QR de Retiro Orden #${orden.id}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "ORDEN #${orden.id} · SINCRONIZADO EN NUBE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulVariante
                            )
                        }
                    }
                }

                // Vista Técnico: Elaborar Cotización
                if (rolUsuario == RolUsuario.TECNICO && (orden.estado == EstadoOrden.INGRESADO || orden.estado == EstadoOrden.EN_DIAGNOSTICO)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.55f)),
                        colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Build, contentDescription = null, tint = AzulVariante, modifier = Modifier.size(19.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Elaborar Cotización y Diagnóstico",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro
                                )
                            }
                            Text(
                                "Completa el informe técnico, presupuesto y evidencia fotográfica.",
                                fontSize = 12.sp,
                                color = TextoSecundario
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

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = montoTexto,
                                onValueChange = {
                                    montoTexto = it
                                    errorGeneral = null
                                },
                                label = { Text("Monto de la Cotización ($ CLP)", fontSize = 13.sp) },
                                textStyle = TextStyle(fontSize = 14.sp, color = TextoOscuro),
                                placeholder = { Text("Ej: 89000", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null, tint = AzulVariante, modifier = Modifier.size(20.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                            val montoNumPreview = montoTexto.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0
                            val enDolaresPreview = if (valorDolar > 0) montoNumPreview / valorDolar else 0.0
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = AzulContenedor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Dólar hoy: $${valorDolar.toInt()} CLP  ·  Equivalente: US$ ${String.format(Locale.US, "%.2f", enDolaresPreview)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF7CE8E2),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

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

                            Text(
                                "Evidencia Fotográfica",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoSecundario
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { abrirCamaraConPermiso() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF151F22)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextoOscuro)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cámara", fontSize = 13.sp, color = TextoOscuro)
                                }

                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF151F22)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextoOscuro)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Galería", fontSize = 13.sp, color = TextoOscuro)
                                }
                            }

                            fotoBitmap?.let { bmp ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Foto capturada",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, GrisBorde, RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            fotoUri?.let { uri ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Archivo adjunto: ${uri.lastPathSegment}",
                                    fontSize = 12.sp,
                                    color = AzulVariante
                                )
                            }

                            errorGeneral?.let {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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
                                        errorGeneral = "Completa el diagnóstico, detalle y un monto válido."
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
                                        mensajeExitoTecnico = "Cotización enviada al cliente."
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF2FA39E), Color(0xFF176664))
                                        )
                                    )
                            ) {
                                Text("Enviar Cotización al Cliente", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }

                            mensajeExitoTecnico?.let {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(it, color = Color(0xFF7CE8E2), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Cotización y botones Aprobar / Rechazar del Cliente
                orden.cotizacion?.let { cotizacion ->
                    val montoFormateado = formatoMoneda.format(cotizacion.montoTotal)
                    val montoEnUsd = if (valorDolar > 0) cotizacion.montoTotal / valorDolar else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = AzulVariante, modifier = Modifier.size(19.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Presupuesto de Reparación",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "$$montoFormateado CLP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7CE8E2)
                            )
                            Text(
                                text = "Referencia USD: US$ ${String.format(Locale.US, "%.2f", montoEnUsd)} (Dólar: $${valorDolar.toInt()} CLP)",
                                fontSize = 12.sp,
                                color = TextoSecundario
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cotizacion.detalleServicios,
                                fontSize = 14.sp,
                                color = TextoOscuro
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Autorización: ${cotizacion.estadoAutorizacion}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoSecundario
                            )

                            if (rolUsuario == RolUsuario.CLIENTE && cotizacion.estadoAutorizacion == "PENDIENTE") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onAprobarCotizacion,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AzulVariante),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Aprobar y Generar QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = onRechazarCotizacion,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GrisBorde),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text("Rechazar", fontSize = 13.sp, color = TextoOscuro)
                                    }
                                }
                            } else if (rolUsuario != RolUsuario.CLIENTE) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Solo el cliente puede autorizar o rechazar este presupuesto.",
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )
                            }
                        }
                    }
                }

                if (rolUsuario == RolUsuario.ADMINISTRADOR) {
                    var menuEstadosExpandido by remember { mutableStateOf(false) }
                    var confirmarBorradoDetalle by remember { mutableStateOf(false) }

                    if (confirmarBorradoDetalle) {
                        AlertDialog(
                            onDismissRequest = { confirmarBorradoDetalle = false },
                            containerColor = GrisSuperficie,
                            shape = RoundedCornerShape(18.dp),
                            title = { Text("Eliminar Orden #${orden.id}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoOscuro) },
                            text = { Text("Esta acción eliminará la orden y su historial de estados. ¿Continuar?", fontSize = 13.sp, color = TextoSecundario) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        confirmarBorradoDetalle = false
                                        onEliminarOrdenAdmin(orden.id)
                                        onVolver()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Sí, eliminar", fontSize = 13.sp)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmarBorradoDetalle = false }) {
                                    Text("Cancelar", fontSize = 13.sp, color = TextoSecundario)
                                }
                            }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = AzulVariante, modifier = Modifier.size(19.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Control de Administrador",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoOscuro
                                )
                            }
                            Text(
                                text = "Gestiona manualmente el estado de la orden o elimínala del registro.",
                                fontSize = 12.sp,
                                color = TextoSecundario
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { menuEstadosExpandido = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                ) {
                                    Text(
                                        "Estado actual: ${orden.estado.name.replace("_", " ")}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF7CE8E2)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuEstadosExpandido,
                                    onDismissRequest = { menuEstadosExpandido = false }
                                ) {
                                    EstadoOrden.values().forEach { estadoOpcion ->
                                        DropdownMenuItem(
                                            text = { Text(estadoOpcion.name.replace("_", " "), fontSize = 13.sp) },
                                            onClick = {
                                                menuEstadosExpandido = false
                                                onCambiarEstadoAdmin(orden.id, estadoOpcion)
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { confirmarBorradoDetalle = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Eliminar Orden y Estado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}