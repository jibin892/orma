package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.MetaIntegrationRepository
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.MetaConnectionRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.metaIntegrationRoutes(
    config: AppConfig,
    metaIntegrationRepository: MetaIntegrationRepository?,
) {
    get("/integrations/meta/status") {
        val repository = metaIntegrationRepository ?: return@get call.metaDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val status = repository.status(firebaseUser) ?: return@get call.metaWorkspaceNotFound()
        call.respond(status)
    }

    post("/integrations/meta/connection") {
        val repository = metaIntegrationRepository ?: return@post call.metaDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<MetaConnectionRequest>()
        val status = repository.upsertConnection(firebaseUser, request) ?: return@post call.metaWorkspaceNotFound()
        call.respond(status)
    }

    post("/integrations/meta/catalog/sync") {
        val repository = metaIntegrationRepository ?: return@post call.metaDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val sync = repository.syncCatalog(firebaseUser) ?: return@post call.metaWorkspaceNotFound()
        call.respond(sync)
    }

    get("/webhooks/meta") {
        val mode = call.request.queryParameters["hub.mode"]
        val token = call.request.queryParameters["hub.verify_token"]
        val challenge = call.request.queryParameters["hub.challenge"]
        if (mode == "subscribe" && token == config.metaWebhookVerifyToken && !challenge.isNullOrBlank()) {
            call.respond(HttpStatusCode.OK, challenge)
        } else {
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    code = "meta_webhook_verification_failed",
                    message = "Meta webhook verification failed.",
                ),
            )
        }
    }

    post("/webhooks/meta") {
        val repository = metaIntegrationRepository ?: return@post call.metaDatabaseNotConfigured()
        val event = repository.recordWebhook(call.receiveText())
        call.respond(event)
    }
}

private suspend fun ApplicationCall.metaDatabaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before Meta integration APIs can run.",
        ),
    )
}

private suspend fun ApplicationCall.metaWorkspaceNotFound() {
    respond(
        HttpStatusCode.NotFound,
        ErrorResponse(
            code = "workspace_not_found",
            message = "Complete business setup before connecting Meta.",
        ),
    )
}
