package org.orma.project_90.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import org.orma.project_90.designsystem.OrmaColors

@Composable
actual fun OrmaLogoPreviewImage(
    bytes: ByteArray,
    contentDescription: String?,
    modifier: Modifier,
) {
    val bitmap = remember(bytes) {
        runCatching {
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }
    if (bitmap == null) {
        Box(modifier = modifier.background(OrmaColors.CellBackground))
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
