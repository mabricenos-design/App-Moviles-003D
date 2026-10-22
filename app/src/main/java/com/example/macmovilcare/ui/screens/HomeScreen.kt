package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.TarjetaOrden

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ordenes: List<OrdenTrabajo>,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    esPantallaAncha: Boolean = false,
    mensajeNotificacion: String? = null,
    onDescartarNotificacion: () -> Unit = {},
    onOrdenClick: (String) -> Unit,
    onNuevaSolicitudClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    // 1. Filtrado de órdenes por rol
    val ordenesFiltradas = when (rolUsuario) {
        RolUsuario.CLIENTE -> ordenes
        RolUsuario.TECNICO -> ordenes.filter {
            it.estado == EstadoOrden.INGRESADO || it.estado == EstadoOrden.EN_DIAGNOSTICO || it.estado == EstadoOrden.EN_REPARACION || it.estado == EstadoOrden.COTIZADO_PENDIENTE
        }
        RolUsuario.ADMINISTRADOR -> ordenes
    }

    // Para el Cliente: separamos las pendientes respondidas por el técnico para ponerlas ARRIBA
    val pendientesCliente = ordenesFiltradas.filter { it.estado == EstadoOrden.COTIZADO_PENDIENTE }
    val otrasOrdenesCliente = ordenesFiltradas.filter { it.estado != EstadoOrden.COTIZADO_PENDIENTE }

    val tituloPantalla = when (rolUsuario) {
        RolUsuario.CLIENTE -> "Mis Equipos y Solicitudes"
        RolUsuario.TECNICO -> "Taller Técnico (Órdenes)"
        RolUsuario.ADMINISTRADOR -> "Gestión Global (Admin)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloPantalla) },
                navigationIcon = {
                    TextButton(onClick = onCerrarSesion) {
                        Text("← Salir", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (rolUsuario == RolUsuario.CLIENTE) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaSolicitudClick,
                    text = { Text("Nueva Solicitud") },
                    icon = { Text("+") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // BANNER DE NOTIFICACIÓN PARA EL TÉCNICO
            if (rolUsuario == RolUsuario.TECNICO && mensajeNotificacion != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔔 Nueva Solicitud Recibida", style = MaterialTheme.typography.titleSmall)
                            Text(mensajeNotificacion, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = onDescartarNotificacion) {
                            Text("OK")
                        }
                    }
                }
            }

            if (ordenesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay órdenes disponibles para este perfil.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // --- SECCIÓN SUPERIOR: PENDIENTES DE RESPUESTA PARA EL CLIENTE ---
                    if (rolUsuario == RolUsuario.CLIENTE && pendientesCliente.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚠️ Respuestas Técnicas Pendientes de Aprobación (${pendientesCliente.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "El técnico ha evaluado tu solicitud. Toca una orden para revisar sus comentarios y autorizar la cotización.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(pendientesCliente) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = "Equipo #${orden.equipoId} (¡Pendiente de tu respuesta!)",
                                onClick = { onOrdenClick(orden.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Historial y Otras Solicitudes",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(otrasOrdenesCliente) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = "Equipo #${orden.equipoId}",
                                onClick = { onOrdenClick(orden.id) }
                            )
                        }
                    } else {
                        // Lista estándar para Técnico, Admin o Cliente sin pendientes
                        items(ordenesFiltradas) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = if (orden.estado == EstadoOrden.INGRESADO && rolUsuario == RolUsuario.TECNICO) {
                                    "⚡ Nueva Solicitud Cliente - Equipo #${orden.equipoId}"
                                } else {
                                    "Equipo #${orden.equipoId}"
                                },
                                onClick = { onOrdenClick(orden.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}