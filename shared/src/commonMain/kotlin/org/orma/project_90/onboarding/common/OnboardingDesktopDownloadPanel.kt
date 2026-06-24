package org.orma.project_90.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaFlatIcon
import org.orma.project_90.designsystem.OrmaFlatIconKind
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.downloads.OrmaDesktopDownloadPlatform
import org.orma.project_90.downloads.OrmaDesktopDownloads
import org.orma.project_90.downloads.currentOrmaDesktopDownloadPlatform
import org.orma.project_90.downloads.isOrmaWebDownloadSurface
import org.orma.project_90.downloads.openOrmaDownload

@Composable
internal fun OrmaDesktopDownloadPanel(
    wide: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isOrmaWebDownloadSurface()) return
    val downloadOptions = ormaDesktopDownloadOptions(currentOrmaDesktopDownloadPlatform())

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Desktop app",
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Use ORMA in a dedicated app on your computer.",
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        if (wide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                downloadOptions.forEach { option ->
                    OrmaDesktopDownloadButton(
                        title = option.title,
                        subtitle = option.subtitle,
                        platform = option.platform,
                        onClick = { openOrmaDownload(option.url) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                downloadOptions.forEach { option ->
                    OrmaDesktopDownloadButton(
                        title = option.title,
                        subtitle = option.subtitle,
                        platform = option.platform,
                        onClick = { openOrmaDownload(option.url) },
                    )
                }
            }
        }
    }
}

private fun ormaDesktopDownloadOptions(
    platform: OrmaDesktopDownloadPlatform,
): List<OrmaDesktopDownloadOption> = when (platform) {
    OrmaDesktopDownloadPlatform.Mac -> listOf(OrmaDesktopDownloadOption.Mac)
    OrmaDesktopDownloadPlatform.Windows -> listOf(OrmaDesktopDownloadOption.Windows)
    OrmaDesktopDownloadPlatform.Unknown -> listOf(
        OrmaDesktopDownloadOption.Mac,
        OrmaDesktopDownloadOption.Windows,
    )
}

@Composable
private fun OrmaDesktopDownloadButton(
    title: String,
    subtitle: String,
    platform: OrmaDesktopDownloadPlatform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = OrmaShapes.SmallCard,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(1.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = OrmaShapes.Capsule,
                color = OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    OrmaDesktopPlatformIcon(
                        platform = platform,
                        color = OrmaColors.IconPrimary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OrmaDownloadArrowIcon(
                color = OrmaColors.TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun OrmaDesktopPlatformIcon(
    platform: OrmaDesktopDownloadPlatform,
    color: Color,
    modifier: Modifier = Modifier.size(23.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = (size.minDimension * 0.095f).coerceAtLeast(1.6f)
        when (platform) {
            OrmaDesktopDownloadPlatform.Mac -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.20f),
                    size = Size(size.width * 0.68f, size.height * 0.48f),
                    cornerRadius = CornerRadius(size.minDimension * 0.07f, size.minDimension * 0.07f),
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.50f, size.height * 0.68f),
                    end = Offset(size.width * 0.50f, size.height * 0.82f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.82f),
                    end = Offset(size.width * 0.66f, size.height * 0.82f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = color.copy(alpha = 0.75f),
                    radius = size.minDimension * 0.025f,
                    center = Offset(size.width * 0.50f, size.height * 0.61f),
                )
            }
            OrmaDesktopDownloadPlatform.Windows,
            OrmaDesktopDownloadPlatform.Unknown -> {
                val paneWidth = size.width * 0.27f
                val paneHeight = size.height * 0.25f
                val gapX = size.width * 0.06f
                val gapY = size.height * 0.06f
                val startX = size.width * 0.20f
                val startY = size.height * 0.20f
                repeat(2) { row ->
                    repeat(2) { column ->
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(
                                x = startX + column * (paneWidth + gapX),
                                y = startY + row * (paneHeight + gapY),
                            ),
                            size = Size(paneWidth, paneHeight),
                            cornerRadius = CornerRadius(size.minDimension * 0.025f, size.minDimension * 0.025f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrmaDownloadArrowIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    OrmaFlatIcon(
        kind = OrmaFlatIconKind.Download,
        modifier = modifier,
        color = color,
    )
}

private data class OrmaDesktopDownloadOption(
    val title: String,
    val subtitle: String,
    val platform: OrmaDesktopDownloadPlatform,
    val url: String,
) {
    companion object {
        val Mac = OrmaDesktopDownloadOption(
            title = "Download Mac",
            subtitle = "DMG installer",
            platform = OrmaDesktopDownloadPlatform.Mac,
            url = OrmaDesktopDownloads.MacUrl,
        )

        val Windows = OrmaDesktopDownloadOption(
            title = "Download Windows",
            subtitle = "MSI installer",
            platform = OrmaDesktopDownloadPlatform.Windows,
            url = OrmaDesktopDownloads.WindowsUrl,
        )
    }
}
