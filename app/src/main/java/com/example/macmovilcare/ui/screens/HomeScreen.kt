package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.model.OrdenTrabajo
import com.example.macmovilcare.model.RolUsuario
import com.example.macmovilcare.ui.components.EscanerQrDialog
import com.example.macmovilcare.ui.components.TarjetaOrden
import com.example.macmovilcare.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ordenes: List<OrdenTrabajo>,
    rolUsuario: RolUsuario = RolUsuario.CLIENTE,
    nombreUsuario: String = "Usuario",
    esPantallaAncha: Boolean = false,
    mensajeNotificacion: String? = null,
    resultadoEscaneoQr: String? = null,
    onDescartarNotificacion: () -> Unit = {},
    onQrEscaneadoPorTecnico: (String) -> Unit = {},
    onLimpiarResultadoQr: () -> Unit = {},
    onOrdenClick: (String) -> Unit,
    onNuevaSolicitudClick: () -> Unit,
    onEliminarOrden: (String) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    var ordenAEliminar by remember { mutableStateOf<OrdenTrabajo?>(null) }
    var filtroEstadoAdmin by remember { mutableStateOf<EstadoOrden?>(null) }
    var mostrarEscanerQr by remember { mutableStateOf(false) }

    if (mostrarEscanerQr) {
        EscanerQrDialog(
            onQrDetectado = { codigoLeido ->
                mostrarEscanerQr = false
                onQrEscaneadoPorTecnico(codigoLeido)
            },
            onCerrar = { mostrarEscanerQr = false }
        )
    }

    resultadoEscaneoQr?.let { mensajeResultado ->
        AlertDialog(
            onDismissRequest = onLimpiarResultadoQr,
            containerColor = GrisSuperficie,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Validación de Retiro",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            },
            text = {
                Text(
                    text = mensajeResultado,
                    fontSize = 13.sp,
                    color = if (mensajeResultado.startsWith("Error")) EstadoRojo else Color(0xFF7CE8E2)
                )
            },
            confirmButton = {
                Button(
                    onClick = onLimpiarResultadoQr,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulVariante)
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    ordenAEliminar?.let { orden ->
        AlertDialog(
            onDismissRequest = { ordenAEliminar = null },
            containerColor = GrisSuperficie,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Eliminar Orden #${orden.id}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoOscuro) },
            text = {
                Text(
                    "¿Deseas eliminar permanentemente esta orden y su estado actual (${orden.estado.name})?",
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminarOrden(orden.id)
                        ordenAEliminar = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { ordenAEliminar = null }) {
                    Text("Cancelar", fontSize = 13.sp, color = TextoSecundario)
                }
            }
        )
    }

    val ordenesFiltradas = when (rolUsuario) {
        RolUsuario.CLIENTE -> ordenes
        RolUsuario.TECNICO -> ordenes.filter {
            it.estado == EstadoOrden.INGRESADO ||
                    it.estado == EstadoOrden.EN_DIAGNOSTICO ||
                    it.estado == EstadoOrden.EN_REPARACION ||
                    it.estado == EstadoOrden.COTIZADO_PENDIENTE ||
                    it.estado == EstadoOrden.APROBADO
        }
        RolUsuario.ADMINISTRADOR -> {
            if (filtroEstadoAdmin == null) ordenes else ordenes.filter { it.estado == filtroEstadoAdmin }
        }
    }

    val pendientesCliente = ordenesFiltradas.filter { it.estado == EstadoOrden.COTIZADO_PENDIENTE }
    val otrasOrdenesCliente = ordenesFiltradas.filter { it.estado != EstadoOrden.COTIZADO_PENDIENTE }

    val (tituloPantalla, subtituloRol) = when (rolUsuario) {
        RolUsuario.CLIENTE -> "Mis Equipos" to "Portal de Cliente"
        RolUsuario.TECNICO -> "Taller Técnico" to "Órdenes Asignadas"
        RolUsuario.ADMINISTRADOR -> "Panel Global" to "Administración de Taller"
    }

    Scaffold(
        containerColor = GrisFondo,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF12272A), Color(0xFF1D4F4E))
                        )
                    )
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Column {
                            Text(
                                text = tituloPantalla,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtituloRol,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 1
                            )
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF143537),
                            border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.55f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF7CE8E2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Bienvenido, $nombreUsuario",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7CE8E2),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = onCerrarSesion) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color(0xFF7CE8E2),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            when (rolUsuario) {
                RolUsuario.CLIENTE -> {
                    ExtendedFloatingActionButton(
                        onClick = onNuevaSolicitudClick,
                        shape = RoundedCornerShape(14.dp),
                        containerColor = AzulVariante,
                        contentColor = Color.White,
                        modifier = Modifier.height(46.dp),
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(19.dp)) },
                        text = { Text("Nueva Solicitud", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
                RolUsuario.TECNICO -> {
                    ExtendedFloatingActionButton(
                        onClick = { mostrarEscanerQr = true },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = AzulVariante,
                        contentColor = Color.White,
                        modifier = Modifier.height(46.dp),
                        icon = { Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(19.dp)) },
                        text = { Text("Escanear QR Retiro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (rolUsuario == RolUsuario.ADMINISTRADOR) {
                val total = ordenes.size
                val pendientes = ordenes.count { it.estado == EstadoOrden.INGRESADO || it.estado == EstadoOrden.COTIZADO_PENDIENTE }
                val enTaller = ordenes.count { it.estado == EstadoOrden.APROBADO || it.estado == EstadoOrden.EN_REPARACION }
                val finalizados = ordenes.count { it.estado == EstadoOrden.LISTO_PARA_ENTREGA || it.estado == EstadoOrden.ENTREGADO }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(titulo = "Total", valor = "$total", color = EstadoAzul, modifier = Modifier.weight(1f))
                    KpiCard(titulo = "Pendientes", valor = "$pendientes", color = EstadoRojo, modifier = Modifier.weight(1f))
                    KpiCard(titulo = "En Taller", valor = "$enTaller", color = EstadoAzul, modifier = Modifier.weight(1f))
                    KpiCard(titulo = "Listos", valor = "$finalizados", color = EstadoVerde, modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroEstadoAdmin == null,
                        onClick = { filtroEstadoAdmin = null },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GrisSuperficie,
                            labelColor = TextoSecundario,
                            selectedContainerColor = AzulContenedor,
                            selectedLabelColor = Color(0xFF7CE8E2)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtroEstadoAdmin == null,
                            borderColor = GrisBorde,
                            selectedBorderColor = AzulVariante
                        ),
                        label = { Text("Todos (${ordenes.size})", fontSize = 12.sp) }
                    )
                    EstadoOrden.values().forEach { estadoOpcion ->
                        val cantidad = ordenes.count { it.estado == estadoOpcion }
                        if (cantidad > 0) {
                            val seleccionado = filtroEstadoAdmin == estadoOpcion
                            FilterChip(
                                selected = seleccionado,
                                onClick = {
                                    filtroEstadoAdmin = if (seleccionado) null else estadoOpcion
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = GrisSuperficie,
                                    labelColor = TextoSecundario,
                                    selectedContainerColor = AzulContenedor,
                                    selectedLabelColor = Color(0xFF7CE8E2)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = seleccionado,
                                    borderColor = GrisBorde,
                                    selectedBorderColor = AzulVariante
                                ),
                                label = { Text("${estadoOpcion.name.replace("_", " ")} ($cantidad)", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            if (rolUsuario == RolUsuario.TECNICO && mensajeNotificacion != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulContenedor),
                    border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = AzulVariante,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nueva Solicitud Recibida", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                            Text(mensajeNotificacion, fontSize = 12.sp, color = TextoSecundario)
                        }
                        TextButton(onClick = onDescartarNotificacion) {
                            Text("Listo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulVariante)
                        }
                    }
                }
            }

            if (ordenesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay órdenes registradas en esta vista.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp, top = 6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (rolUsuario == RolUsuario.CLIENTE && pendientesCliente.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = EstadoRojoFondo.copy(alpha = 0.7f)
                                ),
                                border = BorderStroke(1.dp, EstadoRojo.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = EstadoRojo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Cotizaciones por Autorizar (${pendientesCliente.size})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EstadoRojo
                                        )
                                        Text(
                                            text = "Revisa el diagnóstico técnico y aprueba el presupuesto.",
                                            fontSize = 12.sp,
                                            color = TextoOscuro.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }

                        items(pendientesCliente) { orden ->
                            TarjetaOrden(
                                orden = orden,
                                nombreEquipo = "Equipo #${orden.equipoId} · Requiere aprobación",
                                onClick = { onOrdenClick(orden.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Historial de Órdenes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoSecundario
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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
                                    "Nueva solicitud · Equipo #${orden.equipoId}"
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

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GrisBorde),
        colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = valor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = TextoSecundario
            )
        }
    }
}