package org.orma.project_90.backend

actual fun currentOrmaBackendConfig(): OrmaBackendConfig =
    System.getenv("ORMA_BACKEND_BASE_URL")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let(::OrmaBackendConfig)
        ?: devOrmaBackendConfig()
