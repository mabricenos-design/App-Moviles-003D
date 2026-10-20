package com.example.macmovilcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.ui.theme.*

@Composable
fun EstadoBadge(estado: EstadoOrden) {
    val (fondo, textoColor, label) = when (estado) {
        EstadoOrden.INGRESADO -> Triple(Color(0xFFE0E0E0), Color.DarkGray, "Ingresado")
        EstadoOrden.EN_DIAGNOSTICO -> Triple(EstadoAzul, Color.White, "En Diagnóstico")
        EstadoOrden.COTIZADO_PENDIENTE -> Triple(EstadoAmarillo, Color.DarkGray, "Cotizado / Pendiente")
        EstadoOrden.APROBADO -> Triple(EstadoVerde, Color.White, "Aprobado")
        EstadoOrden.RECHAZADO -> Triple(EstadoRojo, Color.White, "Rechazado")
        EstadoOrden.EN_REPARACION -> Triple(AzulPrimario, Color.White, "En Reparación")
        EstadoOrden.LISTO_PARA_ENTREGA -> Triple(EstadoVerde, Color.White, "Listo p/ Entrega")
        EstadoOrden.ENTREGADO -> Triple(Color(0xFF9E9E9E), Color.White, "Entregado")
    }

    Text(
        text = label,
        color = textoColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color = fondo, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}