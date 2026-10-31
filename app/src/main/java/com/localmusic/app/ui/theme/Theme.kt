package com.localmusic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// —— 品牌色 ——
val KugouBlue = Color(0xFF2CA2F9)
val KugouBlueDark = Color(0xFF169CDD)
val KugouBlueLight = Color(0xFF4FB5FF)

val AccentPink = Color(0xFFFF5C7A)
val AccentGold = Color(0xFFFFC24B)

// —— 深色 ——
val DarkBackground = Color(0xFF0B0F14)
val DarkSurface = Color(0xFF151B23)
val DarkSurfaceHigh = Color(0xFF1C2430)
val DarkSurfaceVariant = Color(0xFF252E3A)
val DarkOnSurfaceVariant = Color(0xFF8B98A6)
val DarkOutline = Color(0xFF2E3844)

// —— 浅色 ——
val LightBackground = Color(0xFFF3F6FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFF8FAFC)
val LightSurfaceVariant = Color(0xFFE8EEF4)
val LightOnSurfaceVariant = Color(0xFF5A6470)
val LightOutline = Color(0xFFD5DDE6)

private val DarkColors = darkColorScheme(
    primary = KugouBlueLight,
    onPrimary = Color(0xFF00243C),
    primaryContainer = Color(0xFF0D4F7A),
    onPrimaryContainer = Color(0xFFC8E8FF),
    secondary = KugouBlue,
    onSecondary = Color(0xFF00243C),
    tertiary = AccentPink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFE6EDF4),
    surface = DarkSurface,
    onSurface = Color(0xFFE6EDF4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1F2833)
)

private val LightColors = lightColorScheme(
    primary = KugouBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EBFB),
    onPrimaryContainer = Color(0xFF0A4A72),
    secondary = KugouBlue,
    onSecondary = Color.White,
    tertiary = AccentPink,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF15202B),
    surface = LightSurface,
    onSurface = Color(0xFF15202B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceHigh,
    outline = LightOutline,
    outlineVariant = Color(0xFFE4EAF0)
)

@Composable
fun LocalMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
