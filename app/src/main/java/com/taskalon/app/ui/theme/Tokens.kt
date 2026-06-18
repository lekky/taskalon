package com.taskalon.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * Taskalon's themed color tokens. Read these from [LocalTaskalonColors] / [LocalAccent]
 * rather than `MaterialTheme.colorScheme` — the design is custom-tokened. Hex values are
 * authoritative from the handoff.
 */
data class TaskalonColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val fg1: Color,
    val fg2: Color,
    val fg3: Color,
    val isDark: Boolean,
)

val LightColors = TaskalonColors(
    bg = Color(0xFFF6F7F9),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEFF1F4),
    border = Color(0xFFE5E8EC),
    fg1 = Color(0xFF1A1C20),
    fg2 = Color(0xFF5C616B),
    fg3 = Color(0xFF9A9FA8),
    isDark = false,
)

val DarkColors = TaskalonColors(
    bg = Color(0xFF121317),
    surface = Color(0xFF1B1D22),
    surface2 = Color(0xFF242730),
    border = Color(0xFF30343D),
    fg1 = Color(0xFFECE9E2),
    fg2 = Color(0xFFA39E94),
    fg3 = Color(0xFF6F6C65),
    isDark = true,
)

val LocalTaskalonColors = staticCompositionLocalOf { LightColors }
val LocalAccent = staticCompositionLocalOf { Color(0xFF0E9F6E) }
val LocalAppFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.SansSerif }

/** Fixed priority colors (not themed): index 0 = Low(1), 1 = Medium(2), 2 = High(3). */
val PriorityColors = listOf(
    Color(0xFF3B6FE0), // Low — blue
    Color(0xFFE0922F), // Medium — amber
    Color(0xFFE5484D), // High — red
)

/** Null for priority 0 (None). */
fun priorityColor(priority: Int): Color? = PriorityColors.getOrNull(priority - 1)

/** The fixed "overdue" red used by due badges and the delete action. */
val OverdueRed = Color(0xFFE5484D)

/** Parse a `#RRGGBB` (or `#AARRGGBB`) hex string into a [Color], falling back to Green. */
fun hexColor(hex: String): Color = runCatching {
    val clean = hex.trim().removePrefix("#")
    when (clean.length) {
        6 -> Color(("FF$clean").toLong(16))
        8 -> Color(clean.toLong(16))
        else -> Color(0xFF0E9F6E)
    }
}.getOrDefault(Color(0xFF0E9F6E))
