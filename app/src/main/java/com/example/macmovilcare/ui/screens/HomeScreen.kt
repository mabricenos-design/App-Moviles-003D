package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onEliminarOrden: (String) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    var ordenAEliminar by remember { mutableStateOf<OrdenTrabajo?>(null) }
    var filtroEstadoAdmin by remember { mutableStateOf<EstadoOrden?>(null) }

    // Diálogo de confirmación antes de eliminar orden/estado
    ordenAEliminar?.let { orden ->
        AlertDialog(
            onDismissRequest = { ordenAEliminar = null },
            title = { Text("Eliminar Orden #${orden.id}") },
            text = {
                Text("¿Estás seguro de que deseas eliminar permanentemente esta orden y su estado actual (${orden.estado.name})?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarOrden(orden.id)
                        ordenAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ordenAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 1. Filtrado de órdenes por rol
    val ordenesFiltradas = when (rolUsuario) {
        RolUsuario.CLIENTE -> ordenes
        RolUsuario.TECNICO -> ordenes.filter {
            it.estado == EstadoOrden.INGRESADO ||
                    it.estado == EstadoOrden.EN_DIAGNOSTICO ||
                    it.estado == EstadoOrden.EN_REPARACION ||
                    it.estado == EstadoOrden.COTIZADO_PENDIENTE
        }
        RolUsuario.ADMINISTRADOR -> {
            if (filtroEstadoAdmin == null) ordenes else ordenes.filter { it.estado == filtroEstadoAdmin }
        }
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
            // PANEL DE RESUMEN Y FILTROS EXCLUSIVO ADMINISTRADOR
            if (rolUsuario == RolUsuario.ADMINISTRADOR) {
                val total = ordenes.size
                val pendientes = ordenes.count { it.estado == EstadoOrden.INGRESADO || it.estado == EstadoOrden.COTIZADO_PENDIENTE }
                val enTaller = ordenes.count { it.estado == EstadoOrden.APROBADO || it.estado == EstadoOrden.EN_REPARACION }
                val finalizados = ordenes.count { it.estado == EstadoOrden.LISTO_PARA_ENTREGA || it.estado == EstadoOrden.ENTREGADO }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📊 Resumen Operativo del Taller", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: $total", style = MaterialTheme.typography.bodySmall)
                            Text("Pendientes: $pendientes", style = MaterialTheme.typography.bodySmall)
                            Text("En Taller: $enTaller", style = MaterialTheme.typography.bodySmall)
                            Text("Finalizados: $finalizados", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Chips de filtro rápido por estado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroEstadoAdmin == null,
                        onClick = { filtroEstadoAdmin = null },
                        label = { Text("Todos (${ordenes.size})") }
                    )
                    EstadoOrden.values().forEach { estadoOpcion ->
                        val cantidad = ordenes.count { it.estado == estadoOpcion }
                        if (cantidad > 0) {
                            FilterChip(
                                selected = filtroEstadoAdmin == estadoOpcion,
                                onClick = {
                                    filtroEstadoAdmin = if (filtroEstadoAdmin == estadoOpcion) null else estadoOpcion
                                },
                                label = { Text("${estadoOpcion.name.replace("_", " ")} ($cantidad)") }
                            )
                        }
                    }
                }
            }

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
                    Text("No hay órdenes disponibles para este perfil o filtro.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
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
                        items(ordenesFiltradas) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = if (orden.estado == EstadoOrden.INGRESADO && rolUsuario == RolUsuario.TECNICO) {
                                    "⚡ Nueva Solicitud Cliente - Equipo #${orden.equipoId}"
                                } else {
                                    "Equipo #${orden.equipoId}"
                                },
                                onClick = { onOrdenClick(orden.id) },
                                onEliminarClick = if (rolUsuario == RolUsuario.ADMINISTRADOR) {
                                    { ordenAEliminar = orden }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}