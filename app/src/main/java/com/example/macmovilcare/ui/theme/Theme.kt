package com.example.macmovilcare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkTealColorScheme = darkColorScheme(
    primary = AzulVariante,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = Color(0xFF7CE8E2),
    secondary = AzulVariante,
    background = GrisFondo,
    surface = GrisSuperficie,
    onPrimary = Color.White,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    outline = GrisBorde,
    error = EstadoRojo,
    errorContainer = EstadoRojoFondo
)

@Composable
fun MCmovilCareTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkTealColorScheme,
        typography = Typography,
        content = content
    )
}