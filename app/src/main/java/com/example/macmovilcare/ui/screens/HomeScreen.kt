package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.TarjetaOrden


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ordenes: List<OrdenTrabajo>,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE, // Recibe el rol activo
    esPantallaAncha: Boolean = false,
    onOrdenClick: (String) -> Unit,
    onNuevaSolicitudClick: () -> Unit
) {
    // 1. Filtrado de órdenes según la responsabilidad del rol
    val ordenesFiltradas = when (rolUsuario) {
        RolUsuario.CLIENTE -> ordenes // En app real: solo las del cliente autenticado
        RolUsuario.TECNICO -> ordenes.filter { it.estado.name == "EN_REVISION" || it.estado.name == "EN_REPARACION" }
        RolUsuario.ADMINISTRADOR -> ordenes // Ve la totalidad de las órdenes del sistema
    }

    // 2. Personalización de título
    val tituloPantalla = when (rolUsuario) {
        RolUsuario.CLIENTE -> "Mis Equipos y Solicitude"
        RolUsuario.TECNICO -> "Órdenes de Taller (Pendientes)"
        RolUsuario.ADMINISTRADOR -> "Gestión Global de Órdenes"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(tituloPantalla) }) },
        // SOLO EL CLIENTE VE EL BOTÓN DE CREAR SOLICITUD
        floatingActionButton = {
            if (rolUsuario == RolUsuario.CLIENTE) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaSolicitudClick,
                    text = { Text("Nueva Solicitud") },
                    icon = { Text("+") }
                )
            }
        }
    ) { innerPadding ->
        if (ordenesFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No hay órdenes disponibles para este perfil.")
            }
        } else if (esPantallaAncha) {
            // Layout de 2 columnas para Tablets (Medium / Expanded)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Seleccione una orden para ver el detalle",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            // Layout estándar para Smartphones (Compact)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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