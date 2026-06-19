package org.orma.project_90.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun OrmaLogoPreviewImage(
    bytes: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
)
