package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.NotificationPreferenceRequest
import com.orma.backend.models.OnboardingMutationResponse
import com.orma.backend.models.TeamInviteJoinRequest
import com.orma.backend.models.TeamInviteLookupRequest
import com.orma.backend.models.TeamInviteResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.onboardingRoutes(
    config: AppConfig,
    onboardingRepository: OnboardingRepository?,
) {
    post("/onboarding/business") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<BusinessSetupRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        val session = repository.completeBusinessSetup(firebaseUser, request)
        call.respond(session.toMutationResponse(config))
    }

    post("/onboarding/team-invites/lookup") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<TeamInviteLookupRequest>()
        call.verifiedFirebaseUser(config) ?: return@post

        val workspace = repository.lookupInvite(request.code)
        if (workspace == null) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    code = "invite_not_found",
                    message = "Invite code is invalid or expired.",
                ),
            )
            return@post
        }

        call.respond(
            TeamInviteResponse(
                code = workspace.inviteCode ?: request.code.trim().uppercase(),
                workspace = workspace.toResponse(config),
            ),
        )
    }

    post("/onboarding/team-invites/join") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<TeamInviteJoinRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        val session = repository.joinInvite(firebaseUser, request.code)
        if (session == null) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    code = "invite_not_found",
                    message = "Invite code is invalid or expired.",
                ),
            )
            return@post
        }

        call.respond(session.toMutationResponse(config))
    }

    post("/onboarding/notifications") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<NotificationPreferenceRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        val session = repository.updateNotificationPreference(firebaseUser, request.enabled)
        call.respond(session.toMutationResponse(config))
    }
}

private suspend fun ApplicationCall.databaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before onboarding APIs can run.",
        ),
    )
}

private fun com.orma.backend.db.OnboardingSessionRecord.toMutationResponse(config: AppConfig): OnboardingMutationResponse =
    OnboardingMutationResponse(
        user = user.toResponse(),
        workspace = workspace?.toResponse(config),
        onboardingStatus = onboardingStatus,
        requiredStep = requiredStep,
        accessPath = accessPath,
    )
