package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.TeamInviteRequest
import com.orma.backend.models.TeamMemberAccessRequest
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppUserRecord(
    val id: String,
    val firebaseUid: String,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val role: String,
    val notificationsEnabled: Boolean,
)

data class WorkspaceRecord(
    val id: String,
    val businessName: String,
    val legalName: String,
    val role: String,
    val onboardingComplete: Boolean,
    val logoFileName: String?,
    val coverFileName: String?,
    val website: String? = null,
    val isTaxRegistered: Boolean? = null,
    val taxNumber: String? = null,
    val taxLabel: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val invoicePrefix: String? = null,
    val nextInvoiceNumber: String? = null,
    val paymentTerms: String? = null,
    val invoiceFooter: String? = null,
    val currency: String? = null,
    val taxMode: String? = null,
    val pricesIncludeTax: Boolean? = null,
)

data class OnboardingSessionRecord(
    val user: AppUserRecord,
    val workspace: WorkspaceRecord?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

data class TeamMemberRecord(
    val id: String,
    val userId: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: String,
    val permissions: List<String>,
    val status: String,
    val joinedAt: String,
)

data class TeamInviteRecord(
    val id: String,
    val code: String,
    val inviteeName: String?,
    val inviteeEmail: String?,
    val inviteePhoneNumber: String?,
    val role: String,
    val permissions: List<String>,
    val status: String,
    val createdAt: String,
    val expiresAt: String?,
    val createdByDisplayName: String?,
    val createdByEmail: String?,
)

data class TeamOverviewRecord(
    val workspace: WorkspaceRecord,
    val canInviteMembers: Boolean,
    val members: List<TeamMemberRecord>,
    val invites: List<TeamInviteRecord>,
)

class TeamAccessException(
    val code: String,
    override val message: String,
) : RuntimeException(message)

data class ProductImageRecord(
    val id: String,
    val workspaceId: String,
    val productId: String,
    val storagePath: String,
    val originalFileName: String?,
    val contentType: String,
    val sizeBytes: Long,
)

private data class ActivityActorRecord(
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: String?,
)

class OnboardingRepository(
    private val dataSource: DataSource,
) {
    suspend fun resolveSession(
        firebaseUser: VerifiedFirebaseUser,
        providerFallback: String?,
        emailFallback: String?,
        phoneNumberFallback: String?,
        displayNameFallback: String?,
    ): OnboardingSessionRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val user = connection.upsertUser(
                firebaseUser = firebaseUser,
                providerFallback = providerFallback,
                emailFallback = emailFallback,
                phoneNumberFallback = phoneNumberFallback,
                displayNameFallback = displayNameFallback,
            )
            val workspace = connection.findPrimaryWorkspace(user.id)
            user.toSession(workspace)
        }
    }

    suspend fun completeBusinessSetup(
        firebaseUser: VerifiedFirebaseUser,
        request: BusinessSetupRequest,
    ): OnboardingSessionRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser.copy(displayName = firebaseUser.displayName ?: request.ownerName),
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = request.ownerName,
            )
            val workspace = connection.upsertWorkspaceForBusinessSetup(user.id, request)
            if (workspace.role == RoleBusinessOwner) {
                connection.ensureOwnerMembership(workspace.id, user.id)
            }
                val updatedUser = connection.markUserOwnerComplete(
                    userId = user.id,
                    displayName = request.ownerName.ifBlank { user.displayName.orEmpty() },
                    promoteToOwner = workspace.role == RoleBusinessOwner,
                )
                connection.commit()
                updatedUser.toSession(workspace)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun listWorkspaceTeam(
        firebaseUser: VerifiedFirebaseUser,
    ): TeamOverviewRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val user = connection.upsertUser(
                firebaseUser = firebaseUser,
                providerFallback = null,
                emailFallback = null,
                phoneNumberFallback = null,
                displayNameFallback = null,
            )
            val workspace = connection.findPrimaryWorkspace(user.id) ?: return@withContext null
            val canInviteMembers = workspace.role == RoleBusinessOwner
            if (canInviteMembers) {
                connection.ensureOwnerMembership(workspace.id, user.id)
            }
            TeamOverviewRecord(
                workspace = workspace,
                canInviteMembers = canInviteMembers,
                members = connection.listWorkspaceMembers(workspace.id),
                invites = if (canInviteMembers) connection.listWorkspaceInvites(workspace.id) else emptyList(),
            )
        }
    }

    suspend fun createTeamInvite(
        firebaseUser: VerifiedFirebaseUser,
        request: TeamInviteRequest,
    ): TeamOverviewRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser,
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = null,
                )
                val workspace = connection.requireOwnerWorkspace(user.id)
                val invite = connection.insertTeamInvite(workspace.id, user.id, request)
                connection.insertTeamActivity(
                    workspaceId = workspace.id,
                    actorUserId = user.id,
                    activityType = "team_invite_created",
                    entityType = "team_invite",
                    entityId = invite.id,
                    entityLabel = invite.inviteeName ?: invite.inviteeEmail ?: invite.inviteePhoneNumber ?: "Team invite",
                    title = "Team invite created",
                    body = "${teamRoleLabel(invite.role)} invite prepared for ${invite.inviteeName ?: invite.inviteeEmail ?: invite.inviteePhoneNumber ?: "a team member"}.",
                    tone = "success",
                )
                connection.commit()
                connection.teamOverview(workspace, canInviteMembers = true)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun revokeTeamInvite(
        firebaseUser: VerifiedFirebaseUser,
        inviteId: String,
    ): TeamOverviewRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser,
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = null,
                )
                val workspace = connection.requireOwnerWorkspace(user.id)
                val invite = connection.revokeTeamInvite(workspace.id, inviteId)
                if (invite != null) {
                    connection.insertTeamActivity(
                        workspaceId = workspace.id,
                        actorUserId = user.id,
                        activityType = "team_invite_revoked",
                        entityType = "team_invite",
                        entityId = invite.id,
                        entityLabel = invite.inviteeName ?: invite.inviteeEmail ?: invite.inviteePhoneNumber ?: "Team invite",
                        title = "Team invite revoked",
                        body = "${teamRoleLabel(invite.role)} invite removed for ${invite.inviteeName ?: invite.inviteeEmail ?: invite.inviteePhoneNumber ?: "a team member"}.",
                        tone = "warning",
                    )
                }
                connection.commit()
                connection.teamOverview(workspace, canInviteMembers = true)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun removeTeamMember(
        firebaseUser: VerifiedFirebaseUser,
        memberId: String,
    ): TeamOverviewRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser,
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = null,
                )
                val workspace = connection.requireOwnerWorkspace(user.id)
                val member = connection.findWorkspaceMember(workspace.id, memberId)
                    ?: throw TeamAccessException("team_member_not_found", "This team member is no longer active.")
                if (member.userId == user.id) {
                    throw TeamAccessException("team_remove_self_blocked", "Owners cannot remove their own active workspace access.")
                }
                if (member.role == RoleBusinessOwner && connection.countActiveOwners(workspace.id) <= 1) {
                    throw TeamAccessException("team_last_owner_blocked", "Keep at least one active owner on this workspace.")
                }
                connection.disableWorkspaceMember(workspace.id, member.id)
                connection.insertTeamActivity(
                    workspaceId = workspace.id,
                    actorUserId = user.id,
                    activityType = "team_member_removed",
                    entityType = "team_member",
                    entityId = member.id,
                    entityLabel = member.displayName ?: member.email ?: member.phoneNumber ?: "Team member",
                    title = "Team member removed",
                    body = "${member.displayName ?: member.email ?: member.phoneNumber ?: "Team member"} no longer has active workspace access.",
                    tone = "warning",
                )
                connection.commit()
                connection.teamOverview(workspace, canInviteMembers = true)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateTeamMemberAccess(
        firebaseUser: VerifiedFirebaseUser,
        memberId: String,
        request: TeamMemberAccessRequest,
    ): TeamOverviewRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser,
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = null,
                )
                val workspace = connection.requireOwnerWorkspace(user.id)
                val member = connection.findWorkspaceMember(workspace.id, memberId)
                    ?: throw TeamAccessException("team_member_not_found", "This team member is no longer active.")
                val nextRole = request.role.normalizedTeamRole()
                if (member.role == RoleBusinessOwner && nextRole != RoleBusinessOwner && connection.countActiveOwners(workspace.id) <= 1) {
                    throw TeamAccessException("team_last_owner_blocked", "Keep at least one active owner on this workspace.")
                }
                val permissions = request.permissions.normalizedTeamPermissions(nextRole)
                val updated = connection.updateWorkspaceMemberAccess(
                    workspaceId = workspace.id,
                    memberId = member.id,
                    role = nextRole,
                    permissions = permissions,
                )
                connection.insertTeamActivity(
                    workspaceId = workspace.id,
                    actorUserId = user.id,
                    activityType = "team_member_access_updated",
                    entityType = "team_member",
                    entityId = updated.id,
                    entityLabel = updated.displayName ?: updated.email ?: updated.phoneNumber ?: "Team member",
                    title = "Team access updated",
                    body = "${updated.displayName ?: updated.email ?: updated.phoneNumber ?: "Team member"} now has ${teamRoleLabel(updated.role)} access.",
                    tone = "success",
                )
                connection.commit()
                connection.teamOverview(workspace, canInviteMembers = true)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateNotificationPreference(
        firebaseUser: VerifiedFirebaseUser,
        enabled: Boolean,
        deviceToken: String? = null,
        platform: String? = null,
        deviceName: String? = null,
    ): OnboardingSessionRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val user = connection.upsertUser(
                firebaseUser = firebaseUser,
                providerFallback = null,
                emailFallback = null,
                phoneNumberFallback = null,
                displayNameFallback = null,
            )
            val workspace = connection.findPrimaryWorkspace(user.id)
            val updatedUser = if (enabled) {
                connection.upsertNotificationDeviceToken(
                    userId = user.id,
                    workspaceId = workspace?.id,
                    token = deviceToken,
                    platform = platform,
                    deviceName = deviceName,
                )
                connection.updateNotificationPreference(user.id, true)
            } else {
                val hasDeviceToken = !deviceToken.isNullOrBlank()
                if (hasDeviceToken) {
                    connection.disableNotificationDeviceToken(user.id, deviceToken)
                } else {
                    connection.disableNotificationDeviceTokens(user.id)
                }
                connection.updateNotificationPreference(
                    userId = user.id,
                    enabled = if (hasDeviceToken) connection.hasEnabledNotificationDeviceTokens(user.id) else false,
                )
            }
            updatedUser.toSession(workspace)
        }
    }

    suspend fun unregisterNotificationDevice(
        firebaseUser: VerifiedFirebaseUser,
        deviceToken: String?,
    ) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val user = connection.upsertUser(
                firebaseUser = firebaseUser,
                providerFallback = null,
                emailFallback = null,
                phoneNumberFallback = null,
                displayNameFallback = null,
            )
            connection.disableNotificationDeviceToken(user.id, deviceToken)
        }
    }

    suspend fun saveBusinessLogo(
        workspaceId: String,
        storagePath: String,
    ) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.updateBusinessLogo(workspaceId, storagePath)
        }
    }

    suspend fun saveBusinessCover(
        workspaceId: String,
        storagePath: String,
    ) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.updateBusinessCover(workspaceId, storagePath)
        }
    }

    suspend fun saveProductImage(
        workspaceId: String,
        userId: String,
        productId: String,
        storagePath: String,
        originalFileName: String?,
        contentType: String,
        sizeBytes: Long,
    ): ProductImageRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val record = connection.insertProductImage(
                    workspaceId = workspaceId,
                    userId = userId,
                    productId = productId,
                    storagePath = storagePath,
                    originalFileName = originalFileName,
                    contentType = contentType,
                    sizeBytes = sizeBytes,
                )
                connection.insertProductImageActivity(
                    workspaceId = workspaceId,
                    userId = userId,
                    productId = productId,
                )
                connection.commit()
                record
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun Connection.upsertUser(
        firebaseUser: VerifiedFirebaseUser,
        providerFallback: String?,
        emailFallback: String?,
        phoneNumberFallback: String?,
        displayNameFallback: String?,
    ): AppUserRecord {
        val sql = """
            insert into app_users (
                firebase_uid,
                email,
                phone_number,
                display_name,
                provider,
                role,
                onboarding_status,
                last_login_at,
                updated_at
            )
            values (?, ?, ?, ?, ?, ?, 'pending', now(), now())
            on conflict (firebase_uid) do update set
                email = coalesce(excluded.email, app_users.email),
                phone_number = coalesce(excluded.phone_number, app_users.phone_number),
                display_name = coalesce(excluded.display_name, app_users.display_name),
                provider = coalesce(excluded.provider, app_users.provider),
                last_login_at = now(),
                updated_at = now()
            returning
                id::text,
                firebase_uid,
                email,
                phone_number,
                display_name,
                role,
                notifications_enabled
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, firebaseUser.uid)
            statement.setNullableString(2, firebaseUser.email ?: emailFallback)
            statement.setNullableString(3, firebaseUser.phoneNumber ?: phoneNumberFallback)
            statement.setNullableString(4, firebaseUser.displayName ?: displayNameFallback)
            statement.setNullableString(5, firebaseUser.provider ?: providerFallback)
            statement.setString(6, RoleBusinessOwner)
            statement.executeQuery().use { result ->
                result.next()
                val user = result.toUserRecord()
                acceptMatchingTeamInvite(user)
                user
            }
        }
    }

    private fun Connection.findPrimaryWorkspace(userId: String): WorkspaceRecord? {
        val sql = """
            select
                bw.id::text,
                bw.business_name,
                bw.legal_name,
                case
                    when bw.owner_user_id = wm.user_id then 'business_owner'
                    else wm.role
                end as role,
                bw.onboarding_completed_at is not null as onboarding_complete,
                bw.logo_file_name,
                bw.cover_file_name,
                bw.website,
                bw.is_tax_registered,
                bw.tax_number,
                bw.tax_label,
                bw.address_line,
                bw.city,
                bw.region,
                bw.country,
                bw.postal_code,
                bw.invoice_prefix,
                bw.next_invoice_number,
                bw.payment_terms,
                bw.invoice_footer,
                bw.currency,
                bw.tax_mode,
                bw.prices_include_tax
            from workspace_members wm
            join business_workspaces bw on bw.id = wm.workspace_id
            where wm.user_id = ?::uuid
              and wm.status = 'active'
            order by case when bw.owner_user_id = wm.user_id or wm.role = 'business_owner' then 0 else 1 end, wm.created_at
            limit 1
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toWorkspaceRecord() else null
            }
        }
    }

    private fun Connection.upsertOwnerWorkspace(
        userId: String,
        request: BusinessSetupRequest,
    ): WorkspaceRecord {
        findOwnerWorkspaceId(userId)?.let { workspaceId ->
            return updateWorkspaceSettings(workspaceId, RoleBusinessOwner, request)
        }
        return insertOwnerWorkspace(userId, request)
    }

    private fun Connection.upsertWorkspaceForBusinessSetup(
        userId: String,
        request: BusinessSetupRequest,
    ): WorkspaceRecord {
        val workspace = findPrimaryWorkspace(userId)
        if (workspace != null && workspace.role != RoleBusinessOwner) {
            if (!hasWorkspacePermission(workspace.id, userId, PermissionManageAccount)) {
                throw TeamAccessException(
                    code = "team_permission_denied",
                    message = "This staff role cannot update account settings.",
                )
            }
            return updateWorkspaceSettings(workspace.id, workspace.role, request)
        }
        return upsertOwnerWorkspace(userId, request)
    }

    private fun Connection.hasWorkspacePermission(
        workspaceId: String,
        userId: String,
        permission: String,
    ): Boolean {
        val sql = """
            select role, permissions
            from workspace_members
            where workspace_id = ?::uuid
              and user_id = ?::uuid
              and status = 'active'
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.executeQuery().use { result ->
                if (!result.next()) return@use false
                val role = result.getString("role").normalizedTeamRole()
                if (role == RoleBusinessOwner) return@use true
                val permissions = result.getStringArray("permissions")
                    .map { it.normalizedPermissionKey() }
                    .toSet()
                PermissionReadOnly !in permissions && permission in permissions
            }
        }
    }

    private fun Connection.findOwnerWorkspaceId(userId: String): String? {
        val sql = """
            select id::text
            from business_workspaces
            where owner_user_id = ?::uuid
            order by created_at
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("id") else null
            }
        }
    }

    private fun Connection.insertOwnerWorkspace(
        userId: String,
        request: BusinessSetupRequest,
    ): WorkspaceRecord {
        val sql = """
            insert into business_workspaces (
                owner_user_id,
                business_name,
                legal_name,
                industry,
                business_mode,
                website,
                is_tax_registered,
                tax_number,
                tax_label,
                address_line,
                city,
                region,
                country,
                postal_code,
                logo_file_name,
                invoice_prefix,
                next_invoice_number,
                payment_terms,
                invoice_footer,
                currency,
                tax_mode,
                prices_include_tax,
                onboarding_completed_at,
                updated_at
            )
            values (
                ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now()
            )
            returning
                id::text,
                business_name,
                legal_name,
                'business_owner' as role,
                onboarding_completed_at is not null as onboarding_complete,
                logo_file_name,
                cover_file_name,
                website,
                is_tax_registered,
                tax_number,
                tax_label,
                address_line,
                city,
                region,
                country,
                postal_code,
                invoice_prefix,
                next_invoice_number,
                payment_terms,
                invoice_footer,
                currency,
                tax_mode,
                prices_include_tax,
                null::text as invite_code
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.bindBusinessSetup(userId, request)
            statement.executeQuery().use { result ->
                result.next()
                result.toWorkspaceRecord()
            }
        }
    }

    private fun Connection.updateWorkspaceSettings(
        workspaceId: String,
        role: String,
        request: BusinessSetupRequest,
    ): WorkspaceRecord {
        val sql = """
            update business_workspaces
            set
                business_name = ?,
                legal_name = ?,
                industry = ?,
                business_mode = ?,
                website = ?,
                is_tax_registered = ?,
                tax_number = ?,
                tax_label = ?,
                address_line = ?,
                city = ?,
                region = ?,
                country = ?,
                postal_code = ?,
                logo_file_name = ?,
                invoice_prefix = ?,
                next_invoice_number = ?,
                payment_terms = ?,
                invoice_footer = ?,
                currency = ?,
                tax_mode = ?,
                prices_include_tax = ?,
                onboarding_completed_at = coalesce(onboarding_completed_at, now()),
                updated_at = now()
            where id = ?::uuid
            returning
                id::text,
                business_name,
                legal_name,
                ? as role,
                onboarding_completed_at is not null as onboarding_complete,
                logo_file_name,
                cover_file_name,
                website,
                is_tax_registered,
                tax_number,
                tax_label,
                address_line,
                city,
                region,
                country,
                postal_code,
                invoice_prefix,
                next_invoice_number,
                payment_terms,
                invoice_footer,
                currency,
                tax_mode,
                prices_include_tax,
                null::text as invite_code
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.bindBusinessSetupUpdate(workspaceId, request)
            statement.setString(23, role.normalizedTeamRole())
            statement.executeQuery().use { result ->
                result.next()
                result.toWorkspaceRecord()
            }
        }
    }

    private fun Connection.ensureOwnerMembership(workspaceId: String, userId: String) {
        val sql = """
            insert into workspace_members (workspace_id, user_id, role, permissions, status, updated_at)
            values (?::uuid, ?::uuid, 'business_owner', ?, 'active', now())
            on conflict (workspace_id, user_id) do update set
                role = 'business_owner',
                permissions = excluded.permissions,
                status = 'active',
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.setArray(3, createArrayOf("text", RoleBusinessOwner.defaultTeamPermissions().toTypedArray()))
            statement.executeUpdate()
        }
    }

    private fun Connection.listWorkspaceMembers(workspaceId: String): List<TeamMemberRecord> {
        val sql = """
            select
                wm.id::text as member_id,
                wm.user_id::text as user_id,
                au.display_name,
                au.email,
                au.phone_number,
                case
                    when bw.owner_user_id = wm.user_id then 'business_owner'
                    else wm.role
                end as role,
                wm.permissions,
                wm.status,
                wm.created_at::text as joined_at
            from workspace_members wm
            join app_users au on au.id = wm.user_id
            join business_workspaces bw on bw.id = wm.workspace_id
            where wm.workspace_id = ?::uuid
              and wm.status = 'active'
            order by
                case when bw.owner_user_id = wm.user_id or wm.role = 'business_owner' then 0 else 1 end,
                coalesce(nullif(au.display_name, ''), au.email, au.phone_number, wm.created_at::text)
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toTeamMemberRecord())
                }
            }
        }
    }

    private fun Connection.listWorkspaceInvites(workspaceId: String): List<TeamInviteRecord> {
        val sql = """
            select
                ti.id::text as invite_id,
                ti.code,
                ti.invitee_name,
                ti.invitee_email,
                ti.invitee_phone_number,
                ti.role,
                ti.permissions,
                ti.status,
                ti.created_at::text as created_at,
                ti.expires_at::text as expires_at,
                au.display_name as created_by_display_name,
                au.email as created_by_email
            from team_invites ti
            left join app_users au on au.id = ti.created_by_user_id
            where ti.workspace_id = ?::uuid
              and ti.status in ('active', 'pending')
            order by ti.created_at desc
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toTeamInviteRecord())
                }
            }
        }
    }

    private fun Connection.teamOverview(
        workspace: WorkspaceRecord,
        canInviteMembers: Boolean,
    ): TeamOverviewRecord =
        TeamOverviewRecord(
            workspace = workspace,
            canInviteMembers = canInviteMembers,
            members = listWorkspaceMembers(workspace.id),
            invites = if (canInviteMembers) listWorkspaceInvites(workspace.id) else emptyList(),
        )

    private fun Connection.requireOwnerWorkspace(userId: String): WorkspaceRecord {
        val workspace = findPrimaryWorkspace(userId)
            ?: throw TeamAccessException(
                code = "workspace_not_found",
                message = "Complete business setup before managing team access.",
            )
        if (workspace.role != RoleBusinessOwner) {
            throw TeamAccessException(
                code = "team_owner_required",
                message = "Only the workspace owner can manage team access.",
            )
        }
        return workspace
    }

    private fun Connection.insertTeamInvite(
        workspaceId: String,
        userId: String,
        request: TeamInviteRequest,
    ): TeamInviteRecord {
        val cleanName = request.inviteeName.cleanOptional()?.take(120)
        val cleanEmail = request.inviteeEmail.cleanOptional()?.lowercase()?.take(160)
        val cleanPhone = request.inviteePhoneNumber.cleanOptional()?.take(40)
        val role = request.role.normalizedTeamRole()
        val permissions = request.permissions.normalizedTeamPermissions(role)
        if (cleanEmail == null && cleanPhone == null) {
            throw TeamAccessException(
                code = "team_invite_contact_required",
                message = "Add an email or phone number before creating the invite.",
            )
        }
        val sql = """
            insert into team_invites (
                code,
                workspace_id,
                created_by_user_id,
                role,
                permissions,
                status,
                expires_at,
                invitee_name,
                invitee_email,
                invitee_phone_number,
                updated_at
            )
            values (?, ?::uuid, ?::uuid, ?, ?, 'active', now() + interval '14 days', ?, ?, ?, now())
            returning
                id::text as invite_id,
                code,
                invitee_name,
                invitee_email,
                invitee_phone_number,
                role,
                permissions,
                status,
                created_at::text as created_at,
                expires_at::text as expires_at,
                null::text as created_by_display_name,
                null::text as created_by_email
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, generateTeamInviteCode())
            statement.setString(2, workspaceId)
            statement.setString(3, userId)
            statement.setString(4, role)
            statement.setArray(5, createArrayOf("text", permissions.toTypedArray()))
            statement.setNullableString(6, cleanName)
            statement.setNullableString(7, cleanEmail)
            statement.setNullableString(8, cleanPhone)
            statement.executeQuery().use { result ->
                result.next()
                result.toTeamInviteRecord()
            }
        }
    }

    private fun Connection.revokeTeamInvite(
        workspaceId: String,
        inviteId: String,
    ): TeamInviteRecord? {
        val sql = """
            update team_invites
            set status = 'revoked', updated_at = now()
            where id::text = ?
              and workspace_id = ?::uuid
              and status in ('active', 'pending')
            returning
                id::text as invite_id,
                code,
                invitee_name,
                invitee_email,
                invitee_phone_number,
                role,
                permissions,
                status,
                created_at::text as created_at,
                expires_at::text as expires_at,
                null::text as created_by_display_name,
                null::text as created_by_email
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, inviteId.trim())
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toTeamInviteRecord() else null
            }
        }
    }

    private fun Connection.findWorkspaceMember(
        workspaceId: String,
        memberId: String,
    ): TeamMemberRecord? {
        val sql = """
            select
                wm.id::text as member_id,
                wm.user_id::text as user_id,
                au.display_name,
                au.email,
                au.phone_number,
                wm.role,
                wm.permissions,
                wm.status,
                wm.created_at::text as joined_at
            from workspace_members wm
            join app_users au on au.id = wm.user_id
            where wm.workspace_id = ?::uuid
              and wm.id::text = ?
              and wm.status = 'active'
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, memberId.trim())
            statement.executeQuery().use { result ->
                if (result.next()) result.toTeamMemberRecord() else null
            }
        }
    }

    private fun Connection.countActiveOwners(workspaceId: String): Int {
        val sql = """
            select count(*)::int as owner_count
            from workspace_members
            where workspace_id = ?::uuid
              and role = 'business_owner'
              and status = 'active'
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                result.getInt("owner_count")
            }
        }
    }

    private fun Connection.disableWorkspaceMember(
        workspaceId: String,
        memberId: String,
    ) {
        val sql = """
            update workspace_members
            set status = 'disabled', updated_at = now()
            where workspace_id = ?::uuid
              and id::text = ?
              and status = 'active'
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, memberId.trim())
            statement.executeUpdate()
        }
    }

    private fun Connection.updateWorkspaceMemberAccess(
        workspaceId: String,
        memberId: String,
        role: String,
        permissions: List<String>,
    ): TeamMemberRecord {
        val sql = """
            update workspace_members
            set role = ?, permissions = ?, updated_at = now()
            where workspace_id = ?::uuid
              and id::text = ?
              and status = 'active'
            returning
                id::text as member_id,
                user_id::text as user_id,
                null::text as display_name,
                null::text as email,
                null::text as phone_number,
                role,
                permissions,
                status,
                created_at::text as joined_at
        """.trimIndent()
        val updated = prepareStatement(sql).use { statement ->
            statement.setString(1, role)
            statement.setArray(2, createArrayOf("text", permissions.toTypedArray()))
            statement.setString(3, workspaceId)
            statement.setString(4, memberId.trim())
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    throw TeamAccessException("team_member_not_found", "This team member is no longer active.")
                }
                result.toTeamMemberRecord()
            }
        }
        return findWorkspaceMember(workspaceId, updated.id) ?: updated
    }

    private fun Connection.acceptMatchingTeamInvite(user: AppUserRecord) {
        val email = user.email.cleanOptional()?.lowercase()
        val phoneDigits = user.phoneNumber.cleanOptional()?.filter { it.isDigit() }
        if (email == null && phoneDigits.isNullOrBlank()) return
        val sql = """
            with matched_invite as (
                select
                    id,
                    workspace_id,
                    role,
                    permissions
                from team_invites
                where status in ('active', 'pending')
                  and (expires_at is null or expires_at > now())
                  and (
                    (?::text is not null and lower(invitee_email) = ?)
                    or (?::text is not null and regexp_replace(coalesce(invitee_phone_number, ''), '[^0-9]', '', 'g') = ?)
                  )
                order by created_at desc
                limit 1
            ),
            upserted_member as (
                insert into workspace_members (workspace_id, user_id, role, permissions, status, updated_at)
                select workspace_id, ?::uuid, role, permissions, 'active', now()
                from matched_invite
                on conflict (workspace_id, user_id) do update set
                    role = excluded.role,
                    permissions = excluded.permissions,
                    status = 'active',
                    updated_at = now()
                returning workspace_id
            )
            update team_invites
            set status = 'accepted', updated_at = now()
            where id in (select id from matched_invite)
              and exists (select 1 from upserted_member)
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setNullableString(1, email)
            statement.setNullableString(2, email)
            statement.setNullableString(3, phoneDigits)
            statement.setNullableString(4, phoneDigits)
            statement.setString(5, user.id)
            statement.executeUpdate()
        }
    }

    private fun Connection.markUserOwnerComplete(
        userId: String,
        displayName: String,
        promoteToOwner: Boolean,
    ): AppUserRecord {
        val sql = """
            update app_users
            set
                role = case when ? then 'business_owner' else role end,
                display_name = ?,
                onboarding_status = 'complete',
                updated_at = now()
            where id = ?::uuid
            returning
                id::text,
                firebase_uid,
                email,
                phone_number,
                display_name,
                role,
                notifications_enabled
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setBoolean(1, promoteToOwner)
            statement.setString(2, displayName)
            statement.setString(3, userId)
            statement.executeQuery().use { result ->
                result.next()
                result.toUserRecord()
            }
        }
    }

    private fun Connection.updateNotificationPreference(
        userId: String,
        enabled: Boolean,
    ): AppUserRecord {
        val sql = """
            update app_users
            set
                notifications_enabled = ?,
                updated_at = now()
            where id = ?::uuid
            returning
                id::text,
                firebase_uid,
                email,
                phone_number,
                display_name,
                role,
                notifications_enabled
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setBoolean(1, enabled)
            statement.setString(2, userId)
            statement.executeQuery().use { result ->
                result.next()
                result.toUserRecord()
            }
        }
    }

    private fun Connection.upsertNotificationDeviceToken(
        userId: String,
        workspaceId: String?,
        token: String?,
        platform: String?,
        deviceName: String?,
    ) {
        val cleanToken = token?.trim()?.takeIf { it.isNotBlank() } ?: return
        val cleanWorkspaceId = workspaceId?.trim()?.takeIf { it.isNotBlank() } ?: return
        val cleanPlatform = platform?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "unknown"
        val cleanDeviceName = deviceName?.trim()?.takeIf { it.isNotBlank() }?.take(120)
        prepareStatement(
            """
            insert into notification_device_tokens (
                user_id, workspace_id, token, platform, device_name, enabled,
                last_seen_at, updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, true, now(), now())
            on conflict (token) do update set
                user_id = excluded.user_id,
                workspace_id = excluded.workspace_id,
                platform = excluded.platform,
                device_name = excluded.device_name,
                enabled = true,
                last_seen_at = now(),
                updated_at = now()
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.setString(2, cleanWorkspaceId)
            statement.setString(3, cleanToken)
            statement.setString(4, cleanPlatform)
            statement.setNullableString(5, cleanDeviceName)
            statement.executeUpdate()
        }
    }

    private fun Connection.disableNotificationDeviceTokens(userId: String) {
        prepareStatement(
            """
            update notification_device_tokens
            set enabled = false, updated_at = now()
            where user_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeUpdate()
        }
    }

    private fun Connection.disableNotificationDeviceToken(userId: String, token: String?) {
        val cleanToken = token?.trim()?.takeIf { it.isNotBlank() } ?: return
        prepareStatement(
            """
            update notification_device_tokens
            set enabled = false, updated_at = now()
            where user_id = ?::uuid and token = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.setString(2, cleanToken)
            statement.executeUpdate()
        }
    }

    private fun Connection.hasEnabledNotificationDeviceTokens(userId: String): Boolean {
        return prepareStatement(
            """
            select exists (
                select 1
                from notification_device_tokens
                where user_id = ?::uuid
                  and enabled = true
                limit 1
            )
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { result ->
                result.next()
                result.getBoolean(1)
            }
        }
    }

    private fun Connection.updateBusinessLogo(
        workspaceId: String,
        storagePath: String,
    ) {
        val sql = """
            update business_workspaces
            set logo_file_name = ?, updated_at = now()
            where id = ?::uuid
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, storagePath)
            statement.setString(2, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.updateBusinessCover(
        workspaceId: String,
        storagePath: String,
    ) {
        val sql = """
            update business_workspaces
            set cover_file_name = ?, updated_at = now()
            where id = ?::uuid
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, storagePath)
            statement.setString(2, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.insertProductImage(
        workspaceId: String,
        userId: String,
        productId: String,
        storagePath: String,
        originalFileName: String?,
        contentType: String,
        sizeBytes: Long,
    ): ProductImageRecord {
        val sql = """
            insert into product_images (
                workspace_id,
                product_id,
                storage_path,
                original_file_name,
                content_type,
                size_bytes,
                created_by_user_id,
                updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?::uuid, now())
            returning
                id::text,
                workspace_id::text,
                product_id,
                storage_path,
                original_file_name,
                content_type,
                size_bytes
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, productId.trim())
            statement.setString(3, storagePath)
            statement.setNullableString(4, originalFileName?.trim()?.ifBlank { null })
            statement.setString(5, contentType)
            statement.setLong(6, sizeBytes)
            statement.setString(7, userId)
            statement.executeQuery().use { result ->
                result.next()
                result.toProductImageRecord()
            }
        }
    }

    private fun Connection.insertProductImageActivity(
        workspaceId: String,
        userId: String,
        productId: String,
    ) {
        val product = findProductActivityTarget(workspaceId, productId)
        val actor = findActivityActor(workspaceId, userId)
        val productName = product?.first ?: "Catalog item"
        val itemType = product?.second ?: "product"
        val sql = """
            insert into workspace_activity (
                workspace_id, actor_user_id, actor_display_name, actor_email, actor_phone_number,
                actor_role, activity_type, entity_type, entity_id, entity_label, title, body, tone
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?,
                ?, 'product_image_uploaded', ?, ?::uuid, ?, ?, ?, 'success'
            )
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.setNullableString(3, actor?.displayName)
            statement.setNullableString(4, actor?.email?.lowercase())
            statement.setNullableString(5, actor?.phoneNumber)
            statement.setNullableString(6, actor?.role)
            statement.setString(7, itemType.cleanActivityType())
            statement.setNullableUuid(8, productId)
            statement.setString(9, productName)
            statement.setString(10, "${itemType.sellableActivityLabel()} image uploaded")
            statement.setString(11, productName)
            statement.executeUpdate()
        }
    }

    private fun Connection.findProductActivityTarget(
        workspaceId: String,
        productId: String,
    ): Pair<String, String>? {
        val sql = """
            select name, item_type
            from products
            where workspace_id = ?::uuid
              and id::text = ?
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, productId.trim())
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("name") to (result.getString("item_type") ?: "product") else null
            }
        }
    }

    private fun Connection.findActivityActor(
        workspaceId: String,
        userId: String,
    ): ActivityActorRecord? {
        val sql = """
            select
                au.display_name,
                au.email,
                au.phone_number,
                wm.role
            from app_users au
            left join workspace_members wm on wm.user_id = au.id
                and wm.workspace_id = ?::uuid
                and wm.status = 'active'
            where au.id = ?::uuid
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    ActivityActorRecord(
                        displayName = result.getString("display_name"),
                        email = result.getString("email"),
                        phoneNumber = result.getString("phone_number"),
                        role = result.getString("role")?.normalizedTeamRole(),
                    )
                }
            }
        }
    }

    private fun Connection.insertTeamActivity(
        workspaceId: String,
        actorUserId: String,
        activityType: String,
        entityType: String,
        entityId: String?,
        entityLabel: String?,
        title: String,
        body: String,
        tone: String,
    ) {
        val actor = findActivityActor(workspaceId, actorUserId)
        val sql = """
            insert into workspace_activity (
                workspace_id, actor_user_id, actor_display_name, actor_email, actor_phone_number,
                actor_role, activity_type, entity_type, entity_id, entity_label, title, body, tone
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?,
                ?, ?, ?, ?::uuid, ?, ?, ?, ?
            )
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, actorUserId)
            statement.setNullableString(3, actor?.displayName.cleanOptional())
            statement.setNullableString(4, actor?.email.cleanOptional()?.lowercase())
            statement.setNullableString(5, actor?.phoneNumber.cleanOptional())
            statement.setNullableString(6, actor?.role.cleanOptional())
            statement.setString(7, activityType.cleanActivityType())
            statement.setString(8, entityType.cleanActivityType())
            statement.setNullableUuid(9, entityId)
            statement.setNullableString(10, entityLabel.cleanOptional())
            statement.setString(11, title.cleanOptional() ?: "Team access updated")
            statement.setString(12, body.cleanOptional() ?: "Team access was updated.")
            statement.setString(13, tone.cleanActivityTone())
            statement.executeUpdate()
        }
    }

    private fun PreparedStatement.bindBusinessSetup(
        userId: String,
        request: BusinessSetupRequest,
    ) {
        setString(1, userId)
        setString(2, request.businessName.trim())
        setString(3, request.legalName.trim())
        setString(4, request.industry.trim())
        setString(5, request.businessMode.cleanBusinessMode(request.industry))
        setNullableString(6, request.website.trim().ifBlank { null })
        setBoolean(7, request.isTaxRegistered)
        setNullableString(8, request.taxNumber.trim().ifBlank { null })
        setString(9, request.taxLabel.trim())
        setString(10, request.addressLine.trim())
        setString(11, request.city.trim())
        setNullableString(12, request.region.trim().ifBlank { null })
        setString(13, request.country.trim())
        setNullableString(14, request.postalCode.trim().ifBlank { null })
        setNullableString(15, request.logoFileName.trim().ifBlank { null })
        setString(16, request.invoicePrefix.trim().uppercase())
        setString(17, request.nextInvoiceNumber.trim())
        setString(18, request.paymentTerms.trim())
        setString(19, request.invoiceFooter.trim())
        setString(20, request.currency.trim().uppercase())
        setString(21, request.taxMode.trim())
        setBoolean(22, request.pricesIncludeTax)
    }

    private fun PreparedStatement.bindBusinessSetupUpdate(
        workspaceId: String,
        request: BusinessSetupRequest,
    ) {
        setString(1, request.businessName.trim())
        setString(2, request.legalName.trim())
        setString(3, request.industry.trim())
        setString(4, request.businessMode.cleanBusinessMode(request.industry))
        setNullableString(5, request.website.trim().ifBlank { null })
        setBoolean(6, request.isTaxRegistered)
        setNullableString(7, request.taxNumber.trim().ifBlank { null })
        setString(8, request.taxLabel.trim())
        setString(9, request.addressLine.trim())
        setString(10, request.city.trim())
        setNullableString(11, request.region.trim().ifBlank { null })
        setString(12, request.country.trim())
        setNullableString(13, request.postalCode.trim().ifBlank { null })
        setNullableString(14, request.logoFileName.trim().ifBlank { null })
        setString(15, request.invoicePrefix.trim().uppercase())
        setString(16, request.nextInvoiceNumber.trim())
        setString(17, request.paymentTerms.trim())
        setString(18, request.invoiceFooter.trim())
        setString(19, request.currency.trim().uppercase())
        setString(20, request.taxMode.trim())
        setBoolean(21, request.pricesIncludeTax)
        setString(22, workspaceId)
    }

    private fun ResultSet.toUserRecord(): AppUserRecord =
        AppUserRecord(
            id = getString("id"),
            firebaseUid = getString("firebase_uid"),
            email = getString("email"),
            phoneNumber = getString("phone_number"),
            displayName = getString("display_name"),
            role = getString("role"),
            notificationsEnabled = getBoolean("notifications_enabled"),
        )

    private fun ResultSet.toWorkspaceRecord(): WorkspaceRecord =
        WorkspaceRecord(
            id = getString("id"),
            businessName = getString("business_name"),
            legalName = getString("legal_name"),
            role = runCatching { getString("role") }.getOrNull()
                ?: runCatching { getString("workspace_role") }.getOrNull()
                ?: RoleTeamMember,
            onboardingComplete = getBoolean("onboarding_complete"),
            logoFileName = getString("logo_file_name"),
            coverFileName = runCatching { getString("cover_file_name") }.getOrNull(),
            website = runCatching { getString("website") }.getOrNull(),
            isTaxRegistered = runCatching { getBoolean("is_tax_registered") }.getOrNull(),
            taxNumber = runCatching { getString("tax_number") }.getOrNull(),
            taxLabel = runCatching { getString("tax_label") }.getOrNull(),
            addressLine = runCatching { getString("address_line") }.getOrNull(),
            city = runCatching { getString("city") }.getOrNull(),
            region = runCatching { getString("region") }.getOrNull(),
            country = runCatching { getString("country") }.getOrNull(),
            postalCode = runCatching { getString("postal_code") }.getOrNull(),
            invoicePrefix = runCatching { getString("invoice_prefix") }.getOrNull(),
            nextInvoiceNumber = runCatching { getString("next_invoice_number") }.getOrNull(),
            paymentTerms = runCatching { getString("payment_terms") }.getOrNull(),
            invoiceFooter = runCatching { getString("invoice_footer") }.getOrNull(),
            currency = runCatching { getString("currency") }.getOrNull(),
            taxMode = runCatching { getString("tax_mode") }.getOrNull(),
            pricesIncludeTax = runCatching { getBoolean("prices_include_tax") }.getOrNull(),
        )

    private fun ResultSet.toTeamMemberRecord(): TeamMemberRecord =
        getString("role").normalizedTeamRole().let { normalizedRole ->
            TeamMemberRecord(
                id = getString("member_id"),
                userId = getString("user_id"),
                displayName = getString("display_name"),
                email = getString("email"),
                phoneNumber = getString("phone_number"),
                role = normalizedRole,
                permissions = getStringArray("permissions").normalizedTeamPermissions(normalizedRole),
                status = getString("status"),
                joinedAt = getString("joined_at"),
            )
        }

    private fun ResultSet.toTeamInviteRecord(): TeamInviteRecord =
        getString("role").normalizedTeamRole().let { normalizedRole ->
            TeamInviteRecord(
                id = getString("invite_id"),
                code = getString("code"),
                inviteeName = getString("invitee_name"),
                inviteeEmail = getString("invitee_email"),
                inviteePhoneNumber = getString("invitee_phone_number"),
                role = normalizedRole,
                permissions = getStringArray("permissions").normalizedTeamPermissions(normalizedRole),
                status = getString("status"),
                createdAt = getString("created_at"),
                expiresAt = getString("expires_at"),
                createdByDisplayName = getString("created_by_display_name"),
                createdByEmail = getString("created_by_email"),
            )
        }

    private fun ResultSet.toProductImageRecord(): ProductImageRecord =
        ProductImageRecord(
            id = getString("id"),
            workspaceId = getString("workspace_id"),
            productId = getString("product_id"),
            storagePath = getString("storage_path"),
            originalFileName = getString("original_file_name"),
            contentType = getString("content_type"),
            sizeBytes = getLong("size_bytes"),
        )

    private fun AppUserRecord.toSession(
        workspace: WorkspaceRecord?,
    ): OnboardingSessionRecord {
        val accessPath = if (workspace?.role == RoleTeamMember || role == RoleTeamMember) {
            RoleTeamMember
        } else {
            RoleBusinessOwner
        }
        val onboardingStatus = when {
            workspace == null -> "new_account"
            workspace.role == RoleTeamMember -> "team_member_ready"
            workspace.onboardingComplete -> "complete"
            else -> "business_setup_required"
        }
        val requiredStep = when (onboardingStatus) {
            "new_account" -> "owner"
            "business_setup_required" -> "business_setup"
            else -> "complete"
        }
        return OnboardingSessionRecord(
            user = this,
            workspace = workspace,
            onboardingStatus = onboardingStatus,
            requiredStep = requiredStep,
            accessPath = accessPath,
        )
    }

    private fun PreparedStatement.setNullableString(index: Int, value: String?) {
        if (value.isNullOrBlank()) {
            setString(index, null)
        } else {
            setString(index, value)
        }
    }

    private fun PreparedStatement.setNullableUuid(index: Int, value: String?) {
        val cleanValue = value?.trim()?.takeIf { it.isNotBlank() }
        if (cleanValue == null || runCatching { java.util.UUID.fromString(cleanValue) }.isFailure) {
            setString(index, null)
        } else {
            setString(index, cleanValue)
        }
    }

    private fun String?.cleanOptional(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }

    private fun ResultSet.getStringArray(column: String): List<String> =
        runCatching {
            val value = getArray(column)?.array ?: return@runCatching emptyList()
            when (value) {
                is Array<*> -> value.mapNotNull { it?.toString() }
                else -> emptyList()
            }
        }.getOrDefault(emptyList())

    private fun String.cleanActivityType(): String =
        trim()
            .lowercase()
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(60)
            .ifBlank { "activity" }

    private fun String.cleanActivityTone(): String =
        trim().lowercase().takeIf { it in setOf("info", "success", "warning", "danger") } ?: "info"

    private fun String.sellableActivityLabel(): String =
        when (cleanActivityType()) {
            "service" -> "Service"
            "appointment" -> "Appointment"
            else -> "Product"
        }

    private fun teamRoleLabel(role: String): String = when (role.normalizedTeamRole()) {
        RoleBusinessOwner -> "Business owner"
        "manager" -> "Manager"
        "cashier" -> "Cashier"
        "accountant" -> "Accountant"
        "inventory_manager" -> "Inventory"
        "sales_staff" -> "Sales"
        "read_only" -> "Read only"
        else -> "Staff"
    }

    private fun generateTeamInviteCode(): String =
        "TEAM-" + UUID.randomUUID().toString().replace("-", "").take(12).uppercase()

    private fun String.normalizedTeamRole(): String {
        val normalized = trim()
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedTeamRoles) normalized else RoleTeamMember
    }

    private fun List<String>.normalizedTeamPermissions(role: String): List<String> {
        val cleanPermissions = map { it.normalizedPermissionKey() }
            .filter { it in AllowedTeamPermissions }
            .distinct()
        return cleanPermissions.ifEmpty { role.defaultTeamPermissions() }
    }

    private fun String.defaultTeamPermissions(): List<String> = when (normalizedTeamRole()) {
        RoleBusinessOwner -> AllowedTeamPermissions.toList()
        "manager" -> listOf(
            PermissionCreateSale,
            PermissionEditSale,
            PermissionChangeBookingStatus,
            PermissionCreateProduct,
            PermissionCreateService,
            PermissionCreateAppointment,
            PermissionCreateOffer,
            PermissionManageStock,
            PermissionManageCustomers,
            PermissionManageAccount,
            PermissionManageMarketing,
            PermissionDownloadInvoice,
        )
        "cashier" -> listOf(
            PermissionCreateSale,
            PermissionChangeBookingStatus,
            PermissionDownloadInvoice,
        )
        "accountant" -> listOf(PermissionDownloadInvoice)
        "inventory_manager" -> listOf(
            PermissionCreateProduct,
            PermissionCreateService,
            PermissionCreateAppointment,
            PermissionCreateOffer,
            PermissionManageStock,
        )
        "sales_staff" -> listOf(
            PermissionCreateSale,
            PermissionEditSale,
            PermissionChangeBookingStatus,
            PermissionManageCustomers,
            PermissionDownloadInvoice,
        )
        "read_only" -> listOf(PermissionReadOnly)
        else -> listOf(PermissionReadOnly)
    }

    private fun String.normalizedPermissionKey(): String =
        trim()
            .lowercase()
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }

    private fun String.cleanBusinessMode(industry: String): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        if (normalized in AllowedBusinessModes) return normalized
        return when (industry.trim().lowercase()) {
            "services", "service", "repair", "professional services", "professional service" -> "service_selling"
            "salon", "healthcare", "clinic", "fitness", "education" -> "appointment"
            "b2b" -> "mixed"
            else -> "product_selling"
        }
    }

    private companion object {
        const val RoleBusinessOwner = "business_owner"
        const val RoleTeamMember = "team_member"
        const val PermissionReadOnly = "read_only"
        const val PermissionCreateSale = "create_sale"
        const val PermissionEditSale = "edit_sale"
        const val PermissionChangeBookingStatus = "change_booking_status"
        const val PermissionCreateProduct = "create_product"
        const val PermissionCreateService = "create_service"
        const val PermissionCreateAppointment = "create_appointment"
        const val PermissionCreateOffer = "create_offer"
        const val PermissionManageStock = "manage_stock"
        const val PermissionManageCustomers = "manage_customers"
        const val PermissionManageAccount = "manage_account"
        const val PermissionManageMarketing = "manage_marketing"
        const val PermissionDownloadInvoice = "download_invoice"
        val AllowedTeamRoles = setOf(
            RoleBusinessOwner,
            RoleTeamMember,
            "manager",
            "cashier",
            "accountant",
            "inventory_manager",
            "sales_staff",
            "read_only",
        )
        val AllowedTeamPermissions = setOf(
            PermissionCreateSale,
            PermissionEditSale,
            PermissionChangeBookingStatus,
            PermissionCreateProduct,
            PermissionCreateService,
            PermissionCreateAppointment,
            PermissionCreateOffer,
            PermissionManageStock,
            PermissionManageCustomers,
            PermissionManageAccount,
            PermissionManageMarketing,
            PermissionDownloadInvoice,
            PermissionReadOnly,
        )
        val AllowedBusinessModes = setOf("product_selling", "service_selling", "appointment", "mixed")
    }
}
