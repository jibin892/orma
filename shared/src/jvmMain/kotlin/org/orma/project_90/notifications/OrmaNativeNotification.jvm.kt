package org.orma.project_90.notifications

import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage

actual fun showOrmaNativeNotification(
    title: String,
    body: String,
) {
    DesktopNotificationBridge.show(title = title, body = body)
}

private object DesktopNotificationBridge {
    private val trayIcon: TrayIcon? by lazy { createTrayIcon() }

    fun show(title: String, body: String) {
        val cleanTitle = title.takeIf { it.isNotBlank() } ?: "ORMA"
        val cleanBody = body.takeIf { it.isNotBlank() } ?: "New workspace update"
        val icon = trayIcon
        if (icon != null) {
            icon.displayMessage(cleanTitle, cleanBody, TrayIcon.MessageType.INFO)
        } else {
            runCatching { Toolkit.getDefaultToolkit().beep() }
        }
    }

    private fun createTrayIcon(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        return runCatching {
            val tray = SystemTray.getSystemTray()
            val icon = TrayIcon(notificationImage(), "ORMA").apply {
                isImageAutoSize = true
            }
            tray.add(icon)
            icon
        }.getOrNull()
    }

    private fun notificationImage(): Image {
        val size = 16
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = java.awt.Color(0, 66, 60)
            graphics.fillRoundRect(0, 0, size, size, 4, 4)
            graphics.color = java.awt.Color.WHITE
            graphics.fillOval(4, 4, 8, 8)
        } finally {
            graphics.dispose()
        }
        return image
    }
}
