package com.example.macmovilcare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de colores en modo claro (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    primaryContainer = AzulContenedor,
    secondary = AzulVariante,
    background = GrisFondo,
    surface = GrisSuperficie,
    onPrimary = Color.White,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

// Esquema de colores en modo oscuro (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = AzulPrimario,
    primaryContainer = AzulVariante,
    secondary = AzulVariante,
    background = TextoOscuro,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MCmovilCareTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}