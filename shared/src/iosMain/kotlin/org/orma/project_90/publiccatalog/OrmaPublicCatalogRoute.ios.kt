package org.orma.project_90.publiccatalog

actual fun currentOrmaPublicCatalogWorkspaceId(): String? = null

actual fun currentOrmaPublicCatalogUrl(workspaceId: String): String =
    "https://orma-web-dist-dev-api.vercel.app/catalog/${workspaceId.trim().trim('/')}"
