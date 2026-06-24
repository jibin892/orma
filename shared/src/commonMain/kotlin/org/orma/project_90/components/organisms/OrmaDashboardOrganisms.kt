package org.orma.project_90.components.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import kotlin.math.abs
import kotlin.math.roundToInt
import org.orma.project_90.backend.OrmaDashboardActivity
import org.orma.project_90.backend.OrmaDashboardBreakdown
import org.orma.project_90.components.atoms.OrmaDashboardMetric
import org.orma.project_90.backend.OrmaDashboardNotificationPreview
import org.orma.project_90.backend.OrmaDashboardRevenuePoint
import org.orma.project_90.backend.OrmaDashboardTask
import org.orma.project_90.backend.OrmaDashboardTopItem
import org.orma.project_90.components.atoms.OrmaDashboardEmptyState
import org.orma.project_90.components.atoms.OrmaDashboardIconBubble
import org.orma.project_90.components.atoms.OrmaKpiCard
import org.orma.project_90.components.molecules.OrmaDashboardPanel
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaSecondaryButton
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.ormaStatusColors

/**
 * Responsive dashboard KPI grid.
 *
 * Mobile: renders a readable single column to avoid clipped values.
 * Desktop/web: renders one row of evenly weighted cards.
 */
@Composable
fun OrmaDashboardStatsGrid(
    metrics: List<OrmaDashboardMetric>,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    if (wide) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            metrics.forEach { metric ->
                OrmaKpiCard(
                    metric = metric,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            metrics.forEach { metric ->
                OrmaKpiCard(metric = metric)
            }
        }
    }
}

/**
 * Chart-ready revenue card. It renders only real series values and shows an empty
 * state when the backend has no paid activity yet.
 */
