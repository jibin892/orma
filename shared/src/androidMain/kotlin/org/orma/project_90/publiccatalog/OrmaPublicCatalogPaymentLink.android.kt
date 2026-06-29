package org.orma.project_90.publiccatalog

import android.content.Intent
import android.net.Uri
import org.orma.project_90.auth.OrmaAndroidAuthSessionStore

internal actual fun openPublicCatalogPaymentLink(url: String) {
    val cleanUrl = url.trim().takeIf { it.isNotBlank() } ?: return
    val context = OrmaAndroidAuthSessionStore.context() ?: return
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
