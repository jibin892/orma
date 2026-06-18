package org.orma.project_90.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object OrmaColors {
    val Accent = Color(0xFF173B3D)
    val AccentMuted = Color(0xFF245D61)
    val ScreenBackground = Color(0xFFFEF9EF)
    val CellBackground = Color(0xFFF7EEE0)
    val CardBackground = Color(0xFFFFFFFF)
    val ElevatedBackground = CellBackground

    val TextPrimary = Accent
    val TextSecondary = Accent.copy(alpha = 0.40f)
    val TextTertiary = Accent.copy(alpha = 0.30f)
    val TextDisabled = Accent.copy(alpha = 0.20f)
    val TextFaint = Accent.copy(alpha = 0.15f)

    val DarkTextPrimary = Color.White
    val DarkTextSecondary = Color.White.copy(alpha = 0.60f)
    val DarkTextTertiary = Color.White.copy(alpha = 0.40f)
    val DarkSubtleBadge = Color.White.copy(alpha = 0.15f)
    val DarkHairline = Color.White.copy(alpha = 0.06f)

    val Divider = Accent.copy(alpha = 0.06f)
    val Hairline = Accent.copy(alpha = 0.12f)
    val SkeletonBase = Accent.copy(alpha = 0.08f)
    val SkeletonHighlight = Accent.copy(alpha = 0.13f)
    val Warning = Color(0xFF9A6B21)
    val Error = Color(0xFFC33C2E)
}

internal val OrmaMaterialColorScheme: ColorScheme = lightColorScheme(
    primary = OrmaColors.Accent,
    onPrimary = OrmaColors.ScreenBackground,
    primaryContainer = OrmaColors.CellBackground,
    onPrimaryContainer = OrmaColors.Accent,
    secondary = OrmaColors.Accent,
    onSecondary = OrmaColors.ScreenBackground,
    secondaryContainer = OrmaColors.CellBackground,
    onSecondaryContainer = OrmaColors.Accent,
    tertiary = OrmaColors.AccentMuted,
    onTertiary = Color.White,
    background = OrmaColors.ScreenBackground,
    onBackground = OrmaColors.Accent,
    surface = OrmaColors.ScreenBackground,
    onSurface = OrmaColors.Accent,
    surfaceVariant = OrmaColors.CellBackground,
    onSurfaceVariant = OrmaColors.TextSecondary,
    surfaceContainerLowest = OrmaColors.ScreenBackground,
    surfaceContainerLow = OrmaColors.ScreenBackground,
    surfaceContainer = OrmaColors.CellBackground,
    surfaceContainerHigh = OrmaColors.CellBackground,
    surfaceContainerHighest = OrmaColors.CardBackground,
    outline = OrmaColors.Hairline,
    outlineVariant = OrmaColors.Accent.copy(alpha = 0.08f),
    error = OrmaColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFE8E2),
    onErrorContainer = Color(0xFF5F170F),
)
