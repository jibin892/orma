package org.orma.project_90.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class OrmaWindowClass {
    Mobile,
    Wide,
}

data class OrmaProgressItem(
    val label: String,
    val complete: Boolean,
    val active: Boolean,
)

data class OrmaRailStep(
    val number: String,
    val title: String,
    val body: String,
    val complete: Boolean,
    val active: Boolean,
)

@Composable
fun OrmaGoogleBrandIcon(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = OrmaGoogleGIcon,
        contentDescription = null,
        modifier = modifier.alpha(if (enabled) 1f else 0.32f),
        tint = Color.Unspecified,
    )
}

private val OrmaGoogleGIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GoogleG",
        defaultWidth = 18.dp,
        defaultHeight = 18.dp,
        viewportWidth = 18f,
        viewportHeight = 18f,
    ).apply {
        path(
            fill = SolidColor(Color(0xFF4285F4)),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(17.64f, 9.2045f)
            curveTo(17.64f, 8.5664f, 17.5827f, 7.9527f, 17.4764f, 7.3636f)
            horizontalLineTo(9f)
            verticalLineTo(10.8455f)
            horizontalLineTo(13.8436f)
            curveTo(13.635f, 11.9705f, 13.0009f, 12.9237f, 12.0477f, 13.5619f)
            verticalLineTo(15.8201f)
            horizontalLineTo(14.9564f)
            curveTo(16.6582f, 14.2533f, 17.64f, 11.946f, 17.64f, 9.2045f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFF34A853)),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(9f, 18f)
            curveTo(11.43f, 18f, 13.4673f, 17.1941f, 14.9564f, 15.82f)
            lineTo(12.0477f, 13.5618f)
            curveTo(11.2418f, 14.1018f, 10.2109f, 14.4209f, 9f, 14.4209f)
            curveTo(6.6559f, 14.4209f, 4.6718f, 12.8377f, 3.964f, 10.7105f)
            horizontalLineTo(0.9573f)
            verticalLineTo(13.0423f)
            curveTo(2.4382f, 15.9832f, 5.4818f, 18f, 9f, 18f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFBBC05)),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(3.964f, 10.7105f)
            curveTo(3.784f, 10.1705f, 3.6818f, 9.5937f, 3.6818f, 9f)
            curveTo(3.6818f, 8.4063f, 3.7841f, 7.8295f, 3.9641f, 7.2895f)
            verticalLineTo(4.9577f)
            horizontalLineTo(0.9573f)
            curveTo(0.3477f, 6.1732f, 0f, 7.5477f, 0f, 9f)
            curveTo(0f, 10.4523f, 0.3477f, 11.8268f, 0.9573f, 13.0423f)
            lineTo(3.964f, 10.7105f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFEA4335)),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(9f, 3.5791f)
            curveTo(10.3214f, 3.5791f, 11.5077f, 4.0332f, 12.4405f, 4.9251f)
            lineTo(15.0218f, 2.3437f)
            curveTo(13.4632f, 0.8918f, 11.4259f, 0f, 9f, 0f)
            curveTo(5.4818f, 0f, 2.4382f, 2.0168f, 0.9573f, 4.9577f)
            lineTo(3.964f, 7.2895f)
            curveTo(4.6718f, 5.1623f, 6.6559f, 3.579f, 9f, 3.579f)
            close()
        }
    }.build()
}

@Composable
fun OrmaAdaptiveSurface(
    modifier: Modifier = Modifier,
    mobileBelow: Dp = 840.dp,
    content: @Composable OrmaWindowClass.() -> Unit,
) {
    OrmaTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = OrmaColors.ScreenBackground,
            contentColor = OrmaColors.TextPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OrmaColors.ScreenBackground),
            ) {
                val windowClass = if (maxWidth < mobileBelow) OrmaWindowClass.Mobile else OrmaWindowClass.Wide
                windowClass.content()
            }
        }
    }
}

@Composable
fun OrmaMobileShell(
    header: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        header()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            content()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun OrmaWideShell(
    rail: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentMaxWidth: Dp = 1080.dp,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier
                .width(344.dp)
                .fillMaxHeight(),
        ) {
            rail()
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = OrmaShapes.PremiumCard,
            color = OrmaColors.ScreenBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(0.8.dp, OrmaColors.Hairline),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(34.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(modifier = Modifier.widthIn(max = contentMaxWidth)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun OrmaBrandRow(
    modifier: Modifier = Modifier,
    platform: String? = null,
    dark: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = OrmaShapes.SmallCard,
                color = OrmaColors.Accent,
                contentColor = OrmaColors.ScreenBackground,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "O", style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                text = "ORMA",
                style = MaterialTheme.typography.titleMedium,
                color = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.Accent,
            )
        }
        trailing?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = it,
            )
        } ?: platform?.let {
            OrmaSmallPill(text = it, dark = dark)
        }
    }
}

@Composable
fun OrmaSmallPill(
    text: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.Capsule,
        color = if (dark) OrmaColors.DarkSubtleBadge else OrmaColors.Accent.copy(alpha = 0.08f),
        contentColor = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.Accent,
        border = BorderStroke(1.dp, if (dark) OrmaColors.DarkHairline else OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun OrmaScreenHeader(
    eyebrow: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.Accent,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = OrmaColors.TextPrimary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = OrmaColors.TextSecondary,
        )
    }
}

@Composable
fun OrmaScreenColumn(
    eyebrow: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OrmaScreenHeader(eyebrow = eyebrow, title = title, body = body)
        content()
    }
}