@Composable
fun OrmaDashboardRevenueCard(
    series: List<OrmaDashboardRevenuePoint>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val maxAmount = series.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0
    OrmaDashboardPanel(
        title = "Revenue overview",
        body = "Paid amount from recent workspace activity.",
        modifier = modifier,
    ) {
        if (series.isEmpty() || maxAmount <= 0.0) {
            OrmaDashboardEmptyState(
                title = "No paid revenue yet",
                body = "Paid orders and completed bookings will build this chart automatically.",
            )
        } else {
            OrmaDashboardRevenueChart(
                series = series,
                currency = currency,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "7 days",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                )
                Text(
                    text = "$currency ${maxAmount.toMoneyLabel()} peak",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
fun OrmaDashboardRevenueChart(
    series: List<OrmaDashboardRevenuePoint>,
    currency: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 184.dp,
) {
    val values = remember(series) {
        series.map { point -> (point.amount.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0) }
    }
    val xValues = remember(values) { values.indices.map { it } }
    val maxAmount = values.maxOrNull() ?: 0.0
    val bottomLabels = remember(series) { series.map { it.date.shortChartDateLabel() } }
    val modelProducer = remember { CartesianChartModelProducer() }
    val rangeProvider = remember(maxAmount) {
        CartesianLayerRangeProvider.fixed(
            minY = 0.0,
            maxY = (maxAmount * 1.14).coerceAtLeast(1.0),
        )
    }

    LaunchedEffect(values) {
        if (values.isNotEmpty()) {
            modelProducer.runTransaction {
                lineModel { series(x = xValues, y = values) }
            }
        }
    }

    val lineColor = Color(0xFF9E7DE3)
    val gridLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.075f)),
        thickness = 1.dp,
    )
    val baselineLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.16f)),
        thickness = 1.dp,
    )
    val revenueLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(
            thickness = 2.5.dp,
            cap = StrokeCap.Round,
        ),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(
                        lineColor.copy(alpha = 0.50f),
                        lineColor.copy(alpha = 0.24f),
                        lineColor.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                ),
            ),
        ),
        interpolator = LineCartesianLayer.Interpolator.catmullRom(),
    )
    val startAxisValueFormatter = remember(currency) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): CharSequence = value.compactChartAmount(currency)
        }
    }
    val bottomAxisValueFormatter = remember(bottomLabels) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): CharSequence {
                val index = value.roundToInt()
                return bottomLabels.getOrNull(index).orEmpty()
            }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(revenueLine),
                rangeProvider = rangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                label = null,
                valueFormatter = startAxisValueFormatter,
                tick = null,
                guideline = gridLine,
                itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 5 }, shiftTopLines = false) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = baselineLine,
                label = null,
                tick = null,
                guideline = null,
                valueFormatter = bottomAxisValueFormatter,
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(chartHeight)
            .semantics {
                contentDescription = "Revenue chart for ${series.size} days. Highest value $currency ${maxAmount.toMoneyLabel()}."
            },
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
fun OrmaDashboardCustomerMovementChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 156.dp,
) {
    val chartValues = remember(values) { values.map { it.coerceAtLeast(0.0) } }
    val xValues = remember(chartValues) { chartValues.indices.map { it } }
    val maxValue = chartValues.maxOrNull() ?: 0.0
    val modelProducer = remember { CartesianChartModelProducer() }
    val rangeProvider = remember(maxValue) {
        CartesianLayerRangeProvider.fixed(
            minY = 0.0,
            maxY = (maxValue * 1.22).coerceAtLeast(1.0),
        )
    }

    LaunchedEffect(chartValues) {
        if (chartValues.isNotEmpty()) {
            modelProducer.runTransaction {
                lineModel { series(x = xValues, y = chartValues) }
            }
        }
    }

    val lineColor = OrmaColors.Success
    val gridLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.065f)),
        thickness = 1.dp,
    )
    val baselineLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.14f)),
        thickness = 1.dp,
    )
    val movementLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(
            thickness = 2.4.dp,
            cap = StrokeCap.Round,
        ),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(
                        lineColor.copy(alpha = 0.34f),
                        lineColor.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                ),
            ),
        ),
        interpolator = LineCartesianLayer.Interpolator.catmullRom(),
    )
    val bottomAxisValueFormatter = remember(labels) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): CharSequence {
                val index = value.roundToInt()
                return labels.getOrNull(index).orEmpty()
            }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(movementLine),
                rangeProvider = rangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                label = null,
                tick = null,
                guideline = gridLine,
                itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 4 }, shiftTopLines = false) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = baselineLine,
                label = null,
                tick = null,
                guideline = null,
                valueFormatter = bottomAxisValueFormatter,
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(chartHeight)
            .semantics {
                contentDescription = "Customer movement chart for ${chartValues.size} periods. Highest value ${maxValue.roundToInt()} customers."
            },
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
fun OrmaDashboardInventoryRiskChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 156.dp,
) {
    val chartValues = remember(values) { values.map { it.coerceAtLeast(0.0) } }
    val maxValue = chartValues.maxOrNull() ?: 0.0
    val modelProducer = remember { CartesianChartModelProducer() }
    val rangeProvider = remember(maxValue) {
        CartesianLayerRangeProvider.fixed(
            minY = 0.0,
            maxY = (maxValue * 1.24).coerceAtLeast(1.0),
        )
    }

    LaunchedEffect(chartValues) {
        if (chartValues.isNotEmpty()) {
            modelProducer.runTransaction {
                columnModel { series(chartValues) }
            }
        }
    }

    val column = rememberLineComponent(
        fill = Fill(OrmaColors.Warning.copy(alpha = 0.86f)),
        thickness = 14.dp,
    )
    val gridLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.065f)),
        thickness = 1.dp,
    )
    val baselineLine = rememberLineComponent(
        fill = Fill(OrmaColors.Accent.copy(alpha = 0.14f)),
        thickness = 1.dp,
    )
    val bottomAxisValueFormatter = remember(labels) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): CharSequence {
                val index = value.roundToInt()
                return labels.getOrNull(index).orEmpty()
            }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(column),
                rangeProvider = rangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                label = null,
                tick = null,
                guideline = gridLine,
                itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 4 }, shiftTopLines = false) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = baselineLine,
                label = null,
                tick = null,
                guideline = null,
                valueFormatter = bottomAxisValueFormatter,
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(chartHeight)
            .semantics {
                contentDescription = "Inventory risk chart for ${chartValues.size} stock groups. Highest group ${maxValue.roundToInt()} items."
            },
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

