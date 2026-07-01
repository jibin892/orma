package org.orma.project_90.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private object OrmaThemePreset {
    val Accent = Color(0xFF003333)
    val AccentMuted = Color(0xFF3D6B6B)
    val ScreenBackground = Color(0xFFFCFDFE)
    val CellBackground = Color(0xFFF4F7FB)
    val CardBackground = Color(0xFFFFFFFF)
    val WorkspaceChrome = Color(0xFFEEF2FF)
    val WorkspacePanel = Color(0xFFF8FAFC)
    val HeroAccentStart = Accent
    val HeroAccentEnd = Color(0xFF3D6B6B)
    val TextAndIconInk = Color(0xFF173B3D)
    val OnAccent = ScreenBackground
    val Info = Color(0xFF5C9E9E)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Danger = Color(0xFFE11D48)
    val DarkBackground = Color(0xFF0B0F19)
    val DarkSurface = Color(0xFF111827)
    val DarkCard = Color(0xFF1F2937)
    val DarkField = Color(0xFF374151)
    val FocusRing = Color(0xFF5C9E9E)
}

object OrmaColors {
    val Accent = OrmaThemePreset.Accent
    val AccentMuted = OrmaThemePreset.AccentMuted
    val ScreenBackground = OrmaThemePreset.ScreenBackground
    val CellBackground = OrmaThemePreset.CellBackground
    val CardBackground = OrmaThemePreset.CardBackground
    val ElevatedBackground = CellBackground
    val WorkspaceChrome = OrmaThemePreset.WorkspaceChrome
    val WorkspacePanel = OrmaThemePreset.WorkspacePanel
    val HeroAccentStart = OrmaThemePreset.HeroAccentStart
    val HeroAccentEnd = OrmaThemePreset.HeroAccentEnd
    val TextAndIconInk = OrmaThemePreset.TextAndIconInk
    val OnAccent = OrmaThemePreset.OnAccent

    val TextPrimary = TextAndIconInk
    val TextSecondary = TextAndIconInk.copy(alpha = 0.56f)
    val TextTertiary = TextAndIconInk.copy(alpha = 0.42f)
    val TextDisabled = TextAndIconInk.copy(alpha = 0.28f)
    val TextFaint = TextAndIconInk.copy(alpha = 0.18f)
    val IconPrimary = TextAndIconInk
    val IconSecondary = TextAndIconInk.copy(alpha = 0.56f)
    val IconTertiary = TextAndIconInk.copy(alpha = 0.42f)
    val IconDisabled = TextAndIconInk.copy(alpha = 0.28f)

    val DarkTextPrimary = Color.White
    val DarkTextSecondary = Color.White.copy(alpha = 0.60f)
    val DarkTextTertiary = Color.White.copy(alpha = 0.40f)
    val DarkSubtleBadge = Color.White.copy(alpha = 0.15f)
    val DarkHairline = Color.White.copy(alpha = 0.06f)

    val Divider = Accent.copy(alpha = 0.032f)
    val Hairline = Accent.copy(alpha = 0.052f)
    val SkeletonBase = Color(0xFFE7EEF2)
    val SkeletonHighlight = Color(0xFFF8FBFC)
    val Info = OrmaThemePreset.Info
    val Success = OrmaThemePreset.Success
    val Warning = OrmaThemePreset.Warning
    val Error = OrmaThemePreset.Error
    val Danger = OrmaThemePreset.Danger

    val DarkBackground = OrmaThemePreset.DarkBackground
    val DarkSurface = OrmaThemePreset.DarkSurface
    val DarkCard = OrmaThemePreset.DarkCard
    val DarkField = OrmaThemePreset.DarkField
    val FocusRing = OrmaThemePreset.FocusRing
}

internal val OrmaMaterialColorScheme: ColorScheme = lightColorScheme(
    primary = OrmaColors.Accent,
    onPrimary = OrmaColors.OnAccent,
    primaryContainer = OrmaColors.CellBackground,
    onPrimaryContainer = OrmaColors.TextPrimary,
    secondary = OrmaColors.Accent,
    onSecondary = OrmaColors.OnAccent,
    secondaryContainer = OrmaColors.CellBackground,
    onSecondaryContainer = OrmaColors.TextPrimary,
    tertiary = OrmaColors.AccentMuted,
    onTertiary = OrmaColors.OnAccent,
    background = OrmaColors.ScreenBackground,
    onBackground = OrmaColors.TextPrimary,
    surface = OrmaColors.ScreenBackground,
    onSurface = OrmaColors.TextPrimary,
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
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

internal val OrmaMaterialDarkColorScheme: ColorScheme = darkColorScheme(
    primary = OrmaColors.FocusRing,
    onPrimary = OrmaColors.DarkBackground,
    primaryContainer = OrmaColors.DarkField,
    onPrimaryContainer = OrmaColors.DarkTextPrimary,
    secondary = OrmaColors.FocusRing,
    onSecondary = OrmaColors.DarkBackground,
    secondaryContainer = OrmaColors.DarkCard,
    onSecondaryContainer = OrmaColors.DarkTextPrimary,
    tertiary = OrmaColors.AccentMuted,
    onTertiary = Color.White,
    background = OrmaColors.DarkBackground,
    onBackground = OrmaColors.DarkTextPrimary,
    surface = OrmaColors.DarkSurface,
    onSurface = OrmaColors.DarkTextPrimary,
    surfaceVariant = OrmaColors.DarkCard,
    onSurfaceVariant = OrmaColors.DarkTextSecondary,
    surfaceContainerLowest = OrmaColors.DarkBackground,
    surfaceContainerLow = OrmaColors.DarkSurface,
    surfaceContainer = OrmaColors.DarkCard,
    surfaceContainerHigh = OrmaColors.DarkCard,
    surfaceContainerHighest = OrmaColors.DarkField,
    outline = OrmaColors.DarkHairline,
    outlineVariant = Color.White.copy(alpha = 0.10f),
    error = OrmaColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)
