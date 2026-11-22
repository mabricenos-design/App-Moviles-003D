package com.example.macmovilcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.model.EstadoOrden
import com.example.macmovilcare.ui.theme.*

@Composable
fun EstadoBadge(estado: EstadoOrden) {
    val (fondo, textoColor, label) = when (estado) {
        EstadoOrden.INGRESADO -> Triple(EstadoGrisFondo, EstadoGris, "Ingresado")
        EstadoOrden.EN_DIAGNOSTICO -> Triple(EstadoAzulFondo, EstadoAzul, "En Diagnóstico")
        EstadoOrden.COTIZADO_PENDIENTE -> Triple(EstadoAmarilloFondo, EstadoAmarillo, "Cotizado")
        EstadoOrden.APROBADO -> Triple(EstadoVerdeFondo, EstadoVerde, "Aprobado")
        EstadoOrden.RECHAZADO -> Triple(EstadoRojoFondo, EstadoRojo, "Rechazado")
        EstadoOrden.EN_REPARACION -> Triple(EstadoAzulFondo, EstadoAzul, "En Reparación")
        EstadoOrden.LISTO_PARA_ENTREGA -> Triple(EstadoVerdeFondo, EstadoVerde, "Listo p/ Entrega")
        EstadoOrden.ENTREGADO -> Triple(EstadoGrisFondo, EstadoGris, "Entregado")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = fondo, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textoColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textoColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}