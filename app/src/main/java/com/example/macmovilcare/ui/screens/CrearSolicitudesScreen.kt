package com.example.macmovilcare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.model.SolicitudUiState
import com.example.macmovilcare.ui.components.CampoTextoCustom
import com.example.macmovilcare.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(
    state: SolicitudUiState,
    onEquipoIdChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onEnviarSolicitud: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        containerColor = GrisFondo,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF122326)),
                title = {
                    Text(
                        "Nueva Solicitud",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, GrisBorde),
                colors = CardDefaults.cardColors(containerColor = GrisSuperficie)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Datos del Equipo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Text(
                        text = "Detalla el equipo y la falla que presenta para asignarlo a un técnico.",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CampoTextoCustom(
                        valor = state.equipoId,
                        onValueChange = onEquipoIdChange,
                        label = "ID o N° de Serie del Equipo",
                        error = state.errores.equipoId,
                        leadingIcon = {
                            Icon(Icons.Rounded.Devices, contentDescription = null, tint = AzulVariante, modifier = Modifier.size(20.dp))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CampoTextoCustom(
                        valor = state.descripcionProblema,
                        onValueChange = onDescripcionChange,
                        label = "Descripción del problema o falla",
                        error = state.errores.descripcionProblema,
                        singleLine = false,
                        minLines = 4
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onEnviarSolicitud,
                        enabled = !state.cargando,
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
                        if (state.cargando) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Solicitud a Taller", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            state.mensajeExito?.let { mensaje ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulContenedor),
                    border = BorderStroke(1.dp, AzulVariante.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF7CE8E2),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}