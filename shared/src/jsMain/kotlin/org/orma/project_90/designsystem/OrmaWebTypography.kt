package org.orma.project_90.designsystem

import androidx.compose.runtime.Composable

@Composable
fun OrmaWebTypographyProvider(
    content: @Composable () -> Unit,
) {
    OrmaTypographyOverride(
        typography = ormaTypography(),
        content = content,
    )
}
