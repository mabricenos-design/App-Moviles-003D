package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.EstadoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    orden: OrdenTrabajo?,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    onAprobarCotizacion: () -> Unit,
    onRechazarCotizacion: () -> Unit,
    onAdjuntarEvidencia: (String) -> Unit = {},
    onVolver: () -> Unit
) {
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
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Cargando información...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // 1. Cabecera con número de orden y badge de estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Orden #${orden.id}", style = MaterialTheme.typography.headlineSmall)
                    EstadoBadge(estado = orden.estado)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Tarjeta de Diagnóstico
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Diagnóstico Técnico", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(orden.diagnostico ?: "Pendiente de revisión técnica.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Tarjeta de Cotización
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

                // 4. Tarjeta de Garantía
                orden.garantia?.let { garantia ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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

                // 5. Tarjeta de Próxima Mantención
                orden.proximaMantencion?.let { mantencion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔧 Próxima Mantención Sugerida", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tipo: ${mantencion.tipoMantencion}")
                            Text("Fecha Sugerida: ${mantencion.fechaSugerida}")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6. Botón de Evidencias (Solo para Técnico o Administrador)
                if (rolUsuario == RolUsuario.TECNICO || rolUsuario == RolUsuario.ADMINISTRADOR) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onAdjuntarEvidencia(orden.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷 Adjuntar Evidencia Técnica (Galería Nativa)")
                    }
                }
            }
        }
    }
}