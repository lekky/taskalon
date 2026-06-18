package com.taskalon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF1F8A70)

private val LightColors = lightColorScheme(
    primary = Accent,
)

private val DarkColors = darkColorScheme(
    primary = Accent,
)

/**
 * Minimal Material 3 theme for the scaffold. The real Taskalon design tokens
 * (custom palette, fonts, accents) replace this when the handoff is implemented.
 */
@Composable
fun TaskalonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
