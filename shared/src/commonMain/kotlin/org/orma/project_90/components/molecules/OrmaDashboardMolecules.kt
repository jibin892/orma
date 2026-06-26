package org.orma.project_90.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
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
import org.orma.project_90.components.atoms.OrmaDashboardSectionLabel
import org.orma.project_90.designsystem.OrmaActionRow
import org.orma.project_90.designsystem.OrmaBadge
import org.orma.project_90.designsystem.OrmaColors
import org.orma.project_90.designsystem.OrmaElevation
import org.orma.project_90.designsystem.OrmaSecondaryButton
import org.orma.project_90.designsystem.OrmaShapes
import org.orma.project_90.designsystem.OrmaStatusTone

enum class OrmaDashboardActionVariant {
    Primary,
    Secondary,
    Tertiary,
}

@Immutable
data class OrmaDashboardAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val variant: OrmaDashboardActionVariant = OrmaDashboardActionVariant.Primary,
)

/**
 * Feature-level dashboard header with slot-like actions.
 *
 * Mobile: compact stacked header and thumb-friendly action row.
 * Desktop/web: shadcn-style card header with right-aligned actions.
 */
@Composable
fun OrmaDashboardActionHeader(
    eyebrow: String,
    title: String,
    body: String,
    primaryAction: OrmaDashboardAction,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    loading: Boolean = false,
    secondaryAction: OrmaDashboardAction? = null,
    tertiaryAction: OrmaDashboardAction? = null,
) {
    if (wide) {
        OrmaDashboardWideActionHeader(
            eyebrow = eyebrow,
            title = title,
            body = body,
            primaryAction = primaryAction.copy(
                text = if (loading) "Syncing..." else primaryAction.text,
                enabled = primaryAction.enabled && !loading,
            ),
            secondaryAction = secondaryAction,
            tertiaryAction = tertiaryAction,
            modifier = modifier,
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            OrmaBadge(text = eyebrow.uppercase(), tone = OrmaStatusTone.Info)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOfNotNull(
                    primaryAction.copy(
                        text = if (loading) "Syncing..." else primaryAction.text,
                        enabled = primaryAction.enabled && !loading,
                    ),
                    secondaryAction,
                    tertiaryAction,
                ).forEach { action ->
                    OrmaDashboardHeaderButton(action = action)
                }
            }
        }
    }
}

@Composable
private fun OrmaDashboardWideActionHeader(
    eyebrow: String,
    title: String,
    body: String,
    primaryAction: OrmaDashboardAction,
    secondaryAction: OrmaDashboardAction?,
    tertiaryAction: OrmaDashboardAction?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $body"
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OrmaBadge(text = eyebrow.uppercase(), tone = OrmaStatusTone.Info)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOfNotNull(tertiaryAction, secondaryAction, primaryAction).forEach { action ->
                OrmaDashboardHeaderButton(action = action)
            }
        }
    }
}

@Composable
private fun OrmaDashboardHeaderButton(
    action: OrmaDashboardAction,
    modifier: Modifier = Modifier,
) {
    val primary = action.variant == OrmaDashboardActionVariant.Primary
    Surface(
        onClick = action.onClick,
        modifier = modifier
            .height(38.dp)
            .widthIn(min = if (primary) 104.dp else 84.dp, max = 170.dp),
        enabled = action.enabled,
        shape = OrmaShapes.CheckoutButton,
        color = when {
            !action.enabled -> OrmaColors.Accent.copy(alpha = 0.16f)
            primary -> OrmaColors.Accent
            else -> OrmaColors.ScreenBackground
        },
        contentColor = if (primary) OrmaColors.OnAccent else OrmaColors.TextPrimary,
        border = if (primary) null else BorderStroke(0.6.dp, OrmaColors.Hairline.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = action.text,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    !action.enabled -> OrmaColors.TextDisabled
                    primary -> OrmaColors.OnAccent
                    else -> OrmaColors.TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Reusable checklist guidance block for dashboard modules.
 */
@Composable
fun OrmaDashboardChecklistCard(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier,
    tertiaryText: String? = null,
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = OrmaColors.TextPrimary,
            )
            items.forEach { item ->
                OrmaDashboardChecklistRow(text = item)
            }
            tertiaryText?.let {
                OrmaBadge(
                    text = it.uppercase(),
                    tone = OrmaStatusTone.Info,
                )
            }
        }
    }
}

@Composable
fun OrmaDashboardChecklistRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 1.dp)
                .height(7.dp)
                .widthIn(min = 7.dp),
            shape = OrmaShapes.Capsule,
            color = OrmaColors.Accent.copy(alpha = 0.42f),
            contentColor = OrmaColors.Accent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {}
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = OrmaColors.TextSecondary,
        )
    }
}

/**
 * Reusable label/value row for dashboard metric cards and panels.
 */
@Composable
fun OrmaDashboardMetricLine(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = OrmaColors.TextPrimary,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = OrmaColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (value.ormaLooksLikeAmountText()) OrmaColors.Accent else OrmaColors.TextPrimary,
        )
    }
}

private fun String.ormaLooksLikeAmountText(): Boolean =
    Regex("\\b(?:INR|AED|USD|EUR|GBP|SAR|QAR|KWD|OMR|BHD)\\b").containsMatchIn(this)

/**
 * Dashboard panel with a title, description, and free content slot.
 */
@Composable
fun OrmaDashboardPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: String? = null,
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
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
                action?.let {
                    OrmaBadge(text = it.uppercase(), tone = OrmaStatusTone.Info)
                }
            }
            HorizontalDivider(color = OrmaColors.Divider.copy(alpha = 0.52f))
            content()
        }
    }
}
