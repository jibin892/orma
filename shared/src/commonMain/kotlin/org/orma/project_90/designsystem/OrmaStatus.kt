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
        container = OrmaColors.Accent.copy(alpha = 0.06f),
        content = OrmaColors.Accent.copy(alpha = 0.64f),
        border = OrmaColors.Accent.copy(alpha = 0.12f),
    )
    OrmaStatusTone.Info -> OrmaStatusColors(
        container = Color(0xFF315E7D).copy(alpha = 0.10f),
        content = Color(0xFF315E7D),
        border = Color(0xFF315E7D).copy(alpha = 0.16f),
    )
    OrmaStatusTone.Success -> OrmaStatusColors(
        container = Color(0xFF2F6F5E).copy(alpha = 0.10f),
        content = Color(0xFF2F6F5E),
        border = Color(0xFF2F6F5E).copy(alpha = 0.16f),
    )
    OrmaStatusTone.Warning -> OrmaStatusColors(
        container = Color(0xFF9A6B21).copy(alpha = 0.12f),
        content = Color(0xFF7A5417),
        border = Color(0xFF9A6B21).copy(alpha = 0.18f),
    )
    OrmaStatusTone.Danger -> OrmaStatusColors(
        container = Color(0xFFA54435).copy(alpha = 0.10f),
        content = Color(0xFFA54435),
        border = Color(0xFFA54435).copy(alpha = 0.18f),
    )
}
