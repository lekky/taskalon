package com.taskalon.app.data

/**
 * The user-selectable accent palette. Default is [GREEN] (`#0E9F6E`). Stored on
 * [Settings.accent] as the hex string so unknown/custom values survive round-trips.
 */
enum class Accents(val displayName: String, val hex: String) {
    GREEN("Green", "#0E9F6E"),
    TEAL("Teal", "#119DA4"),
    BLUE("Blue", "#3B6FE0"),
    VIOLET("Violet", "#7C5CFF"),
    ROSE("Rose", "#E0407A"),
    FLAME("Flame", "#FF531A"),
    AMBER("Amber", "#E0922F"),
    GRAPHITE("Graphite", "#5A5550");

    companion object {
        val DEFAULT = GREEN
    }
}
