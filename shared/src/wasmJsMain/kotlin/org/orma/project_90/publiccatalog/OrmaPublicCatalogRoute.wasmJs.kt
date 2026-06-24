@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

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

actual fun currentOrmaPublicCatalogUrl(workspaceId: String): String =
    "https://orma-web-dist-dev-api.vercel.app/?catalog=$workspaceId"

private fun browserPathname(): String =
    browserPathnameJs().toString()

private fun browserHashPath(): String =
    browserHashPathJs().toString()

@Suppress("UNUSED_PARAMETER")
private fun browserSearchParam(name: String): String? =
    browserSearchParamJs(name).toString().takeIf { it != "null" && it.isNotBlank() }

private fun browserPathnameJs(): JsAny? = js("window.location.pathname || ''")

private fun browserHashPathJs(): JsAny? = js("window.location.hash || ''")

private fun browserSearchParamJs(name: String): JsAny? = js("new URLSearchParams(window.location.search || '').get(name)")

private fun browserCatalogWorkspaceIdFromPath(path: String): String? {
    val normalizedPath = path
        .removePrefix("#")
        .removePrefix("!")
        .trim('/')
    val parts = normalizedPath.split('/').filter(String::isNotBlank)
    val catalogIndex = parts.indexOf("catalog")
    return parts.getOrNull(catalogIndex + 1)?.takeIf(String::isNotBlank)
}
