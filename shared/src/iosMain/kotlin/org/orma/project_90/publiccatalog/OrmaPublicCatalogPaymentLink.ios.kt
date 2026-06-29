package org.orma.project_90.publiccatalog

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun openPublicCatalogPaymentLink(url: String) {
    val cleanUrl = url.trim().takeIf { it.isNotBlank() } ?: return
    val nsUrl = NSURL.URLWithString(cleanUrl) ?: return
    runCatching {
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}
