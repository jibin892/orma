package org.orma.project_90.components.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.orma.project_90.components.molecules.OrmaDashboardAction
import org.orma.project_90.components.molecules.OrmaDashboardActionHeader

/**
 * Reusable dashboard page template for module pages.
 *
 * Mobile: stacked title, context, actions, then content.
 * Desktop/web: compact shadcn-style action header, then operational content.
 */
@Composable
fun OrmaDashboardSectionScaffold(
    eyebrow: String,
    title: String,
    body: String,
    primaryAction: OrmaDashboardAction,
    loading: Boolean,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    secondaryAction: OrmaDashboardAction? = null,
    tertiaryAction: OrmaDashboardAction? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 14.dp),
    ) {
        OrmaDashboardActionHeader(
            eyebrow = eyebrow,
            title = title,
            body = body,
            primaryAction = primaryAction,
            secondaryAction = secondaryAction,
            tertiaryAction = tertiaryAction,
            loading = loading,
            wide = wide,
        )
        content()
    }
}

/**
 * Responsive dashboard workspace for list/detail or records/guidance modules.
 *
 * Mobile: one-column with primary content before guidance.
 * Desktop/web: two-pane layout with a stable secondary inspector area.
 */
@Composable
fun OrmaDashboardResponsiveWorkspace(
    wide: Boolean,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    primaryWeight: Float = 1f,
    secondaryMinWidth: Dp = 320.dp,
    secondaryMaxWidth: Dp = 430.dp,
    stackBelowWidth: Dp = 940.dp,
) {
    if (wide) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            if (maxWidth < stackBelowWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    primary()
                    secondary()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(primaryWeight)) { primary() }
                    Box(
                        modifier = Modifier.widthIn(
                            min = secondaryMinWidth,
                            max = secondaryMaxWidth,
                        ),
                    ) {
                        secondary()
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            primary()
            secondary()
        }
    }
}
