package com.example.ranger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OliveGreen,
    onPrimary = Color.Black, // Ensure button text is black
    primaryContainer = LightKhaki,
    onPrimaryContainer = DarkGreen,
    secondary = Khaki,
    onSecondary = DarkGreen,
    background = OliveGreen, // Olive green background
    surface = LightKhaki,
    onSurface = DarkGreen,
    error = MilitaryBrown
)

private val DarkColorScheme = darkColorScheme(
    primary = Khaki,
    onPrimary = Color.Black, // Button text should also be black in dark mode
    primaryContainer = OliveGreen,
    onPrimaryContainer = OffWhite,
    secondary = CamouflageGreen,
    onSecondary = OffWhite,
    background = DarkGreen, // Dark olive green background
    surface = CamouflageGreen,
    onSurface = OffWhite,
    error = MilitaryBrown
)

@Composable
fun RangerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
