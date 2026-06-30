package org.orma.project_90

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.orma.project_90.designsystem.OrmaWebTypographyProvider

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startOrmaWebApp()
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startOrmaWebApp() {
    ComposeViewport {
        OrmaWebTypographyProvider {
            App()
        }
    }
    hideOrmaStartupSplash()
}

private fun hideOrmaStartupSplash(): Unit = js(
    """
        window.setTimeout(function () {
            const splash = document.getElementById("orma-startup-splash");
            if (splash && splash.parentNode) {
                splash.parentNode.removeChild(splash);
            }
        }, 250);
    """,
)
