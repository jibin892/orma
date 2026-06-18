package com.orma.backend.routes

import com.orma.backend.auth.FirebaseTokenVerifier
import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.db.OnboardingSessionRecord
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.SessionRequest
import com.orma.backend.models.SessionResponse
import com.orma.backend.models.UserResponse
import com.orma.backend.models.WorkspaceResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(
    config: AppConfig,
    onboardingRepository: OnboardingRepository?,
) {
    post("/auth/session") {
        val request = call.receive<SessionRequest>()

        if (request.idToken.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "invalid_id_token",
                    message = "idToken is required.",
                ),
            )
            return@post
        }

        val firebaseUser = FirebaseTokenVerifier(config).verify(request.idToken)
        if (onboardingRepository == null) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse(
                    code = "database_not_configured",
                    message = "DATABASE_URL is required before resolving onboarding sessions.",
                ),
            )
            return@post
        }

        val session = onboardingRepository.resolveSession(
            firebaseUser = firebaseUser,
            providerFallback = request.provider,
            emailFallback = request.email,
            phoneNumberFallback = request.phoneNumber,
            displayNameFallback = request.displayName,
        )
        call.respond(session.toSessionResponse(firebaseUser.uid))
    }
}

fun OnboardingSessionRecord.toSessionResponse(uid: String): SessionResponse =
    SessionResponse(
        uid = uid,
        email = user.email,
        phoneNumber = user.phoneNumber,
        displayName = user.displayName,
        user = user.toResponse(),
        workspace = workspace?.toResponse(),
        onboardingStatus = onboardingStatus,
        requiredStep = requiredStep,
        accessPath = accessPath,
    )

fun com.orma.backend.db.AppUserRecord.toResponse(): UserResponse =
    UserResponse(
        id = id,
        firebaseUid = firebaseUid,
        email = email,
        phoneNumber = phoneNumber,
        displayName = displayName,
        role = role,
        notificationsEnabled = notificationsEnabled,
    )

fun com.orma.backend.db.WorkspaceRecord.toResponse(): WorkspaceResponse =
    WorkspaceResponse(
        id = id,
        businessName = businessName,
        legalName = legalName,
        role = role,
        onboardingComplete = onboardingComplete,
        inviteCode = inviteCode,
    )
