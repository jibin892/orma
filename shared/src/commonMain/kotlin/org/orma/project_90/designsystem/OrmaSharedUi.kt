package org.orma.project_90.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.orma.project_90.calendar.ormaCurrentIsoDate
import orma.shared.generated.resources.Res
import orma.shared.generated.resources.orma_icon_back
import orma.shared.generated.resources.orma_icon_calendar
import orma.shared.generated.resources.orma_icon_download
import orma.shared.generated.resources.orma_icon_edit
import orma.shared.generated.resources.orma_icon_filter
import orma.shared.generated.resources.orma_icon_image
import orma.shared.generated.resources.orma_icon_next
import orma.shared.generated.resources.orma_icon_print
import orma.shared.generated.resources.orma_icon_refresh
import orma.shared.generated.resources.orma_icon_stock
import orma.shared.generated.resources.orma_icon_view

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

@Composable
fun OrmaBrandMark(
    modifier: Modifier = Modifier,
    color: Color = OrmaColors.ScreenBackground,
) {
    Canvas(modifier = modifier) {
        val unit = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = color,
            radius = unit * 0.33f,
            center = center,
            style = Stroke(width = unit * 0.13f),
        )
        val leaf = Path().apply {
            moveTo(center.x, center.y - unit * 0.29f)
            cubicTo(
                center.x + unit * 0.20f,
                center.y - unit * 0.03f,
                center.x + unit * 0.18f,
                center.y + unit * 0.19f,
                center.x,
                center.y + unit * 0.31f,
            )
            cubicTo(
                center.x - unit * 0.18f,
                center.y + unit * 0.19f,
                center.x - unit * 0.20f,
                center.y - unit * 0.03f,
                center.x,
                center.y - unit * 0.29f,
            )
            close()
        }
        drawPath(path = leaf, color = color)
    }
}

enum class OrmaFlatIconKind {
    Refresh,
    Calendar,
    ChevronDown,
    ChevronLeft,
    ChevronRight,
    Close,
    Search,
    Plus,
    Edit,
    View,
    Image,
    Stock,
    Download,
    Upload,
    Print,
    Filter,
    Category,
    Profile,
    Back,
}

@Composable
fun OrmaFlatIcon(
    kind: OrmaFlatIconKind,
    modifier: Modifier = Modifier.size(18.dp),
    color: Color = OrmaColors.IconPrimary,
) {
    val resource = ormaFlatIconResource(kind)
    if (resource != null) {
        Icon(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = modifier,
            tint = color,
        )
    } else {
        Canvas(modifier = modifier) {
            drawOrmaFlatIcon(kind = kind, color = color)
        }
    }
}

private fun ormaFlatIconResource(kind: OrmaFlatIconKind): DrawableResource? = when (kind) {
    OrmaFlatIconKind.Refresh -> Res.drawable.orma_icon_refresh
    OrmaFlatIconKind.Calendar -> Res.drawable.orma_icon_calendar
    OrmaFlatIconKind.ChevronLeft,
    OrmaFlatIconKind.Back -> Res.drawable.orma_icon_back
    OrmaFlatIconKind.ChevronRight -> Res.drawable.orma_icon_next
    OrmaFlatIconKind.Edit -> Res.drawable.orma_icon_edit
    OrmaFlatIconKind.View -> Res.drawable.orma_icon_view
    OrmaFlatIconKind.Image -> Res.drawable.orma_icon_image
    OrmaFlatIconKind.Stock -> Res.drawable.orma_icon_stock
    OrmaFlatIconKind.Download -> Res.drawable.orma_icon_download
    OrmaFlatIconKind.Print -> Res.drawable.orma_icon_print
    OrmaFlatIconKind.Filter -> Res.drawable.orma_icon_filter
    else -> null
}

@Composable
fun OrmaChevronDownIcon(
    modifier: Modifier = Modifier.size(16.dp),
    color: Color = OrmaColors.TextSecondary,
) {
    OrmaFlatIcon(
        kind = OrmaFlatIconKind.ChevronDown,
        modifier = modifier,
        color = color,
    )
}

@Composable
fun OrmaCloseIcon(
    modifier: Modifier = Modifier.size(18.dp),
    color: Color = OrmaColors.IconPrimary,
) {
    OrmaFlatIcon(
        kind = OrmaFlatIconKind.Close,
        modifier = modifier,
        color = color,
    )
}

