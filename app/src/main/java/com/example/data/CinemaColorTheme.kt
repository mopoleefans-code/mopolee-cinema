package com.example.data

import androidx.compose.ui.graphics.Color

enum class CinemaColorTheme(
    val id: String,
    val displayName: String,
    val accentName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBorderColor: Color,
    val glowColor: Color
) {
    ROYAL_GOLD(
        id = "gold",
        displayName = "Royal Gold Cinema",
        accentName = "Warm Gold & Obsidian",
        primaryColor = Color(0xFFFFB80B),
        secondaryColor = Color(0xFFFFCC4D),
        backgroundColor = Color(0xFF0C1526),
        surfaceColor = Color(0xFF16233A),
        cardBorderColor = Color(0xFF233655),
        glowColor = Color(0xFFFFB80B)
    ),
    NEON_VIOLET(
        id = "violet",
        displayName = "Neon Cyber Violet",
        accentName = "Electric Purple & Magenta",
        primaryColor = Color(0xFFB347F7),
        secondaryColor = Color(0xFFF35DF9),
        backgroundColor = Color(0xFF100927),
        surfaceColor = Color(0xFF1E1340),
        cardBorderColor = Color(0xFF3B2067),
        glowColor = Color(0xFFB347F7)
    ),
    ELECTRIC_CYAN(
        id = "cyan",
        displayName = "Electric Cyan Ocean",
        accentName = "Aquatic Neon & Deep Teal",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF64FFDA),
        backgroundColor = Color(0xFF041828),
        surfaceColor = Color(0xFF0B2943),
        cardBorderColor = Color(0xFF154C73),
        glowColor = Color(0xFF00E5FF)
    ),
    CRIMSON_VELVET(
        id = "crimson",
        displayName = "Crimson Velvet Noir",
        accentName = "Ruby Red & Midnight",
        primaryColor = Color(0xFFFF2A3E),
        secondaryColor = Color(0xFFFF6B6F),
        backgroundColor = Color(0xFF170808),
        surfaceColor = Color(0xFF2A1111),
        cardBorderColor = Color(0xFF551D1D),
        glowColor = Color(0xFFFF2A3E)
    ),
    EMERALD_MATRIX(
        id = "emerald",
        displayName = "Emerald Matrix Glow",
        accentName = "Neon Green & Deep Onyx",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF69F0AE),
        backgroundColor = Color(0xFF02170D),
        surfaceColor = Color(0xFF0A2B1B),
        cardBorderColor = Color(0xFF115033),
        glowColor = Color(0xFF00E676)
    ),
    SUNSET_AMBER(
        id = "amber",
        displayName = "Sunset Amber Flame",
        accentName = "Warm Tangerine & Coral",
        primaryColor = Color(0xFFFF6D00),
        secondaryColor = Color(0xFFFF9E80),
        backgroundColor = Color(0xFF170C04),
        surfaceColor = Color(0xFF2B1909),
        cardBorderColor = Color(0xFF522E0E),
        glowColor = Color(0xFFFF6D00)
    )
}
