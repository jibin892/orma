package org.orma.project_90.backend

data class OrmaBackendConfig(
    val baseUrl: String,
)

expect fun currentOrmaBackendConfig(): OrmaBackendConfig
