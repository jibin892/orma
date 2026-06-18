package org.orma.project_90.backend

actual fun currentOrmaBackendConfig(): OrmaBackendConfig =
    OrmaBackendConfig(baseUrl = "http://localhost:8090")