@Composable
fun OrmaUploadImageIcon(
    modifier: Modifier = Modifier.size(34.dp),
    color: Color = OrmaColors.IconPrimary,
) {
    OrmaFlatIcon(
        kind = OrmaFlatIconKind.Upload,
        modifier = modifier,
        color = color,
    )
}

private fun DrawScope.drawOrmaFlatIcon(
    kind: OrmaFlatIconKind,
    color: Color,
) {
    val width = size.width
    val height = size.height
    val unit = size.minDimension
    val strokeWidth = (unit * 0.12f).coerceAtLeast(1.6f)
    val stroke = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    when (kind) {
        OrmaFlatIconKind.Refresh -> {
            val arcBounds = Size(width * 0.64f, height * 0.64f)
            val arcTopLeft = Offset(width * 0.18f, height * 0.18f)
            drawArc(
                color = color,
                startAngle = 42f,
                sweepAngle = 286f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcBounds,
                style = stroke,
            )
            drawLine(
                color = color,
                start = Offset(width * 0.79f, height * 0.18f),
                end = Offset(width * 0.86f, height * 0.36f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(width * 0.79f, height * 0.18f),
                end = Offset(width * 0.60f, height * 0.23f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawArc(
                color = color.copy(alpha = 0.42f),
                startAngle = 222f,
                sweepAngle = 74f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcBounds,
                style = stroke,
            )
        }
        OrmaFlatIconKind.Calendar -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.16f, height * 0.20f),
                size = Size(width * 0.68f, height * 0.64f),
                cornerRadius = CornerRadius(unit * 0.12f, unit * 0.12f),
                style = stroke,
            )
            drawRoundRect(
                color = color.copy(alpha = 0.18f),
                topLeft = Offset(width * 0.16f, height * 0.30f),
                size = Size(width * 0.68f, height * 0.18f),
                cornerRadius = CornerRadius(unit * 0.06f, unit * 0.06f),
            )
            drawLine(color, Offset(width * 0.32f, height * 0.14f), Offset(width * 0.32f, height * 0.30f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.68f, height * 0.14f), Offset(width * 0.68f, height * 0.30f), strokeWidth, StrokeCap.Round)
            drawCircle(color = color, radius = unit * 0.035f, center = Offset(width * 0.36f, height * 0.60f))
            drawCircle(color = color, radius = unit * 0.035f, center = Offset(width * 0.50f, height * 0.60f))
            drawCircle(color = color, radius = unit * 0.035f, center = Offset(width * 0.64f, height * 0.60f))
        }
        OrmaFlatIconKind.ChevronDown -> {
            val arrow = Path().apply {
                moveTo(width * 0.22f, height * 0.36f)
                lineTo(width * 0.50f, height * 0.66f)
                lineTo(width * 0.78f, height * 0.36f)
                lineTo(width * 0.68f, height * 0.28f)
                lineTo(width * 0.50f, height * 0.48f)
                lineTo(width * 0.32f, height * 0.28f)
                close()
            }
            drawPath(path = arrow, color = color)
        }
        OrmaFlatIconKind.ChevronLeft -> {
            drawLine(color, Offset(width * 0.62f, height * 0.22f), Offset(width * 0.38f, height * 0.50f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.38f, height * 0.50f), Offset(width * 0.62f, height * 0.78f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.ChevronRight -> {
            drawLine(color, Offset(width * 0.38f, height * 0.22f), Offset(width * 0.62f, height * 0.50f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.62f, height * 0.50f), Offset(width * 0.38f, height * 0.78f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Close -> {
            drawLine(color, Offset(width * 0.30f, height * 0.30f), Offset(width * 0.70f, height * 0.70f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.70f, height * 0.30f), Offset(width * 0.30f, height * 0.70f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Search -> {
            drawCircle(
                color = color,
                radius = unit * 0.25f,
                center = Offset(width * 0.44f, height * 0.42f),
                style = stroke,
            )
            drawLine(color, Offset(width * 0.62f, height * 0.62f), Offset(width * 0.80f, height * 0.80f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Plus -> {
            drawLine(color, Offset(width * 0.50f, height * 0.24f), Offset(width * 0.50f, height * 0.76f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.24f, height * 0.50f), Offset(width * 0.76f, height * 0.50f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Edit -> {
            drawLine(color, Offset(width * 0.27f, height * 0.72f), Offset(width * 0.68f, height * 0.31f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.62f, height * 0.25f), Offset(width * 0.75f, height * 0.38f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.24f, height * 0.78f), Offset(width * 0.40f, height * 0.74f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.View -> {
            val eye = Path().apply {
                moveTo(width * 0.12f, height * 0.50f)
                cubicTo(width * 0.30f, height * 0.25f, width * 0.70f, height * 0.25f, width * 0.88f, height * 0.50f)
                cubicTo(width * 0.70f, height * 0.75f, width * 0.30f, height * 0.75f, width * 0.12f, height * 0.50f)
                close()
            }
            drawPath(path = eye, color = color.copy(alpha = 0.20f))
            drawPath(path = eye, color = color, style = stroke)
            drawCircle(color = color, radius = unit * 0.10f, center = Offset(width * 0.50f, height * 0.50f))
        }
        OrmaFlatIconKind.Image -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.16f, height * 0.22f),
                size = Size(width * 0.68f, height * 0.58f),
                cornerRadius = CornerRadius(unit * 0.10f, unit * 0.10f),
                style = stroke,
            )
            drawCircle(color = color, radius = unit * 0.045f, center = Offset(width * 0.36f, height * 0.40f))
            val mountain = Path().apply {
                moveTo(width * 0.24f, height * 0.68f)
                lineTo(width * 0.43f, height * 0.52f)
                lineTo(width * 0.55f, height * 0.63f)
                lineTo(width * 0.66f, height * 0.50f)
                lineTo(width * 0.79f, height * 0.68f)
            }
            drawPath(path = mountain, color = color, style = stroke)
        }
        OrmaFlatIconKind.Stock -> {
            val boxPath = Path().apply {
                moveTo(width * 0.20f, height * 0.36f)
                lineTo(width * 0.50f, height * 0.20f)
                lineTo(width * 0.80f, height * 0.36f)
                lineTo(width * 0.80f, height * 0.68f)
                lineTo(width * 0.50f, height * 0.84f)
                lineTo(width * 0.20f, height * 0.68f)
                close()
            }
            drawPath(path = boxPath, color = color.copy(alpha = 0.18f))
            drawPath(path = boxPath, color = color, style = stroke)
            drawLine(color, Offset(width * 0.50f, height * 0.20f), Offset(width * 0.50f, height * 0.52f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Download,
        OrmaFlatIconKind.Upload -> {
            val up = kind == OrmaFlatIconKind.Upload
            val arrowStartY = if (up) height * 0.76f else height * 0.22f
            val arrowEndY = if (up) height * 0.28f else height * 0.70f
            drawLine(color, Offset(width * 0.50f, arrowStartY), Offset(width * 0.50f, arrowEndY), strokeWidth, StrokeCap.Round)
            val arrowTipY = arrowEndY
            val wingY = if (up) arrowTipY + height * 0.16f else arrowTipY - height * 0.16f
            drawLine(color, Offset(width * 0.50f, arrowTipY), Offset(width * 0.34f, wingY), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.50f, arrowTipY), Offset(width * 0.66f, wingY), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.26f, height * 0.82f), Offset(width * 0.74f, height * 0.82f), strokeWidth, StrokeCap.Round)
        }
        OrmaFlatIconKind.Print -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.23f, height * 0.15f),
                size = Size(width * 0.54f, height * 0.24f),
                cornerRadius = CornerRadius(unit * 0.06f, unit * 0.06f),
                style = stroke,
            )
            drawRoundRect(
                color = color.copy(alpha = 0.18f),
                topLeft = Offset(width * 0.15f, height * 0.34f),
                size = Size(width * 0.70f, height * 0.34f),
                cornerRadius = CornerRadius(unit * 0.10f, unit * 0.10f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.15f, height * 0.34f),
                size = Size(width * 0.70f, height * 0.34f),
                cornerRadius = CornerRadius(unit * 0.10f, unit * 0.10f),
                style = stroke,
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.28f, height * 0.58f),
                size = Size(width * 0.44f, height * 0.26f),
                cornerRadius = CornerRadius(unit * 0.05f, unit * 0.05f),
                style = stroke,
            )
            drawCircle(color = color, radius = unit * 0.035f, center = Offset(width * 0.72f, height * 0.46f))
        }
        OrmaFlatIconKind.Filter -> {
            val funnel = Path().apply {
                moveTo(width * 0.18f, height * 0.24f)
                lineTo(width * 0.82f, height * 0.24f)
                lineTo(width * 0.58f, height * 0.52f)
                lineTo(width * 0.58f, height * 0.76f)
                lineTo(width * 0.42f, height * 0.84f)
                lineTo(width * 0.42f, height * 0.52f)
                close()
            }
            drawPath(path = funnel, color = color.copy(alpha = 0.18f))
            drawPath(path = funnel, color = color, style = stroke)
            drawLine(color, Offset(width * 0.28f, height * 0.38f), Offset(width * 0.72f, height * 0.38f), strokeWidth * 0.72f, StrokeCap.Round)
        }
        OrmaFlatIconKind.Category -> {
            val box = unit * 0.22f
            listOf(
                Offset(width * 0.22f, height * 0.22f),
                Offset(width * 0.56f, height * 0.22f),
                Offset(width * 0.22f, height * 0.56f),
                Offset(width * 0.56f, height * 0.56f),
            ).forEach { topLeft ->
                drawRoundRect(
                    color = color.copy(alpha = 0.22f),
                    topLeft = topLeft,
                    size = Size(box, box),
                    cornerRadius = CornerRadius(unit * 0.06f, unit * 0.06f),
                )
                drawRoundRect(
                    color = color,
                    topLeft = topLeft,
                    size = Size(box, box),
                    cornerRadius = CornerRadius(unit * 0.06f, unit * 0.06f),
                    style = Stroke(width = strokeWidth * 0.72f),
                )
            }
        }
        OrmaFlatIconKind.Profile -> {
            drawCircle(
                color = color,
                radius = unit * 0.15f,
                center = Offset(width * 0.50f, height * 0.36f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(width * 0.22f, height * 0.58f),
                size = Size(width * 0.56f, height * 0.24f),
                cornerRadius = CornerRadius(unit * 0.17f, unit * 0.17f),
            )
        }
        OrmaFlatIconKind.Back -> {
            drawLine(color, Offset(width * 0.72f, height * 0.50f), Offset(width * 0.26f, height * 0.50f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.26f, height * 0.50f), Offset(width * 0.46f, height * 0.30f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(width * 0.26f, height * 0.50f), Offset(width * 0.46f, height * 0.70f), strokeWidth, StrokeCap.Round)
        }
    }
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
fun OrmaScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OrmaTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = OrmaColors.ScreenBackground,
            contentColor = OrmaColors.TextPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = content,
        )
    }
}

@Composable
fun OrmaMobileScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OrmaScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = OrmaSpacing.ScreenPadding)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
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
            .padding(horizontal = OrmaSpacing.ScreenPadding)
            .padding(top = 16.dp, bottom = 16.dp),
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
fun OrmaWebDesktopShell(
    rail: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    OrmaWideShell(
        rail = rail,
        content = content,
        modifier = modifier,
    )
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
            .padding(22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Box(
            modifier = Modifier
                .width(304.dp)
                .fillMaxHeight(),
        ) {
            rail()
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = OrmaShapes.StandardCell,
            color = OrmaColors.ScreenBackground,
            contentColor = OrmaColors.TextPrimary,
            border = BorderStroke(0.8.dp, OrmaColors.Divider),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(30.dp),
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
                contentColor = OrmaColors.OnAccent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    OrmaBrandMark(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(7.dp),
                        color = OrmaColors.OnAccent,
                    )
                }
            }
            Text(
                text = "ORMA",
                style = MaterialTheme.typography.titleMedium,
                color = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.TextPrimary,
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
        contentColor = if (dark) OrmaColors.DarkTextPrimary else OrmaColors.TextPrimary,
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
            color = OrmaColors.TextPrimary,
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
                contentColor = if (active) OrmaColors.OnAccent else OrmaColors.Accent,
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
fun OrmaCalendarDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Choose date",
    supportingText: String? = null,
    allowClear: Boolean = true,
    minDate: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var visibleMonth by remember(value) {
        mutableStateOf(ormaCalendarMonthFrom(value).ifBlank { ormaCurrentIsoDate().take(7) })
    }
    val selectedDate = value.take(10).takeIf(::ormaIsIsoDate)
    val minimumDate = minDate?.take(10)?.takeIf(::ormaIsIsoDate)
    val fieldTone = if (selectedDate == null) OrmaColors.TextPrimary.copy(alpha = 0.72f) else OrmaColors.Accent
    var anchorWidthPx by remember { mutableStateOf(0) }
    var anchorHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .onGloballyPositioned { coordinates ->
                        anchorWidthPx = coordinates.size.width
                        anchorHeightPx = coordinates.size.height
                    },
                shape = OrmaShapes.Field,
                color = OrmaColors.CellBackground,
                contentColor = OrmaColors.TextPrimary,
                border = BorderStroke(1.dp, OrmaColors.Accent.copy(alpha = if (selectedDate == null) 0.08f else 0.14f)),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrmaFlatIcon(
                        kind = OrmaFlatIconKind.Calendar,
                        modifier = Modifier.size(18.dp),
                        color = fieldTone,
                    )
                    Text(
                        text = selectedDate?.let(::ormaDateDisplayLabel) ?: placeholder,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedDate == null) OrmaColors.TextSecondary else OrmaColors.Accent,
                    )
                    OrmaChevronDownIcon(
                        modifier = Modifier.size(18.dp),
                        color = fieldTone,
                    )
                }
            }
            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(
                        x = 0,
                        y = anchorHeightPx + with(density) { 8.dp.roundToPx() },
                    ),
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    val pickerWidth = if (anchorWidthPx > 0) with(density) { anchorWidthPx.toDp() } else 280.dp
                    OrmaCalendarMonthPicker(
                        visibleMonth = visibleMonth,
                        selectedDate = selectedDate,
                        minDate = minimumDate,
                        onMonthChange = { visibleMonth = it },
                        onSelected = {
                            onValueChange(it)
                            expanded = false
                        },
                        onClear = if (allowClear) {
                            {
                                onValueChange("")
                                expanded = false
                            }
                        } else {
                            null
                        },
                        modifier = Modifier.width(pickerWidth),
                    )
                }
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
fun OrmaCalendarDateTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Choose date",
    supportingText: String? = null,
    allowClear: Boolean = true,
    disablePastDates: Boolean = true,
) {
    val date = value.take(10).takeIf(::ormaIsIsoDate).orEmpty()
    val time = value.substringAfter(" ", "").take(5).takeIf(::ormaIsTime).orEmpty()
    val minDate = if (disablePastDates) ormaCurrentIsoDate() else null
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OrmaCalendarDateField(
            value = date,
            onValueChange = { nextDate ->
                onValueChange(ormaJoinDateTime(nextDate, time))
            },
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            allowClear = allowClear,
            minDate = minDate,
        )
        if (date.isNotBlank()) {
            OrmaSegmentedRow(
                options = listOf("", "09:00", "12:00", "15:00", "18:00"),
                selected = time,
                label = { it.ifBlank { "Any time" } },
                onSelected = { nextTime -> onValueChange(ormaJoinDateTime(date, nextTime)) },
            )
        }
    }
}

@Composable
private fun OrmaCalendarMonthPicker(
    visibleMonth: String,
    selectedDate: String?,
    minDate: String?,
    onMonthChange: (String) -> Unit,
    onSelected: (String) -> Unit,
    onClear: (() -> Unit)?,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val year = visibleMonth.substringBefore("-").toIntOrNull() ?: 2026
    val month = visibleMonth.substringAfter("-", "06").toIntOrNull()?.coerceIn(1, 12) ?: 6
    val visibleMonthKey = "$year-${month.twoDigit()}"
    val minMonthKey = minDate?.take(7)
    val canNavigatePrevious = minMonthKey == null || visibleMonthKey > minMonthKey
    val days = ormaDaysInMonth(year, month)
    val leadingEmptyCells = ormaFirstWeekdayOffset(year, month)
    Surface(
        modifier = modifier,
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrmaCalendarArrowButton(
                    kind = OrmaFlatIconKind.ChevronLeft,
                    onClick = { onMonthChange(ormaShiftMonth(year, month, -1)) },
                    modifier = Modifier.width(48.dp),
                    enabled = canNavigatePrevious,
                )
                Text(
                    text = "${ormaMonthName(month)} $year",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = OrmaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                OrmaCalendarArrowButton(
                    kind = OrmaFlatIconKind.ChevronRight,
                    onClick = { onMonthChange(ormaShiftMonth(year, month, 1)) },
                    modifier = Modifier.width(48.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = OrmaColors.TextTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            val cells = List(leadingEmptyCells) { 0 } + (1..days).toList()
            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    (week + List(7 - week.size) { 0 }).forEach { day ->
                        if (day == 0) {
                            Spacer(modifier = Modifier.weight(1f).height(40.dp))
                        } else {
                            val isoDate = "$year-${month.twoDigit()}-${day.twoDigit()}"
                            val active = isoDate == selectedDate
                            val disabled = minDate != null && isoDate < minDate
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable(enabled = !disabled) { onSelected(isoDate) },
                                shape = OrmaShapes.SmallCard,
                                color = when {
                                    active && !disabled -> OrmaColors.Accent
                                    disabled -> OrmaColors.CellBackground.copy(alpha = 0.48f)
                                    else -> OrmaColors.CellBackground
                                },
                                contentColor = when {
                                    active && !disabled -> OrmaColors.OnAccent
                                    disabled -> OrmaColors.TextDisabled
                                    else -> OrmaColors.TextPrimary
                                },
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when {
                                            active && !disabled -> OrmaColors.OnAccent
                                            disabled -> OrmaColors.TextDisabled
                                            else -> OrmaColors.TextPrimary
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (onClear != null) {
                OrmaTextButton(
                    text = "Clear date",
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OrmaCalendarArrowButton(
    kind: OrmaFlatIconKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = OrmaShapes.SmallCard,
        color = if (enabled) OrmaColors.CellBackground else OrmaColors.CellBackground.copy(alpha = 0.48f),
        contentColor = if (enabled) OrmaColors.Accent else OrmaColors.TextDisabled,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OrmaFlatIcon(
                kind = kind,
                modifier = Modifier.size(18.dp),
                color = if (enabled) OrmaColors.IconPrimary else OrmaColors.TextDisabled,
            )
        }
    }
}

private fun ormaCalendarMonthFrom(value: String): String =
    value.take(10).takeIf(::ormaIsIsoDate)?.take(7).orEmpty()

private fun ormaJoinDateTime(date: String, time: String): String =
    when {
        date.isBlank() -> ""
        time.isBlank() -> date
        else -> "$date $time"
    }

private fun ormaDateDisplayLabel(value: String): String {
    val parts = value.split("-")
    val year = parts.getOrNull(0).orEmpty()
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return value
    val day = parts.getOrNull(2).orEmpty()
    return "$day ${ormaMonthName(month).take(3)} $year"
}

private fun ormaIsIsoDate(value: String): Boolean {
    val parts = value.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: return false
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return false
    val day = parts.getOrNull(2)?.toIntOrNull() ?: return false
    return year in 1900..2200 && month in 1..12 && day in 1..ormaDaysInMonth(year, month)
}

private fun ormaIsTime(value: String): Boolean {
    val hour = value.substringBefore(":").toIntOrNull() ?: return false
    val minute = value.substringAfter(":", "").toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

private fun ormaDaysInMonth(year: Int, month: Int): Int =
    when (month) {
        2 -> if (ormaIsLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun ormaIsLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun ormaFirstWeekdayOffset(year: Int, month: Int): Int {
    val shiftedMonth = if (month < 3) month + 12 else month
    val shiftedYear = if (month < 3) year - 1 else year
    val k = shiftedYear % 100
    val j = shiftedYear / 100
    val h = (1 + ((13 * (shiftedMonth + 1)) / 5) + k + (k / 4) + (j / 4) + (5 * j)) % 7
    val sundayZero = (h + 6) % 7
    return (sundayZero + 6) % 7
}

private fun ormaShiftMonth(year: Int, month: Int, delta: Int): String {
    val zeroBased = (year * 12) + (month - 1) + delta
    val nextYear = if (zeroBased >= 0) zeroBased / 12 else (zeroBased - 11) / 12
    val nextMonth = ((zeroBased % 12) + 12) % 12 + 1
    return "$nextYear-${nextMonth.twoDigit()}"
}

private fun ormaMonthName(month: Int): String =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    ).getOrElse(month - 1) { "Month" }

private fun Int.twoDigit(): String = toString().padStart(2, '0')

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
                        color = when {
                            dark -> OrmaColors.DarkTextPrimary
                            row.second.ormaLooksLikeAmountText() -> OrmaColors.Accent
                            else -> OrmaColors.TextPrimary
                        },
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

private fun String.ormaLooksLikeAmountText(): Boolean =
    Regex("\\b(?:INR|AED|USD|EUR|GBP|SAR|QAR|KWD|OMR|BHD)\\b").containsMatchIn(this)

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
                contentColor = if (selected) OrmaColors.OnAccent else OrmaColors.Accent,
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
                contentColor = if (item.second) OrmaColors.OnAccent else OrmaColors.Accent,
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
                    contentColor = if (step.active) OrmaColors.TextPrimary else OrmaColors.DarkTextPrimary,
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
