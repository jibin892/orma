package org.orma.project_90.publiccatalog

internal actual fun openPublicCatalogPaymentLink(url: String) {
    openPaymentLink(url)
}

private fun openPaymentLink(url: String): Unit = js(
    """
    if (url && typeof window !== 'undefined') {
      window.location.href = url;
    }
    """,
)
