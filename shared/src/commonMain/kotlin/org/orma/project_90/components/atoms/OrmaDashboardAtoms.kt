package org.orma.project_90.components.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaElevation
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.ormaStatusColors

@Immutable
data class OrmaDashboardMetric(
    val label: String,
    val value: String,
    val detail: String,
    val tone: OrmaStatusTone = OrmaStatusTone.Info,
    val badge: String? = null,
    val trend: String? = null,
)

/**
 * Small reusable metric card kept for existing dashboard call sites.
 *
 * Mobile: use in one-column or two-column groups. Desktop/web: use inside a KPI row.
 */
@Composable
fun OrmaMetricCard(
    metric: OrmaDashboardMetric,
    modifier: Modifier = Modifier,
) {
    OrmaKpiCard(
        metric = metric,
        modifier = modifier,
    )
}

/**
 * Reusable KPI card for business performance indicators.
 *
 * Use this for real workspace numbers only: revenue, orders, customers, stock health,
 * fulfilment, and other measurable business signals.
 */
@Composable
fun OrmaKpiCard(
    metric: OrmaDashboardMetric,
    modifier: Modifier = Modifier,
) {
    val toneColors = ormaStatusColors(metric.tone)
    val semanticText = buildString {
        append("${metric.label}: ${metric.value}. ${metric.detail}")
        metric.trend?.let { append(". $it") }
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = semanticText
            },
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier
                    .width(3.dp)
                    .heightIn(min = 82.dp),
                shape = OrmaShapes.Capsule,
                color = toneColors.content.copy(alpha = 0.42f),
                contentColor = toneColors.content,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {}
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrmaDashboardSectionLabel(
                    text = metric.label,
                    badge = metric.badge,
                    tone = metric.tone,
                )
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OrmaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metric.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrmaColors.TextSecondary.copy(alpha = 0.80f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                metric.trend?.let {
                    OrmaKpiTrendPill(
                        text = it,
                        tone = metric.tone,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrmaKpiTrendPill(
    text: String,
    tone: OrmaStatusTone,
) {
    val colors = ormaStatusColors(tone)
    Surface(
        shape = OrmaShapes.Capsule,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.58f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Uppercase dashboard label with an optional status badge.
 */
@Composable
fun OrmaDashboardSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    tone: OrmaStatusTone = OrmaStatusTone.Info,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = OrmaColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        badge?.let {
            OrmaBadge(
                text = it.uppercase(),
                tone = tone,
            )
        }
    }
}

/**
 * Shared empty state for dashboard tables, cards, and lists.
 *
 * The icon is a slot so app features can supply platform-appropriate symbols without
 * making this atom depend on a specific feature enum.
 */
@Composable
fun OrmaDashboardEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $body"
            },
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            icon?.invoke()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            if (actionText != null && onAction != null) {
                OrmaEmptyStateAction(text = actionText, onClick = onAction)
            }
        }
    }
}

@Composable
private fun OrmaEmptyStateAction(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = OrmaShapes.Capsule,
        color = OrmaColors.Accent.copy(alpha = 0.08f),
        contentColor = OrmaColors.Accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OrmaColors.TextPrimary,
        )
    }
}

/**
 * Base record surface for dashboard rows and mobile cards.
 */
@Composable
fun OrmaDashboardRecordSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OrmaShapes.StandardCell,
        color = OrmaColors.CardBackground,
        contentColor = OrmaColors.TextPrimary,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/**
 * Consistent icon bubble used by dashboard empty states and action rows.
 */
@Composable
fun OrmaDashboardIconBubble(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = OrmaShapes.Capsule,
        color = OrmaColors.Accent.copy(alpha = 0.10f),
        contentColor = OrmaColors.Accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
