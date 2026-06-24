@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.orma.project_90.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaClipboard(): OrmaClipboard =
    remember {
        object : OrmaClipboard {
            override fun copyText(text: String): Boolean = copyTextToBrowserClipboard(text)
        }
    }

private fun copyTextToBrowserClipboard(text: String): Boolean =
    copyTextToBrowserClipboardJs(text).toString() == "true"

private fun copyTextToBrowserClipboardJs(text: String): JsAny? = js(
    """
    (() => {
      try {
        if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text);
          return true;
        }
        if (typeof document !== 'undefined') {
          const input = document.createElement('textarea');
          input.value = text;
          input.setAttribute('readonly', '');
          input.style.position = 'fixed';
          input.style.left = '-9999px';
          document.body.appendChild(input);
          input.select();
          const ok = document.execCommand('copy');
          document.body.removeChild(input);
          return !!ok;
        }
      } catch (error) {
        return false;
      }
      return false;
    })()
    """,
)
