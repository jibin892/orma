package org.orma.project_90.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun OrmaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = OrmaShapes.CheckoutButton
    val iconKind = ormaButtonIconForText(text)
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.35f),
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKind != null) {
                OrmaFlatIcon(
                    kind = iconKind,
                    modifier = Modifier.size(17.dp),
                    color = if (enabled) OrmaColors.OnAccent else OrmaColors.OnAccent.copy(alpha = 0.70f),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.OnAccent,
            )
        }
    }
}

@Composable
fun OrmaFullButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OrmaPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun OrmaCapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.Capsule,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.28f),
        contentColor = OrmaColors.OnAccent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = OrmaSpacing.PrimaryButtonHorizontalPadding,
                vertical = OrmaSpacing.PrimaryButtonVerticalPadding,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = OrmaColors.OnAccent,
        )
    }
}

@Composable
fun OrmaLightButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val iconKind = ormaButtonIconForText(text)
    Surface(
        modifier = modifier
            .height(52.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.Capsule,
        color = OrmaColors.CellBackground.copy(alpha = if (enabled) 1f else 0.54f),
        contentColor = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKind != null) {
                OrmaFlatIcon(
                    kind = iconKind,
                    modifier = Modifier.size(16.dp),
                    color = if (enabled) OrmaColors.IconPrimary else OrmaColors.IconDisabled,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OrmaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val iconKind = ormaButtonIconForText(text)
    Surface(
        modifier = modifier
            .height(52.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.CheckoutButton,
        color = OrmaColors.CellBackground.copy(alpha = if (enabled) 1f else 0.54f),
        contentColor = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKind != null) {
                OrmaFlatIcon(
                    kind = iconKind,
                    modifier = Modifier.size(16.dp),
                    color = if (enabled) OrmaColors.IconPrimary.copy(alpha = 0.70f) else OrmaColors.IconDisabled,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OrmaColors.TextPrimary.copy(alpha = 0.70f) else OrmaColors.TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun ormaButtonIconForText(text: String): OrmaFlatIconKind? {
    val normalized = text.trim().lowercase()
    return when {
        normalized in setOf("refresh", "syncing", "apply", "checking", "search") -> OrmaFlatIconKind.Refresh
        normalized in setOf("back", "owner") -> OrmaFlatIconKind.Back
        normalized == "previous" -> OrmaFlatIconKind.ChevronLeft
        normalized == "next" -> OrmaFlatIconKind.ChevronRight
        normalized == "clear" || normalized.startsWith("close") -> OrmaFlatIconKind.Close
        normalized.startsWith("add") || normalized.startsWith("new") || normalized.startsWith("create") -> OrmaFlatIconKind.Plus
        normalized.startsWith("edit") || normalized.startsWith("update") || normalized.startsWith("save") -> OrmaFlatIconKind.Edit
        normalized.startsWith("open") || normalized.startsWith("view") || normalized.startsWith("preview") -> OrmaFlatIconKind.View
        normalized.contains("print") -> OrmaFlatIconKind.Print
        normalized.contains("download") || normalized.contains("export") || normalized.contains("template") -> OrmaFlatIconKind.Download
        normalized.contains("upload") || normalized.contains("import") -> OrmaFlatIconKind.Upload
        normalized.contains("image") -> OrmaFlatIconKind.Image
        normalized.contains("stock") -> OrmaFlatIconKind.Stock
        normalized.contains("category") -> OrmaFlatIconKind.Category
        else -> null
    }
}

@Composable
fun OrmaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clip(OrmaShapes.Capsule)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun OrmaTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OrmaTextButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun OrmaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = label,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = OrmaShapes.Field,
            color = if (enabled) OrmaColors.CardBackground else OrmaColors.CellBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(
                0.8.dp,
                if (enabled) OrmaColors.Accent.copy(alpha = 0.12f) else OrmaColors.Hairline.copy(alpha = 0.70f),
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = if (singleLine) 13.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                leading?.invoke()
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    singleLine = singleLine,
                    minLines = minLines,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(OrmaColors.Accent),
                    textStyle = MaterialTheme.typography.bodyLarge.merge(
                        TextStyle(color = if (enabled) OrmaColors.TextPrimary else OrmaColors.TextDisabled),
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OrmaColors.TextTertiary,
                                    maxLines = if (singleLine) 1 else minLines,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                trailing?.invoke()
            }
        }
        supportingText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OrmaSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = if (dark) OrmaColors.DarkTextTertiary else OrmaColors.TextTertiary,
    )
}

@Composable
fun OrmaListRow(
    label: String,
    value: String? = null,
    modifier: Modifier = Modifier,
    sub: String? = null,
    dark: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.TextPrimary
    val secondaryColor = if (dark) OrmaColors.DarkTextSecondary else OrmaColors.TextSecondary
    val rowModifier = if (onClick != null) {
        modifier.clickable(enabled = enabled, onClick = onClick)
    } else {
        modifier
    }
    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) contentColor else OrmaColors.TextDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            sub?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else if (!value.isNullOrBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OrmaCleanList(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        content()
    }
}

@Composable
fun OrmaIndentedDivider(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = 20.dp),
        color = if (dark) OrmaColors.DarkHairline else OrmaColors.Divider,
    )
}

@Composable
fun OrmaPrice(
    amount: String,
    currency: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    Text(
        text = "$currency $amount",
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun OrmaStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tone: OrmaStatusTone = OrmaStatusTone.Neutral,
) {
    OrmaBadge(text = text, modifier = modifier, tone = tone)
}

@Composable
fun OrmaChoiceSurface(
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = OrmaShapes.StandardCell,
        color = if (selected) OrmaColors.CellBackground else OrmaColors.Accent.copy(alpha = 0.06f),
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(
            width = 0.8.dp,
            color = if (selected) OrmaColors.Accent.copy(alpha = 0.22f) else OrmaColors.Accent.copy(alpha = 0.08f),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(OrmaSpacing.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(OrmaSpacing.CompactHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectionDot(selected = selected)
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
            trailing?.invoke()
        }
    }
}

@Composable
fun OrmaBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: OrmaStatusTone = OrmaStatusTone.Neutral,
) {
    val colors = ormaStatusColors(tone)
    Surface(
        modifier = modifier,
        shape = OrmaShapes.Capsule,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(0.5.dp, colors.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = OrmaSpacing.BadgeHorizontalPadding,
                vertical = OrmaSpacing.BadgeVerticalPadding,
            ),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun OrmaSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = OrmaShapes.Skeleton,
    animated: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "OrmaSkeleton")
    val shimmerX by transition.animateFloat(
        initialValue = -320f,
        targetValue = 760f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = OrmaMotion.SkeletonMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "OrmaSkeletonShimmer",
    )
    val fillModifier = if (animated) {
        Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    OrmaColors.SkeletonBase,
                    OrmaColors.SkeletonHighlight,
                    OrmaColors.SkeletonBase,
                ),
                start = Offset(shimmerX, 0f),
                end = Offset(shimmerX + 320f, 0f),
            ),
        )
    } else {
        Modifier.background(OrmaColors.SkeletonBase)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(fillModifier),
    )
}

@Composable
private fun SelectionDot(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.18f)
    val fillColor = if (selected) OrmaColors.Accent else Color.Transparent

    Surface(
        modifier = modifier.size(18.dp),
        shape = OrmaShapes.Capsule,
        color = fillColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .background(OrmaColors.OnAccent, OrmaShapes.Capsule),
            )
        }
    }
}

@Composable
fun OrmaFieldLeadingText(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(54.dp),
        style = MaterialTheme.typography.labelLarge,
        color = OrmaColors.TextSecondary,
    )
}

fun phoneKeyboardOptions(): KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
