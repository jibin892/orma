package org.orma.project_90.clipboard

import androidx.compose.runtime.Composable

interface OrmaClipboard {
    fun copyText(text: String): Boolean
}

@Composable
expect fun rememberOrmaClipboard(): OrmaClipboard
