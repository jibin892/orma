package org.orma.project_90.media

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.orma.project_90.designsystem.OrmaColors

@Composable
fun OrmaRemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.background(OrmaColors.CellBackground),
        contentScale = ContentScale.Crop,
    )
}
