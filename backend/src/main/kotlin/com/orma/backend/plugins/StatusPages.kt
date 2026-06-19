package com.orma.backend.plugins

import com.google.firebase.auth.FirebaseAuthException
import com.orma.backend.auth.FirebaseAuthNotConfiguredException
import com.orma.backend.models.ErrorResponse
import com.orma.backend.storage.MediaStorageNotConfiguredException
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

        exception<MediaStorageNotConfiguredException> { call, cause ->
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse(
                    code = cause.notConfiguredCode(),
                    message = cause.message ?: "Media storage is not configured.",
                ),
            )
        }

        exception<FirebaseAuthException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    code = "invalid_firebase_token",
                    message = cause.message ?: "Firebase ID token is invalid.",
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

private fun MediaStorageNotConfiguredException.notConfiguredCode(): String =
    when (providerName) {
        "cloudinary" -> "cloudinary_storage_not_configured"
        "firebase" -> "firebase_storage_not_configured"
        else -> "media_storage_not_configured"
    }
