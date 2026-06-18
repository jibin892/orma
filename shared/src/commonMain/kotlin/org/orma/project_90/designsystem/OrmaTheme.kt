package org.orma.project_90.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun OrmaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = OrmaMaterialColorScheme,
        typography = ormaTypography(),
        shapes = OrmaMaterialShapes,
        content = content,
    )
}
