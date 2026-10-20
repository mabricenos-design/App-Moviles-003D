package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.ui.components.TarjetaOrden

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ordenes: List<OrdenTrabajo>,
    onOrdenClick: (String) -> Unit,
    onNuevaSolicitudClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Equipos y Órdenes") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaSolicitudClick,
                text = { Text("Nueva Solicitud") },
                icon = { Text("+") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(ordenes) { orden ->
                TarjetaOrden(
                    orden = orden,
                    nombreEquipo = "Equipo #${orden.equipoId}",
                    onClick = { onOrdenClick(orden.id) }
                )
            }
        }
    }
}