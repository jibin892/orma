package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.db.OwnerTeamInviteResult
import com.orma.backend.db.TeamInviteListRecord
import com.orma.backend.db.TeamInviteCreateResult
import com.orma.backend.db.TeamInviteJoinResult
import com.orma.backend.db.TeamMemberRecord
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.NotificationPreferenceRequest
import com.orma.backend.models.OnboardingMutationResponse
import com.orma.backend.models.TeamInviteCreateRequest
import com.orma.backend.models.TeamInviteJoinRequest
import com.orma.backend.models.TeamInviteListItemResponse
import com.orma.backend.models.TeamInviteLookupRequest
import com.orma.backend.models.TeamInviteResponse
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

    post("/onboarding/team-invites/lookup") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<TeamInviteLookupRequest>()
        call.verifiedFirebaseUser(config) ?: return@post

        val invite = repository.lookupInviteRecord(request.code)
        if (invite == null) {
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
                code = invite.code,
                workspace = invite.workspace.toResponse(config),
                inviteeName = invite.inviteeName,
                inviteeEmail = invite.inviteeEmail,
                inviteePhoneNumber = invite.inviteePhoneNumber,
                role = invite.role,
            ),
        )
    }

    get("/onboarding/team-invites/active") {
        val repository = onboardingRepository ?: return@get call.databaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get

        when (val result = repository.getOrCreateOwnerTeamInvite(firebaseUser)) {
            is OwnerTeamInviteResult.Success -> {
                call.respond(
                    TeamInviteResponse(
                        code = result.workspace.inviteCode.orEmpty(),
                        workspace = result.workspace.toResponse(config),
                    ),
                )
            }
            OwnerTeamInviteResult.WorkspaceNotFound -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        code = "workspace_not_found",
                        message = "Complete business setup before inviting team members.",
                    ),
                )
            }
            OwnerTeamInviteResult.OwnerRequired -> {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        code = "owner_required",
                        message = "Only the business owner can create team invite codes.",
                    ),
                )
            }
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
                pendingInvites = overview.pendingInvites.map { it.toResponse() },
            ),
        )
    }

    post("/onboarding/team-invites") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<TeamInviteCreateRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post

        if (request.name.isBlank() || request.name.trim().length < 2) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "invitee_name_required",
                    message = "Enter the team member name before creating an invite.",
                ),
            )
            return@post
        }
        if (request.email.isNullOrBlank() && request.phoneNumber.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "invite_contact_required",
                    message = "Enter a phone number or email for this team member.",
                ),
            )
            return@post
        }

        when (val result = repository.createOwnerTeamInvite(firebaseUser, request)) {
            is TeamInviteCreateResult.Success -> {
                val invite = result.invite
                call.respond(
                    TeamInviteResponse(
                        code = invite.code,
                        workspace = invite.workspace.toResponse(config),
                        inviteeName = invite.inviteeName,
                        inviteeEmail = invite.inviteeEmail,
                        inviteePhoneNumber = invite.inviteePhoneNumber,
                        role = invite.role,
                    ),
                )
            }
            TeamInviteCreateResult.WorkspaceNotFound -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        code = "workspace_not_found",
                        message = "Complete business setup before inviting team members.",
                    ),
                )
            }
            TeamInviteCreateResult.OwnerRequired -> {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        code = "owner_required",
                        message = "Only the business owner can invite team members.",
                    ),
                )
            }
        }
    }

    post("/onboarding/team-invites/join") {
        val repository = onboardingRepository ?: return@post call.databaseNotConfigured()
        val request = call.receive<TeamInviteJoinRequest>()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        if (request.displayName.isBlank() || request.displayName.trim().length < 2) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "team_profile_name_required",
                    message = "Enter your name before joining this workspace.",
                ),
            )
            return@post
        }

        when (val result = repository.joinInvite(firebaseUser, request.code, request.displayName)) {
            is TeamInviteJoinResult.Success -> call.respond(result.session.toMutationResponse(config))
            TeamInviteJoinResult.NotFound -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        code = "invite_not_found",
                        message = "Invite code is invalid or expired.",
                    ),
                )
            }
            TeamInviteJoinResult.ContactMismatch -> {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        code = "invite_contact_mismatch",
                        message = "This invite belongs to a different phone number or email. Sign in with the invited account.",
                    ),
                )
            }
        }
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

private fun TeamInviteListRecord.toResponse(): TeamInviteListItemResponse =
    TeamInviteListItemResponse(
        code = code,
        inviteeName = inviteeName,
        inviteeEmail = inviteeEmail,
        inviteePhoneNumber = inviteePhoneNumber,
        role = role,
        status = status,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )
