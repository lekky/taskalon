package com.taskalon.app.data

import kotlinx.serialization.Serializable

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class SortMode { MANUAL, DUE, PRIORITY }

/** The four user-selectable font families (see `ui/theme/Type.kt`). */
enum class AppFont(val label: String, val descriptor: String) {
    SPLINE("Spline Sans", "Default"),
    PLEX("IBM Plex Sans", "Humanist sans"),
    SERIF("Newsreader", "Serif"),
    MONO("JetBrains Mono", "Monospace"),
}

@Serializable
data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val accent: String = Accents.DEFAULT.hex,
    val sort: SortMode = SortMode.MANUAL,
    val showCompleted: Boolean = true,
    val font: AppFont = AppFont.SPLINE,
)
