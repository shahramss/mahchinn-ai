package com.mahchin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFFD4AF37),
    secondary = Color(0xFFB8860B),
    tertiary = Color(0xFFFFD166),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EEF5),
    onPrimary = Color(0xFF1D1400),
    onSecondary = Color.White,
    onTertiary = Color(0xFF1F1300),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFFFD166),
    secondary = Color(0xFFD4AF37),
    tertiary = Color(0xFFFFE29A),
    background = Color(0xFF06111F),
    surface = Color(0xFF0B1726),
    surfaceVariant = Color(0xFF122235),
    onPrimary = Color(0xFF2A1700),
    onSecondary = Color(0xFF1D1400),
    onTertiary = Color(0xFF2A1700),
    onBackground = Color(0xFFEAF2F8),
    onSurface = Color(0xFFEAF2F8),
    onSurfaceVariant = Color(0xFF9AA8B7),
    outline = Color(0xFF21344A)
)

@Composable
fun MahChinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
