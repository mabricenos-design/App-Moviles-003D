package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.ui.components.EstadoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    orden: OrdenTrabajo?,
    onAprobarCotizacion: () -> Unit,
    onRechazarCotizacion: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de la Orden") }) }
    ) { innerPadding ->
        if (orden == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text("Cargando información...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Orden #${orden.id}", style = MaterialTheme.typography.headlineSmall)
                    EstadoBadge(estado = orden.estado)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Diagnóstico", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(orden.diagnostico ?: "Pendiente de revisión técnica.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                orden.cotizacion?.let { cotizacion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cotización", style = MaterialTheme.typography.titleMedium)
                            Text("Monto: $${cotizacion.montoTotal}")
                            Text("Detalle: ${cotizacion.detalleServicios}")

                            if (cotizacion.estadoAutorizacion == "PENDIENTE") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onAprobarCotizacion) { Text("Aprobar") }
                                    OutlinedButton(onClick = onRechazarCotizacion) { Text("Rechazar") }
                                }
                            } else {
                                Text("Estado: ${cotizacion.estadoAutorizacion}")
                            }
                        }
                    }
                }
            }
        }
    }
}