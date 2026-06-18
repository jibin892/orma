package com.orma.backend.plugins

import com.orma.backend.auth.FirebaseAuthNotConfiguredException
import com.orma.backend.models.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<FirebaseAuthNotConfiguredException> { call, cause ->
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse(
                    code = "firebase_auth_not_configured",
                    message = cause.message ?: "Firebase Auth is not configured.",
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled backend error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = "internal_error",
                    message = "Unexpected backend error.",
                ),
            )
        }
    }
}
