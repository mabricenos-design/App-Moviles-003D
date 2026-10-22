package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.background
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
    // 1. Filtrado de órdenes según la responsabilidad del rol
    val ordenesFiltradas = when (rolUsuario) {
        RolUsuario.CLIENTE -> ordenes
        RolUsuario.TECNICO -> ordenes.filter { it.estado == EstadoOrden.EN_REPARACION || it.estado == EstadoOrden.EN_DIAGNOSTICO || it.estado == EstadoOrden.INGRESADO }
        RolUsuario.ADMINISTRADOR -> ordenes // El Admin ve todas las órdenes
    }

    // 2. Título personalizado
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
        // Solo el CLIENTE tiene acceso a crear solicitudes
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
            } else if (esPantallaAncha) {
                // Layout de 2 columnas para Tablets
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(ordenesFiltradas) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = "Equipo #${orden.equipoId}",
                                onClick = { onOrdenClick(orden.id) }
                            )
                        }
                    }

                    Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Seleccione una orden para ver detalles y cotización",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                // Layout estándar para Smartphones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(ordenesFiltradas) { orden ->
                        TarjetaOrden(
                            orden = orden,
                            nombreEquipo = "Equipo #${orden.equipoId}",
                            onClick = { onOrdenClick(orden.id) }
                        )
                    }
                }
            }
        }
    }
}