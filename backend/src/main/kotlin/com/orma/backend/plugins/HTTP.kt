package com.orma.backend.plugins

import com.orma.backend.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.CORSConfig
import io.ktor.server.plugins.cors.routing.CORS
import org.slf4j.event.Level

fun Application.configureHTTP(config: AppConfig) {
    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowNonSimpleContentTypes = true

        if (config.allowedOrigins.contains("*")) {
            anyHost()
        } else {
            config.allowedOrigins.forEach { allowOrigin(it) }
        }
    }
}

private fun CORSConfig.allowOrigin(origin: String) {
    val normalized = origin.trim().trimEnd('/')
    val scheme = normalized.substringBefore("://", missingDelimiterValue = "http")
    val host = normalized.substringAfter("://", missingDelimiterValue = normalized)
    allowHost(host, schemes = listOf(scheme))
}
