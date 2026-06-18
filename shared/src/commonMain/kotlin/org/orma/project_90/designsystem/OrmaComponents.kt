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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun OrmaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = OrmaShapes.SmallCard
    Surface(
        modifier = modifier
            .height(56.dp)
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = if (enabled) OrmaColors.Accent else OrmaColors.Accent.copy(alpha = 0.35f),
        contentColor = OrmaColors.ScreenBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
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
        contentColor = OrmaColors.ScreenBackground,
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
            color = Color.White,
        )
    }
}

@Composable
fun OrmaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OrmaColors.Accent.copy(alpha = 0.70f) else OrmaColors.TextDisabled,
            )
        }
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
            color = if (enabled) OrmaColors.Accent else OrmaColors.TextDisabled,
        )
    }
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
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = OrmaShapes.Field,
            color = OrmaColors.CellBackground,
            contentColor = OrmaColors.TextPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                        TextStyle(color = if (enabled) OrmaColors.Accent else OrmaColors.TextDisabled),
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OrmaColors.TextTertiary,
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
        )
    }
}
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
    val shimmerOffset by transition.animateFloat(
        initialValue = -280f,
        targetValue = 560f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = OrmaMotion.SkeletonMillis, easing = FastOutSlowInEasing),
        ),
        label = "OrmaSkeletonOffset",
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = OrmaMotion.SkeletonMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "OrmaSkeletonAlpha",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = if (animated) {
                    Brush.linearGradient(
                        colors = listOf(
                            OrmaColors.SkeletonBase,
                            OrmaColors.SkeletonHighlight,
                            OrmaColors.SkeletonBase,
                        ),
                        start = Offset(shimmerOffset - 240f, 0f),
                        end = Offset(shimmerOffset, 240f),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(OrmaColors.SkeletonBase, OrmaColors.SkeletonBase),
                    )
                },
                alpha = pulseAlpha,
            ),
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
                    .background(OrmaColors.ScreenBackground, OrmaShapes.Capsule),
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
