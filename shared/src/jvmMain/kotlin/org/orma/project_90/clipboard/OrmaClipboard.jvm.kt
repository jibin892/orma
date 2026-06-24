package org.orma.project_90.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun rememberOrmaClipboard(): OrmaClipboard =
    remember {
        object : OrmaClipboard {
            override fun copyText(text: String): Boolean = runCatching {
                val selection = StringSelection(text)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            }.isSuccess
        }
    }