private fun Double.compactChartAmount(currency: String): String {
    val absValue = abs(this)
    val sign = if (this < 0) "-" else ""
    val amount = when {
        absValue >= 1_000_000.0 -> "$sign${(absValue / 1_000_000.0).toOneDecimal()}M"
        absValue >= 1_000.0 -> "$sign${(absValue / 1_000.0).toOneDecimal()}K"
        else -> "$sign${absValue.roundToInt()}"
    }
    return currency.ifBlank { "INR" } + " " + amount
}

private fun Double.toOneDecimal(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    val whole = rounded.toLong()
    return if (rounded == whole.toDouble()) whole.toString() else rounded.toString()
}

private fun String.shortChartDateLabel(): String =
    if (length >= 10 && this[4] == '-' && this[7] == '-') {
        substring(5, 7) + "/" + substring(8, 10)
    } else {
        take(5)
    }

@Composable
fun OrmaDashboardTasksPanel(
    tasks: List<OrmaDashboardTask>,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OrmaDashboardPanel(
        title = "Tasks",
        body = "Actions derived from real workspace signals.",
        modifier = modifier,
    ) {
        if (tasks.isEmpty()) {
            OrmaDashboardEmptyState(
                title = "No urgent tasks",
                body = "Orders, stock, setup, and channel tasks appear here when attention is needed.",
            )
        } else {
            tasks.forEachIndexed { index, task ->
                OrmaDashboardTaskRow(task = task, onAction = onAction)
                if (index != tasks.lastIndex) HorizontalDivider(color = OrmaColors.Divider)
            }
        }
    }
}

