package com.orma.backend.routes

import com.orma.backend.auth.FirebaseTokenVerifier
import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.models.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

suspend fun ApplicationCall.verifiedFirebaseUser(config: AppConfig): VerifiedFirebaseUser? {
    val token = request.headers[HttpHeaders.Authorization]
        ?.trim()
        ?.removePrefix("Bearer")
        ?.trim()

    if (token.isNullOrBlank()) {
        respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse(
                code = "missing_bearer_token",
                message = "Authorization: Bearer <Firebase ID token> is required.",
            ),
        )
        return null
    }

    return FirebaseTokenVerifier(config).verify(token)
}
