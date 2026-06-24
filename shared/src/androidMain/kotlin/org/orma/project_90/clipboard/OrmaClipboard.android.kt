package org.orma.project_90.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOrmaClipboard(): OrmaClipboard {
    val context = LocalContext.current
    return remember(context) {
        object : OrmaClipboard {
            override fun copyText(text: String): Boolean = runCatching {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ORMA catalog link", text))
            }.isSuccess
        }
    }
}
