package com.example.macmovilcare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulPrimario,
    secondary = AzulVariante,
    background = GrisFondo,
    surface = GrisSuperficie,
    onPrimary = Color.White,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    outline = GrisBorde
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulVariante,
    primaryContainer = Color(0xFF1E293B),
    secondary = Color(0xFF60A5FA),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
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