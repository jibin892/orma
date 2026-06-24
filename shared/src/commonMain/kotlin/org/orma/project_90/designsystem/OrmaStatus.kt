package org.orma.project_90.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class OrmaStatusTone {
    Neutral,
    Info,
    Success,
    Warning,
    Danger,
}

@Immutable
data class OrmaStatusColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

fun ormaStatusToneFor(value: String): OrmaStatusTone {
    val normalized = value.lowercase()
    return when {
        normalized in setOf("success", "successful", "active", "confirmed", "completed", "paid") -> {
            OrmaStatusTone.Success
        }
        normalized in setOf("warning", "pending", "review", "hold", "scheduled") -> {
            OrmaStatusTone.Warning
        }
        normalized in setOf("danger", "error", "failed", "cancelled", "canceled", "overdue") -> {
            OrmaStatusTone.Danger
        }
        normalized in setOf("info", "draft", "new", "processing", "submitted") -> {
            OrmaStatusTone.Info
        }
        else -> OrmaStatusTone.Neutral
    }
}

fun ormaStatusColors(tone: OrmaStatusTone): OrmaStatusColors = when (tone) {
    OrmaStatusTone.Neutral -> OrmaStatusColors(
        container = OrmaColors.TextAndIconInk.copy(alpha = 0.06f),
        content = OrmaColors.TextAndIconInk.copy(alpha = 0.64f),
        border = OrmaColors.TextAndIconInk.copy(alpha = 0.12f),
    )
    OrmaStatusTone.Info -> OrmaStatusColors(
        container = OrmaColors.Info.copy(alpha = 0.10f),
        content = OrmaColors.Info,
        border = OrmaColors.Info.copy(alpha = 0.16f),
    )
    OrmaStatusTone.Success -> OrmaStatusColors(
        container = OrmaColors.Success.copy(alpha = 0.10f),
        content = OrmaColors.Success,
        border = OrmaColors.Success.copy(alpha = 0.16f),
    )
    OrmaStatusTone.Warning -> OrmaStatusColors(
        container = OrmaColors.Warning.copy(alpha = 0.12f),
        content = OrmaColors.Warning,
        border = OrmaColors.Warning.copy(alpha = 0.18f),
    )
    OrmaStatusTone.Danger -> OrmaStatusColors(
        container = OrmaColors.Danger.copy(alpha = 0.10f),
        content = OrmaColors.Danger,
        border = OrmaColors.Danger.copy(alpha = 0.18f),
    )
}
