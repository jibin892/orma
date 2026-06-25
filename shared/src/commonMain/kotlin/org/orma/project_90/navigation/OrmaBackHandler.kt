package org.orma.project_90.navigation

import androidx.compose.runtime.Composable

@Composable
internal expect fun OrmaBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
