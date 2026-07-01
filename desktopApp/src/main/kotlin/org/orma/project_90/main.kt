package org.orma.project_90

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    configureDesktopIcon()
    OrmaLocalPrintAgent.start()

    application {
        Window(
            onCloseRequest = {
                OrmaLocalPrintAgent.stop()
                exitApplication()
            },
            icon = painterResource("orma-app-icon.png"),
            state = rememberWindowState(width = 1180.dp, height = 760.dp),
            title = "Orma",
        ) {
            App()
        }
    }
}

private fun configureDesktopIcon() {
    runCatching {
        if (!Taskbar.isTaskbarSupported()) return@runCatching

        val taskbar = Taskbar.getTaskbar()
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return@runCatching

        val iconUrl = Thread.currentThread()
            .contextClassLoader
            .getResource("orma-app-icon.png")
            ?: return@runCatching

        taskbar.iconImage = ImageIO.read(iconUrl)
    }
}
