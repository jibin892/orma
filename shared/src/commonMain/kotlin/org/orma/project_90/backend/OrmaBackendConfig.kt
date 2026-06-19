package org.orma.project_90.backend

object OrmaBackendBaseUrls {
    const val Local = "http://localhost:8090"
    const val Dev = "https://orma-backend.onrender.com"
}

data class OrmaBackendConfig(
    val baseUrl: String,
)

fun localOrmaBackendConfig(): OrmaBackendConfig =
    OrmaBackendConfig(baseUrl = OrmaBackendBaseUrls.Local)

fun devOrmaBackendConfig(): OrmaBackendConfig =
    OrmaBackendConfig(baseUrl = OrmaBackendBaseUrls.Dev)

expect fun currentOrmaBackendConfig(): OrmaBackendConfig