@Composable
private fun OrmaDashboardTaskRow(
    task: OrmaDashboardTask,
    onAction: (String) -> Unit,
) {
    val tone = task.tone.toOrmaTone()
    val colors = ormaStatusColors(tone)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(10.dp).padding(top = 6.dp),
            shape = OrmaShapes.Capsule,
            color = colors.content,
            contentColor = colors.content,
        ) {}
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.count > 0) {
                    OrmaBadge(text = task.count.toString(), tone = tone)
                }
            }
            Text(
                text = task.body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OrmaSecondaryButton(
                text = task.action.readableActionLabel(),
                onClick = { onAction(task.action) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun OrmaDashboardActivityPanel(
    activities: List<OrmaDashboardActivity>,
    modifier: Modifier = Modifier,
) {
    OrmaDashboardPanel(
        title = "Recent activity",
        body = "Latest real orders, bookings, and notifications.",
        modifier = modifier,
    ) {
        if (activities.isEmpty()) {
            OrmaDashboardEmptyState(
                title = "No activity yet",
                body = "New orders, bookings, stock alerts, and notifications will appear here.",
            )
        } else {
            activities.forEachIndexed { index, activity ->
                OrmaDashboardActivityRow(activity = activity)
                if (index != activities.lastIndex) HorizontalDivider(color = OrmaColors.Divider)
            }
        }
    }
}

@Composable
private fun OrmaDashboardActivityRow(activity: OrmaDashboardActivity) {
    val tone = activity.tone.toOrmaTone()
    val colors = ormaStatusColors(tone)
    val performedBy = activity.performedByLabel()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = listOfNotNull(activity.title, activity.body, performedBy?.let { "Performed by $it" })
                    .joinToString(". ")
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        OrmaDashboardIconBubble(modifier = Modifier.size(34.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .semantics { contentDescription = activity.type },
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = activity.body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (performedBy != null) {
                Text(
                    text = "by $performedBy",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrmaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = activity.occurredAt.shortDateTimeLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = OrmaColors.TextTertiary,
                maxLines = 1,
            )
        }
        Surface(
            modifier = Modifier.size(9.dp).padding(top = 5.dp),
            shape = OrmaShapes.Capsule,
            color = colors.content,
            contentColor = colors.content,
        ) {}
    }
}

private fun OrmaDashboardActivity.performedByLabel(): String? {
    val person = performedByDisplayName?.takeIf { it.isNotBlank() }
        ?: performedByEmail?.takeIf { it.isNotBlank() }
        ?: performedByPhoneNumber?.takeIf { it.isNotBlank() }
    val role = performedByRole?.toActivityRoleLabel()
    return when {
        person != null && role != null && role != "Public catalog" -> "$person · $role"
        person != null && role == "Public catalog" -> "$person via public catalog"
        person != null -> person
        role != null -> role
        else -> null
    }
}

private fun String.toActivityRoleLabel(): String? =
    trim()
        .lowercase()
        .replace("-", "_")
        .takeIf { it.isNotBlank() }
        ?.split("_")
        ?.filter { it.isNotBlank() }
        ?.joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

@Composable
fun OrmaDashboardBreakdownPanel(
    title: String,
    body: String,
    items: List<OrmaDashboardBreakdown>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.count }.coerceAtLeast(1)
    OrmaDashboardPanel(
        title = title,
        body = body,
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            OrmaDashboardEmptyState(
                title = "No records yet",
                body = "This breakdown appears after the first matching order.",
            )
        } else {
            items.forEach { item ->
                val ratio = item.count.toFloat() / total.toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = OrmaColors.TextPrimary,
                        )
                        Text(
                            text = "${item.count} / $currency ${item.amount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrmaColors.TextSecondary,
                        )
                    }
                    Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                        drawRoundRect(
                            color = OrmaColors.CellBackground,
                            size = size,
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                        drawRoundRect(
                            color = OrmaColors.Accent.copy(alpha = 0.72f),
                            size = Size(size.width * ratio.coerceIn(0.04f, 1f), size.height),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrmaDashboardTopItemsPanel(
    items: List<OrmaDashboardTopItem>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    OrmaDashboardPanel(
        title = "Top items",
        body = "Best performing products, services, and appointments.",
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            OrmaDashboardEmptyState(
                title = "No item sales yet",
                body = "Once orders are captured, top products and services will appear here.",
            )
        } else {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = OrmaShapes.SmallCard,
                        color = OrmaColors.CellBackground,
                        contentColor = OrmaColors.Accent,
                        border = BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.12f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = OrmaColors.TextPrimary,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${item.itemType.readableTypeLabel()} / ${item.quantity} sold",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "$currency ${item.amount}",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrmaColors.TextPrimary,
                        maxLines = 1,
                    )
                }
                if (index != items.lastIndex) HorizontalDivider(color = OrmaColors.Divider)
            }
        }
    }
}

@Composable
fun OrmaDashboardNotificationPanel(
    notifications: List<OrmaDashboardNotificationPreview>,
    modifier: Modifier = Modifier,
) {
    OrmaDashboardPanel(
        title = "Notifications",
        body = "Recent customer and workspace message events.",
        modifier = modifier,
    ) {
        if (notifications.isEmpty()) {
            OrmaDashboardEmptyState(
                title = "No notifications yet",
                body = "Order and booking notifications will appear here after delivery attempts.",
            )
        } else {
            notifications.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = OrmaColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrmaColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OrmaBadge(text = item.tone.uppercase(), tone = item.tone.toOrmaTone())
                }
                if (index != notifications.lastIndex) HorizontalDivider(color = OrmaColors.Divider)
            }
        }
    }
}

private fun Double.toMoneyLabel(): String =
    kotlin.math.round(this).toLong().toString()

private fun String.toOrmaTone(): OrmaStatusTone =
    when (lowercase()) {
        "success" -> OrmaStatusTone.Success
        "warning" -> OrmaStatusTone.Warning
        "danger", "error" -> OrmaStatusTone.Danger
        else -> OrmaStatusTone.Info
    }

private fun String.readableTypeLabel(): String =
    split("_", "-", " ").filter { it.isNotBlank() }.joinToString(" ") { part ->
        part.lowercase().replaceFirstChar { it.uppercase() }
    }

private fun String.readableActionLabel(): String =
    when {
        isBlank() -> "Review"
        contains(".") -> substringAfter(".").readableTypeLabel()
        else -> readableTypeLabel()
    }

private fun String.shortDateTimeLabel(): String =
    take(16).replace("T", " ")
