package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.models.HealthResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes(config: AppConfig) {
    get("/health") {
        call.respond(
            HealthResponse(
                status = "ok",
                environment = config.environment,
                databaseConfigured = config.databaseConfigured,
                firebaseAuthConfigured = config.firebaseAuthConfigured,
                firebaseMessagingConfigured = config.firebaseMessagingConfigured,
                oneSignalPushConfigured = config.oneSignalPushConfigured,
                apnsPushConfigured = config.apnsPushConfigured,
                firebaseStorageConfigured = config.firebaseStorageConfigured,
                mediaStorageProvider = config.activeMediaStorageProvider,
                mediaStorageConfigured = config.mediaStorageConfigured,
                cloudinaryConfigured = config.cloudinaryConfigured,
                gstinCheckConfigured = config.gstinCheckConfigured,
                metaWebhookConfigured = config.metaWebhookConfigured,
                metaBackendConfigured = config.metaBackendConfigured,
                metaOAuthConfigured = config.metaOAuthConfigured,
                metaTokenStorageConfigured = config.metaTokenStorageConfigured,
                metaSystemUserTokenConfigured = config.metaSystemUserTokenConfigured,
            ),
        )
    }
}
