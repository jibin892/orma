package org.orma.project_90.publiccatalog

import java.awt.Desktop
import java.net.URI

internal actual fun openPublicCatalogPaymentLink(url: String) {
    val cleanUrl = url.trim().takeIf { it.isNotBlank() } ?: return
    runCatching {
        if (!Desktop.isDesktopSupported()) return@runCatching
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.BROWSE)) return@runCatching
        desktop.browse(URI.create(cleanUrl))
    }
}
