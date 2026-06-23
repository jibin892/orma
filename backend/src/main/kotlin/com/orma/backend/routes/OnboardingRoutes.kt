package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.db.TeamMemberRecord
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.NotificationPreferenceRequest
import com.orma.backend.models.OnboardingMutationResponse
import com.orma.backend.models.TeamMemberResponse
import com.orma.backend.models.TeamOverviewResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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

    get("/onboarding/team") {
        val repository = onboardingRepository ?: return@get call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val overview = repository.listWorkspaceTeam(firebaseUser)
        if (overview == null) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    code = "workspace_not_found",
                    message = "Complete business setup before viewing team access.",
                ),
            )
            return@get
        }
        call.respond(
            TeamOverviewResponse(
                workspace = overview.workspace.toResponse(config),
                canInviteMembers = false,
                members = overview.members.map { it.toResponse() },
            ),
        )
    }

    post("/onboarding/notifications") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<NotificationPreferenceRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        val session = repository.updateNotificationPreference(
            firebaseUser = firebaseUser,
            enabled = request.enabled,
            deviceToken = request.deviceToken,
            platform = request.platform,
            deviceName = request.deviceName,
        )
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

private fun TeamMemberRecord.toResponse(): TeamMemberResponse =
    TeamMemberResponse(
        id = id,
        userId = userId,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        role = role,
        status = status,
        joinedAt = joinedAt,
    )
