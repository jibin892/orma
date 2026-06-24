package org.orma.project_90.publiccatalog

actual fun currentOrmaPublicCatalogWorkspaceId(): String? {
    val pathWorkspaceId = browserCatalogWorkspaceIdFromPath(browserPathname())
        ?: browserCatalogWorkspaceIdFromPath(browserHashPath())
    val queryWorkspaceId = browserSearchParam("workspaceId")
        ?: browserSearchParam("workspace")
        ?: browserSearchParam("catalog")
    return pathWorkspaceId
        ?.takeIf(String::isNotBlank)
        ?: queryWorkspaceId?.takeIf(String::isNotBlank)
}

actual fun currentOrmaPublicCatalogUrl(workspaceId: String): String {
    val origin = browserOrigin().trimEnd('/')
    return "$origin/?catalog=$workspaceId"
}

private fun browserPathname(): String =
    js("window.location.pathname || ''").unsafeCast<String>()

private fun browserHashPath(): String =
    js("window.location.hash || ''").unsafeCast<String>()

private fun browserOrigin(): String =
    js("window.location.origin || ''").unsafeCast<String>()

@Suppress("UNUSED_PARAMETER")
private fun browserSearchParam(name: String): String? =
    js("new URLSearchParams(window.location.search || '').get(name)").unsafeCast<String?>()

private fun browserCatalogWorkspaceIdFromPath(path: String): String? {
    val normalizedPath = path
        .removePrefix("#")
        .removePrefix("!")
        .trim('/')
    val parts = normalizedPath.split('/').filter(String::isNotBlank)
    val catalogIndex = parts.indexOf("catalog")
    return parts.getOrNull(catalogIndex + 1)?.takeIf(String::isNotBlank)
}
