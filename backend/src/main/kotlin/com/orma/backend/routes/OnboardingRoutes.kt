package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.db.TeamAccessException
import com.orma.backend.db.TeamInviteRecord
import com.orma.backend.db.TeamMemberRecord
import com.orma.backend.db.TeamOverviewRecord
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.NotificationPreferenceRequest
import com.orma.backend.models.OnboardingMutationResponse
import com.orma.backend.models.TeamInviteRequest
import com.orma.backend.models.TeamInviteResponse
import com.orma.backend.models.TeamMemberAccessRequest
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

        try {
            val session = repository.completeBusinessSetup(firebaseUser, request)
            call.respond(session.toMutationResponse(config))
        } catch (error: TeamAccessException) {
            call.respondTeamAccessError(error)
        }
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
                canInviteMembers = overview.canInviteMembers,
                members = overview.members.map { it.toResponse() },
                invites = overview.invites.map { it.toResponse() },
            ),
        )
    }

    post("/onboarding/team/invites") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<TeamInviteRequest>()
        val hasContact = !request.inviteeEmail.isNullOrBlank() || !request.inviteePhoneNumber.isNullOrBlank()
        if (!hasContact) {
            call.respondTeamAccessError(
                TeamAccessException(
                    code = "team_invite_contact_required",
                    message = "Add an email or phone number before creating the invite.",
                ),
            )
            return@post
        }
        val overview = try {
            repository.createTeamInvite(firebaseUser, request)
        } catch (error: TeamAccessException) {
            call.respondTeamAccessError(error)
            return@post
        }
        if (overview == null) {
            call.respondTeamWorkspaceNotFound()
            return@post
        }
        call.respond(overview.toResponse(config))
    }

    post("/onboarding/team/invites/{id}/revoke") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val inviteId = call.parameters["id"].orEmpty()
        val overview = try {
            repository.revokeTeamInvite(firebaseUser, inviteId)
        } catch (error: TeamAccessException) {
            call.respondTeamAccessError(error)
            return@post
        }
        if (overview == null) {
            call.respondTeamWorkspaceNotFound()
            return@post
        }
        call.respond(overview.toResponse(config))
    }

    post("/onboarding/team/members/{id}/remove") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val memberId = call.parameters["id"].orEmpty()
        val overview = try {
            repository.removeTeamMember(firebaseUser, memberId)
        } catch (error: TeamAccessException) {
            call.respondTeamAccessError(error)
            return@post
        }
        if (overview == null) {
            call.respondTeamWorkspaceNotFound()
            return@post
        }
        call.respond(overview.toResponse(config))
    }

    post("/onboarding/team/members/{id}/access") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val memberId = call.parameters["id"].orEmpty()
        val request = call.receive<TeamMemberAccessRequest>()
        val overview = try {
            repository.updateTeamMemberAccess(firebaseUser, memberId, request)
        } catch (error: TeamAccessException) {
            call.respondTeamAccessError(error)
            return@post
        }
        if (overview == null) {
            call.respondTeamWorkspaceNotFound()
            return@post
        }
        call.respond(overview.toResponse(config))
    }

    post("/onboarding/notifications") {
        val request = call.receive<NotificationPreferenceRequest>()
        if (request.enabled && request.deviceToken.isNullOrBlank() && request.channels == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "notification_token_required",
                    message = "ORMA could not register this device for notifications. Allow notifications on this device and try again.",
                ),
            )
            return@post
        }
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        val session = repository.updateNotificationPreference(
            firebaseUser = firebaseUser,
            enabled = request.enabled,
            deviceToken = request.deviceToken,
            platform = request.platform,
            deviceName = request.deviceName,
            channels = request.channels,
        )
        call.respond(session.toMutationResponse(config))
    }

    post("/onboarding/notifications/device/logout") {
        val request = call.receive<NotificationPreferenceRequest>()
        if (request.deviceToken.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "notification_token_required",
                    message = "ORMA could not identify this device notification token.",
                ),
            )
            return@post
        }
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        repository.unregisterNotificationDevice(
            firebaseUser = firebaseUser,
            deviceToken = request.deviceToken,
        )
        call.respond(HttpStatusCode.NoContent)
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

private fun TeamOverviewRecord.toResponse(config: AppConfig): TeamOverviewResponse =
    TeamOverviewResponse(
        workspace = workspace.toResponse(config),
        canInviteMembers = canInviteMembers,
        members = members.map { it.toResponse() },
        invites = invites.map { it.toResponse() },
    )

private fun TeamMemberRecord.toResponse(): TeamMemberResponse =
    TeamMemberResponse(
        id = id,
        userId = userId,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        role = role,
        permissions = permissions,
        status = status,
        joinedAt = joinedAt,
    )

private fun TeamInviteRecord.toResponse(): TeamInviteResponse =
    TeamInviteResponse(
        id = id,
        code = code,
        inviteeName = inviteeName,
        inviteeEmail = inviteeEmail,
        inviteePhoneNumber = inviteePhoneNumber,
        role = role,
        permissions = permissions,
        status = status,
        createdAt = createdAt,
        expiresAt = expiresAt,
        createdByDisplayName = createdByDisplayName,
        createdByEmail = createdByEmail,
    )

private suspend fun ApplicationCall.respondTeamWorkspaceNotFound() {
    respond(
        HttpStatusCode.NotFound,
        ErrorResponse(
            code = "workspace_not_found",
            message = "Complete business setup before managing team access.",
        ),
    )
}

private suspend fun ApplicationCall.respondTeamAccessError(error: TeamAccessException) {
    val status = when (error.code) {
        "team_owner_required", "team_permission_denied" -> HttpStatusCode.Forbidden
        "workspace_not_found", "team_member_not_found" -> HttpStatusCode.NotFound
        else -> HttpStatusCode.BadRequest
    }
    respond(
        status,
        ErrorResponse(
            code = error.code,
            message = error.message,
        ),
    )
}
