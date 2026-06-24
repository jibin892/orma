package org.orma.project_90.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberOrmaClipboard(): OrmaClipboard =
    remember {
        object : OrmaClipboard {
            override fun copyText(text: String): Boolean = runCatching {
                UIPasteboard.generalPasteboard.string = text
            }.isSuccess
        }
    }
