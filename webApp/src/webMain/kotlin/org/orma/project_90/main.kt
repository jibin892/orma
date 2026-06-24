package org.orma.project_90

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.orma.project_90.designsystem.OrmaWebTypographyProvider

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    waitForOrmaWebFonts {
        startOrmaWebApp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startOrmaWebApp() {
    ComposeViewport {
        OrmaWebTypographyProvider {
            App()
        }
    }
}

private fun waitForOrmaWebFonts(onReady: () -> Unit): Unit = js(
    """
        const ready = window.OrmaWebFontReady;
        if (ready && typeof ready.then === "function") {
            ready.then(onReady).catch(onReady);
        } else {
            onReady();
        }
    """,
)
