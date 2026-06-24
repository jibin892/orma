package org.orma.project_90.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalOrmaTypographyOverride = staticCompositionLocalOf<Typography?> { null }

@Composable
fun OrmaTypographyOverride(
    typography: Typography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalOrmaTypographyOverride provides typography,
        content = content,
    )
}

@Composable
fun OrmaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val typography = LocalOrmaTypographyOverride.current ?: ormaTypography()

    MaterialTheme(
        colorScheme = if (darkTheme) OrmaMaterialDarkColorScheme else OrmaMaterialColorScheme,
        typography = typography,
        shapes = OrmaMaterialShapes,
    ) {
        ProvideTextStyle(value = typography.bodyLarge, content = content)
    }
}
