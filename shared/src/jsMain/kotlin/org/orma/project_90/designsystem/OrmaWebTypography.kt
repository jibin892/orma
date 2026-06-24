package org.orma.project_90.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun OrmaWebTypographyProvider(
    content: @Composable () -> Unit,
) {
    OrmaTypographyOverride(
        typography = ormaWebTypography(),
        content = content,
    )
}

@Composable
private fun ormaWebTypography(): Typography {
    val base = ormaTypography()

    return base.copy(
        displayLarge = base.displayLarge.copy(fontWeight = FontWeight.Medium),
        displayMedium = base.displayMedium.copy(fontWeight = FontWeight.Medium),
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Medium),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Medium),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Medium),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Medium),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Medium),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontWeight = FontWeight.Medium),
        bodyMedium = base.bodyMedium.copy(fontWeight = FontWeight.Medium),
        bodySmall = base.bodySmall.copy(fontWeight = FontWeight.Medium),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontWeight = FontWeight.SemiBold),
    )
}