@Composable
fun OrmaFormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun OrmaActionRow(
    primaryText: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    if (secondaryText == null || onSecondary == null) {
        OrmaPrimaryButton(
            text = primaryText,
            onClick = onPrimary,
            modifier = modifier.fillMaxWidth(),
            enabled = primaryEnabled,
        )
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrmaSecondaryButton(
                text = secondaryText,
                onClick = onSecondary,
                modifier = Modifier.weight(1f),
            )
            OrmaPrimaryButton(
                text = primaryText,
                onClick = onPrimary,
                modifier = Modifier.weight(1f),
                enabled = primaryEnabled,
            )
        }
    }
}

@Composable
fun <T> OrmaSegmentedRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            Surface(
                modifier = Modifier.clickable { onSelected(option) },
                shape = OrmaShapes.Capsule,
                color = if (active) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (active) OrmaColors.ScreenBackground else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = label(option),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun OrmaKeyValueList(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = if (dark) OrmaColors.DarkSubtleBadge else OrmaColors.CellBackground,
        contentColor = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.TextPrimary,
        border = if (dark) BorderStroke(0.8.dp, OrmaColors.DarkHairline) else null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.first,
                        modifier = Modifier.weight(0.9f),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dark) OrmaColors.DarkTextTertiary else OrmaColors.TextSecondary,
                    )
                    Text(
                        text = row.second.ifBlank { "Not set" },
                        modifier = Modifier.weight(1.1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.Accent,
                        textAlign = TextAlign.End,
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = if (dark) OrmaColors.DarkHairline else OrmaColors.Divider)
                }
            }
        }
    }
}

@Composable
fun OrmaUploadRow(
    title: String,
    body: String,
    initials: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = OrmaShapes.SmallCard,
                color = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (selected) OrmaColors.ScreenBackground else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials.ifBlank { "O" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
fun OrmaSwitchRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CellBackground,
        contentColor = OrmaColors.TextPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = OrmaColors.TextPrimary)
                Text(text = body, style = MaterialTheme.typography.bodyMedium, color = OrmaColors.TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OrmaColors.ScreenBackground,
                    checkedTrackColor = OrmaColors.Accent,
                    uncheckedThumbColor = OrmaColors.Accent.copy(alpha = 0.42f),
                    uncheckedTrackColor = OrmaColors.Accent.copy(alpha = 0.10f),
                ),
            )
        }
    }
}

@Composable
fun OrmaOtpCells(
    code: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val interDigitSpacing = when {
            maxWidth < 320.dp -> 4.dp
            maxWidth < 360.dp -> 6.dp
            else -> 8.dp
        }
        val dashWidth = when {
            maxWidth < 320.dp -> 18.dp
            maxWidth < 360.dp -> 20.dp
            else -> 24.dp
        }
        val cellWidth = ((maxWidth - dashWidth - interDigitSpacing * 4) / 6f).coerceIn(36.dp, 52.dp)
        val cellHeight = (cellWidth * (56f / 52f)).coerceIn(40.dp, 56.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                OrmaOtpCell(index = index, code = code, width = cellWidth, height = cellHeight)
                if (index < 2) Spacer(modifier = Modifier.width(interDigitSpacing))
            }
            Text(
                text = "-",
                modifier = Modifier.width(dashWidth),
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            repeat(3) { relativeIndex ->
                val index = relativeIndex + 3
                OrmaOtpCell(index = index, code = code, width = cellWidth, height = cellHeight)
                if (index < 5) Spacer(modifier = Modifier.width(interDigitSpacing))
            }
        }
    }
}

@Composable
private fun OrmaOtpCell(
    index: Int,
    code: String,
    width: Dp,
    height: Dp,
) {
    val filled = index < code.length
    val current = index == code.length && code.length < 6
    Surface(
        modifier = Modifier
            .width(width)
            .height(height),
        shape = OrmaShapes.CheckoutButton,
        color = if (current) OrmaColors.CellBackground.copy(alpha = 0.85f) else OrmaColors.CellBackground,
        contentColor = OrmaColors.Accent,
        border = if (current) {
            BorderStroke(1.5.dp, OrmaColors.Accent.copy(alpha = 0.30f))
        } else {
            null
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                filled -> Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(OrmaShapes.Capsule)
                        .background(OrmaColors.Accent),
                )
                current -> Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(22.dp)
                        .background(OrmaColors.Accent.copy(alpha = 0.60f)),
                )
            }
        }
    }
}

@Composable
fun OrmaProgressPills(
    items: List<Pair<String, Boolean>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            Surface(
                shape = OrmaShapes.Capsule,
                color = if (item.second) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.08f),
                contentColor = if (item.second) OrmaColors.ScreenBackground else OrmaColors.Accent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = item.first,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun OrmaStepRail(
    steps: List<OrmaRailStep>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        steps.forEach { step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = OrmaShapes.Capsule,
                    color = when {
                        step.active -> OrmaColors.ScreenBackground
                        step.complete -> OrmaColors.DarkSubtleBadge
                        else -> OrmaColors.DarkSubtleBadge
                    },
                    contentColor = if (step.active) OrmaColors.Accent else OrmaColors.DarkTextPrimary,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = step.number, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            step.active -> OrmaColors.DarkTextPrimary
                            step.complete -> OrmaColors.DarkTextSecondary
                            else -> OrmaColors.DarkTextTertiary
                        },
                    )
                    Text(
                        text = step.body,
                        style = MaterialTheme.typography.labelMedium,
                        color = OrmaColors.DarkTextTertiary,
                    )
                }
            }
        }
    }
}
