package com.example.macmovilcare.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.CampoTextoCustom
import com.example.macmovilcare.ui.components.EstadoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    orden: OrdenTrabajo?,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    onAprobarCotizacion: () -> Unit,
    onRechazarCotizacion: () -> Unit,
    onResponderComoTecnico: (ordenId: String, comentarios: String, uriFoto: String?) -> Unit,
    onVolver: () -> Unit
) {
    var respuestaTecnica by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorRespuesta by remember { mutableStateOf<String?>(null) }
    var mensajeExitoTecnico by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
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
                // 1. Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Orden #${orden.id}", style = MaterialTheme.typography.headlineSmall)
                    EstadoBadge(estado = orden.estado)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Tarjeta de Diagnóstico / Comentarios del Técnico
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

                // 3. PANEL PARA QUE EL TÉCNICO RESPONDA LA SOLICITUD
                if (rolUsuario == RolUsuario.TECNICO && (orden.estado == EstadoOrden.INGRESADO || orden.estado == EstadoOrden.EN_DIAGNOSTICO)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🛠️ Responder Solicitud al Cliente", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Ingresa tu evaluación técnica y presupuesto para enviársela al cliente como Pendiente de aprobación:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CampoTextoCustom(
                                valor = respuestaTecnica,
                                onValueChange = {
                                    respuestaTecnica = it
                                    errorRespuesta = null
                                },
                                label = "Diagnóstico / Comentarios técnicos",
                                error = errorRespuesta,
                                singleLine = false,
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Adjuntar Imagen Opcional (Recurso Nativo)
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (fotoUri == null) "📷 Adjuntar Imagen (Opcional)" else "🔄 Cambiar Imagen")
                            }

                            fotoUri?.let { uri ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Imagen seleccionada: ${uri.lastPathSegment}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (respuestaTecnica.isBlank()) {
                                        errorRespuesta = "Debe escribir una respuesta o diagnóstico para el cliente"
                                    } else {
                                        onResponderComoTecnico(orden.id, respuestaTecnica, fotoUri?.toString())
                                        mensajeExitoTecnico = "¡Respuesta técnica enviada al cliente con éxito!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar Evaluación Técnica al Cliente")
                            }

                            mensajeExitoTecnico?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Tarjeta de Cotización y Aprobación del Cliente
                orden.cotizacion?.let { cotizacion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cotización de Reparación", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Monto Total: $${cotizacion.montoTotal}")
                            Text("Detalle: ${cotizacion.detalleServicios}")

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Estado Autorización: ${cotizacion.estadoAutorizacion}")

                            // SOLO EL CLIENTE PUEDE APROBAR O RECHAZAR
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

                // 5. Garantía
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

                // 6. Próxima Mantención
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