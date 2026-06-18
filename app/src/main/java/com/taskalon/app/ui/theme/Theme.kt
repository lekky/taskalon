package com.taskalon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.taskalon.app.data.Settings
import com.taskalon.app.data.ThemeMode

/**
 * Applies Taskalon's design tokens for the given [settings]. The custom palette is exposed
 * via [LocalTaskalonColors] / [LocalAccent] / [LocalAppFontFamily]; a matching Material 3
 * [MaterialTheme] is also provided so standard M3 components (ripples, selection) inherit it.
 */
@Composable
fun TaskalonTheme(
    settings: Settings,
    content: @Composable () -> Unit,
) {
    val dark = when (settings.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkColors else LightColors
    val accent = hexColor(settings.accent)
    val fontFamily = settings.font.toFontFamily()

    val scheme = if (dark) {
        darkColorScheme(
            primary = accent,
            background = colors.bg,
            surface = colors.surface,
            onBackground = colors.fg1,
            onSurface = colors.fg1,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = colors.bg,
            surface = colors.surface,
            onBackground = colors.fg1,
            onSurface = colors.fg1,
        )
    }

    CompositionLocalProvider(
        LocalTaskalonColors provides colors,
        LocalAccent provides accent,
        LocalAppFontFamily provides fontFamily,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = taskalonTypography(fontFamily),
            content = content,
        )
    }
}
