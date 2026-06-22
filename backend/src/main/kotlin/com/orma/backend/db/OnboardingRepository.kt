package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.BusinessSetupRequest
import com.orma.backend.models.TeamInviteCreateRequest
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
    val inviteCode: String?,
)

data class OnboardingSessionRecord(
    val user: AppUserRecord,
    val workspace: WorkspaceRecord?,
    val pendingInvite: TeamInviteRecord?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
)

sealed interface OwnerTeamInviteResult {
    data class Success(val workspace: WorkspaceRecord) : OwnerTeamInviteResult
    data object WorkspaceNotFound : OwnerTeamInviteResult
    data object OwnerRequired : OwnerTeamInviteResult
}

data class TeamInviteRecord(
    val code: String,
    val workspace: WorkspaceRecord,
    val inviteeName: String,
    val inviteeEmail: String?,
    val inviteePhoneNumber: String?,
    val role: String,
)

data class TeamMemberRecord(
    val id: String,
    val userId: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: String,
    val status: String,
    val joinedAt: String,
)

data class TeamInviteListRecord(
    val code: String,
    val inviteeName: String,
    val inviteeEmail: String?,
    val inviteePhoneNumber: String?,
    val role: String,
    val status: String,
    val createdAt: String,
    val expiresAt: String?,
)

data class TeamOverviewRecord(
    val workspace: WorkspaceRecord,
    val canInviteMembers: Boolean,
    val members: List<TeamMemberRecord>,
    val pendingInvites: List<TeamInviteListRecord>,
)

sealed interface TeamInviteCreateResult {
    data class Success(val invite: TeamInviteRecord) : TeamInviteCreateResult
    data object WorkspaceNotFound : TeamInviteCreateResult
    data object OwnerRequired : TeamInviteCreateResult
}

sealed interface TeamInviteJoinResult {
    data class Success(val session: OnboardingSessionRecord) : TeamInviteJoinResult
    data object NotFound : TeamInviteJoinResult
    data object ContactMismatch : TeamInviteJoinResult
}

data class ProductImageRecord(
    val id: String,
    val workspaceId: String,
    val productId: String,
    val storagePath: String,
    val originalFileName: String?,
    val contentType: String,
    val sizeBytes: Long,
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
            val pendingInvite = if (workspace == null) {
                connection.findPendingInviteForUser(user)
            } else {
                null
            }
            user.toSession(workspace, pendingInvite)
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
                val workspace = connection.upsertOwnerWorkspace(user.id, request)
                connection.ensureOwnerMembership(workspace.id, user.id)
                val inviteCode = connection.ensurePilotInvite(workspace.id, user.id, request.businessName)
                val updatedUser = connection.markUserOwnerComplete(user.id, request.ownerName)
                connection.commit()
                updatedUser.toSession(workspace.copy(inviteCode = inviteCode), null)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun lookupInvite(code: String): WorkspaceRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.findInviteWorkspace(code.normalizedInviteCode())
        }
    }

    suspend fun lookupInviteRecord(code: String): TeamInviteRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.findInviteRecord(code.normalizedInviteCode())
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
            TeamOverviewRecord(
                workspace = workspace,
                canInviteMembers = canInviteMembers,
                members = connection.listWorkspaceMembers(workspace.id),
                pendingInvites = if (canInviteMembers) {
                    connection.listWorkspacePendingInvites(workspace.id)
                } else {
                    emptyList()
                },
            )
        }
    }

    suspend fun getOrCreateOwnerTeamInvite(
        firebaseUser: VerifiedFirebaseUser,
    ): OwnerTeamInviteResult = withContext(Dispatchers.IO) {
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
                val workspace = connection.findPrimaryWorkspace(user.id)
                if (workspace == null) {
                    connection.rollback()
                    return@withContext OwnerTeamInviteResult.WorkspaceNotFound
                }
                if (workspace.role != RoleBusinessOwner) {
                    connection.rollback()
                    return@withContext OwnerTeamInviteResult.OwnerRequired
                }

                val inviteCode = connection.ensurePilotInvite(
                    workspaceId = workspace.id,
                    userId = user.id,
                    businessName = workspace.businessName,
                )
                connection.commit()
                OwnerTeamInviteResult.Success(workspace.copy(inviteCode = inviteCode))
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun createOwnerTeamInvite(
        firebaseUser: VerifiedFirebaseUser,
        request: TeamInviteCreateRequest,
    ): TeamInviteCreateResult = withContext(Dispatchers.IO) {
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
                val workspace = connection.findPrimaryWorkspace(user.id)
                if (workspace == null) {
                    connection.rollback()
                    return@withContext TeamInviteCreateResult.WorkspaceNotFound
                }
                if (workspace.role != RoleBusinessOwner) {
                    connection.rollback()
                    return@withContext TeamInviteCreateResult.OwnerRequired
                }

                val invite = connection.createInviteCode(
                    workspace = workspace,
                    userId = user.id,
                    request = request,
                )
                connection.commit()
                TeamInviteCreateResult.Success(invite)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun joinInvite(
        firebaseUser: VerifiedFirebaseUser,
        code: String,
        displayName: String,
    ): TeamInviteJoinResult = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val user = connection.upsertUser(
                    firebaseUser = firebaseUser.copy(displayName = firebaseUser.displayName ?: displayName.trim().take(120)),
                    providerFallback = null,
                    emailFallback = null,
                    phoneNumberFallback = null,
                    displayNameFallback = displayName.trim().take(120),
                )
                val invite = connection.findInviteRecord(code.normalizedInviteCode())
                if (invite == null) {
                    connection.rollback()
                    return@withContext TeamInviteJoinResult.NotFound
                }
                if (!invite.canBeClaimedBy(user)) {
                    connection.rollback()
                    return@withContext TeamInviteJoinResult.ContactMismatch
                }
                connection.ensureTeamMembership(invite.workspace.id, user.id, invite.role)
                connection.markInviteAccepted(invite.code)
                val updatedUser = connection.markUserTeamComplete(user.id, displayName.trim().take(120))
                connection.commit()
                TeamInviteJoinResult.Success(updatedUser.toSession(invite.workspace.copy(inviteCode = invite.code), null))
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
    ): OnboardingSessionRecord = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val user = connection.upsertUser(
                firebaseUser = firebaseUser,
                providerFallback = null,
                emailFallback = null,
                phoneNumberFallback = null,
                displayNameFallback = null,
            )
            val updatedUser = connection.updateNotificationPreference(user.id, enabled)
            val workspace = connection.findPrimaryWorkspace(updatedUser.id)
            updatedUser.toSession(workspace, null)
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
            connection.insertProductImage(
                workspaceId = workspaceId,
                userId = userId,
                productId = productId,
                storagePath = storagePath,
                originalFileName = originalFileName,
                contentType = contentType,
                sizeBytes = sizeBytes,
            )
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
                result.toUserRecord()
            }
        }
    }

    private fun Connection.findPrimaryWorkspace(userId: String): WorkspaceRecord? {
        val sql = """
            select
                bw.id::text,
                bw.business_name,
                bw.legal_name,
                wm.role,
                bw.onboarding_completed_at is not null as onboarding_complete,
                bw.logo_file_name,
                ti.code as invite_code
            from workspace_members wm
            join business_workspaces bw on bw.id = wm.workspace_id
            left join lateral (
                select code
                from team_invites
                where workspace_id = bw.id
                  and status = 'active'
                  and (expires_at is null or expires_at > now())
                order by created_at desc
                limit 1
            ) ti on true
            where wm.user_id = ?::uuid
              and wm.status = 'active'
            order by case when wm.role = 'business_owner' then 0 else 1 end, wm.created_at
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
            return updateOwnerWorkspace(workspaceId, request)
        }
        return insertOwnerWorkspace(userId, request)
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
                ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now()
            )
            returning
                id::text,
                business_name,
                legal_name,
                'business_owner' as role,
                onboarding_completed_at is not null as onboarding_complete,
                logo_file_name,
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

    private fun Connection.updateOwnerWorkspace(
        workspaceId: String,
        request: BusinessSetupRequest,
    ): WorkspaceRecord {
        val sql = """
            update business_workspaces
            set
                business_name = ?,
                legal_name = ?,
                industry = ?,
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
                'business_owner' as role,
                onboarding_completed_at is not null as onboarding_complete,
                logo_file_name,
                null::text as invite_code
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.bindBusinessSetupUpdate(workspaceId, request)
            statement.executeQuery().use { result ->
                result.next()
                result.toWorkspaceRecord()
            }
        }
    }

    private fun Connection.ensureOwnerMembership(workspaceId: String, userId: String) {
        val sql = """
            insert into workspace_members (workspace_id, user_id, role, status, updated_at)
            values (?::uuid, ?::uuid, 'business_owner', 'active', now())
            on conflict (workspace_id, user_id) do update set
                role = 'business_owner',
                status = 'active',
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.executeUpdate()
        }
    }

    private fun Connection.ensureTeamMembership(
        workspaceId: String,
        userId: String,
        role: String,
    ) {
        val sql = """
            insert into workspace_members (workspace_id, user_id, role, status, updated_at)
            values (?::uuid, ?::uuid, ?, 'active', now())
            on conflict (workspace_id, user_id) do update set
                role = excluded.role,
                status = 'active',
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.setString(3, role.normalizedTeamRole())
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
                wm.role,
                wm.status,
                wm.created_at::text as joined_at
            from workspace_members wm
            join app_users au on au.id = wm.user_id
            where wm.workspace_id = ?::uuid
              and wm.status = 'active'
            order by
                case when wm.role = 'business_owner' then 0 else 1 end,
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

    private fun Connection.listWorkspacePendingInvites(workspaceId: String): List<TeamInviteListRecord> {
        val sql = """
            select
                code,
                invitee_name,
                invitee_email,
                invitee_phone_number,
                role,
                status,
                created_at::text as created_at,
                expires_at::text as expires_at
            from team_invites
            where workspace_id = ?::uuid
              and status = 'active'
              and (expires_at is null or expires_at > now())
              and (
                    nullif(invitee_name, '') is not null
                 or nullif(invitee_email, '') is not null
                 or nullif(invitee_phone_number, '') is not null
              )
            order by created_at desc
        """.trimIndent()

        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toTeamInviteListRecord())
                }
            }
        }
    }

    private fun Connection.ensurePilotInvite(
        workspaceId: String,
        userId: String,
        businessName: String,
    ): String {
        findActiveInviteCode(workspaceId)?.let { return it }
        repeat(5) {
            val code = generateInviteCode(businessName)
            val insertedCode = insertInviteCode(workspaceId, userId, code)
            if (insertedCode != null) return insertedCode
        }
        error("Unable to create a unique team invite code.")
    }

    private fun Connection.findActiveInviteCode(workspaceId: String): String? {
        val sql = """
            select code
            from team_invites
            where workspace_id = ?::uuid
              and status = 'active'
              and (expires_at is null or expires_at > now())
            order by created_at desc
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("code") else null
            }
        }
    }

    private fun Connection.insertInviteCode(
        workspaceId: String,
        userId: String,
        code: String,
    ): String? {
        val sql = """
            insert into team_invites (code, workspace_id, created_by_user_id, role, status, updated_at)
            values (?, ?::uuid, ?::uuid, 'team_member', 'active', now())
            on conflict (code) do nothing
            returning code
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, code)
            statement.setString(2, workspaceId)
            statement.setString(3, userId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("code") else null
            }
        }
    }

    private fun Connection.createInviteCode(
        workspace: WorkspaceRecord,
        userId: String,
        request: TeamInviteCreateRequest,
    ): TeamInviteRecord {
        val inviteeName = request.name.trim().take(120)
        val inviteeEmail = request.email?.trim()?.lowercase()?.take(160)?.ifBlank { null }
        val inviteePhoneNumber = request.phoneNumber
            ?.trim()
            ?.filter { it.isDigit() || it == '+' }
            ?.take(24)
            ?.ifBlank { null }
        val role = request.role.normalizedTeamRole()

        repeat(5) {
            val code = generateInviteCode(workspace.businessName)
            val insertedCode = insertInviteCode(
                workspaceId = workspace.id,
                userId = userId,
                code = code,
                role = role,
                inviteeName = inviteeName,
                inviteeEmail = inviteeEmail,
                inviteePhoneNumber = inviteePhoneNumber,
            )
            if (insertedCode != null) {
                return TeamInviteRecord(
                    code = insertedCode,
                    workspace = workspace.copy(inviteCode = insertedCode),
                    inviteeName = inviteeName,
                    inviteeEmail = inviteeEmail,
                    inviteePhoneNumber = inviteePhoneNumber,
                    role = role,
                )
            }
        }
        error("Unable to create a unique team invite code.")
    }

    private fun Connection.insertInviteCode(
        workspaceId: String,
        userId: String,
        code: String,
        role: String,
        inviteeName: String,
        inviteeEmail: String?,
        inviteePhoneNumber: String?,
    ): String? {
        val sql = """
            insert into team_invites (
                code,
                workspace_id,
                created_by_user_id,
                role,
                invitee_name,
                invitee_email,
                invitee_phone_number,
                status,
                updated_at
            )
            values (?, ?::uuid, ?::uuid, ?, ?, ?, ?, 'active', now())
            on conflict (code) do nothing
            returning code
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, code)
            statement.setString(2, workspaceId)
            statement.setString(3, userId)
            statement.setString(4, role.normalizedTeamRole())
            statement.setString(5, inviteeName)
            statement.setNullableString(6, inviteeEmail)
            statement.setNullableString(7, inviteePhoneNumber)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("code") else null
            }
        }
    }

    private fun Connection.findInviteWorkspace(code: String): WorkspaceRecord? {
        return findInviteRecord(code)?.workspace
    }

    private fun Connection.findPendingInviteForUser(user: AppUserRecord): TeamInviteRecord? {
        val email = user.email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val phoneNumber = user.phoneNumber?.normalizedPhoneForInvite()?.takeIf { it.isNotBlank() }
        if (email == null && phoneNumber == null) return null

        val sql = """
            select
                ti.code,
                ti.invitee_name,
                ti.invitee_email,
                ti.invitee_phone_number,
                ti.role,
                bw.id::text,
                bw.business_name,
                bw.legal_name,
                ti.role as workspace_role,
                bw.onboarding_completed_at is not null as onboarding_complete,
                bw.logo_file_name
            from team_invites ti
            join business_workspaces bw on bw.id = ti.workspace_id
            where ti.status = 'active'
              and (ti.expires_at is null or ti.expires_at > now())
              and (
                    (? is not null and lower(ti.invitee_email) = ?)
                 or (? is not null and regexp_replace(coalesce(ti.invitee_phone_number, ''), '[^0-9+]', '', 'g') = ?)
              )
            order by ti.created_at desc
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setNullableString(1, email)
            statement.setNullableString(2, email)
            statement.setNullableString(3, phoneNumber)
            statement.setNullableString(4, phoneNumber)
            statement.executeQuery().use { result ->
                if (result.next()) result.toTeamInviteRecord() else null
            }
        }
    }

    private fun Connection.findInviteRecord(code: String): TeamInviteRecord? {
        val sql = """
            select
                ti.code,
                ti.invitee_name,
                ti.invitee_email,
                ti.invitee_phone_number,
                ti.role,
                bw.id::text,
                bw.business_name,
                bw.legal_name,
                ti.role as workspace_role,
                bw.onboarding_completed_at is not null as onboarding_complete,
                bw.logo_file_name
            from team_invites ti
            join business_workspaces bw on bw.id = ti.workspace_id
            where ti.code = ?
              and ti.status = 'active'
              and (ti.expires_at is null or ti.expires_at > now())
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, code)
            statement.executeQuery().use { result ->
                if (result.next()) result.toTeamInviteRecord() else null
            }
        }
    }

    private fun Connection.markUserOwnerComplete(
        userId: String,
        displayName: String,
    ): AppUserRecord {
        val sql = """
            update app_users
            set
                role = 'business_owner',
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
            statement.setString(1, displayName)
            statement.setString(2, userId)
            statement.executeQuery().use { result ->
                result.next()
                result.toUserRecord()
            }
        }
    }

    private fun Connection.markInviteAccepted(code: String) {
        val sql = """
            update team_invites
            set status = 'accepted', updated_at = now()
            where code = ?
              and invitee_name is not null
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, code)
            statement.executeUpdate()
        }
    }

    private fun Connection.markUserTeamComplete(userId: String, displayName: String): AppUserRecord {
        val sql = """
            update app_users
            set
                role = 'team_member',
                display_name = coalesce(nullif(?, ''), display_name),
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
            statement.setString(1, displayName)
            statement.setString(2, userId)
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

    private fun PreparedStatement.bindBusinessSetup(
        userId: String,
        request: BusinessSetupRequest,
    ) {
        setString(1, userId)
        setString(2, request.businessName.trim())
        setString(3, request.legalName.trim())
        setString(4, request.industry.trim())
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
    }

    private fun PreparedStatement.bindBusinessSetupUpdate(
        workspaceId: String,
        request: BusinessSetupRequest,
    ) {
        setString(1, request.businessName.trim())
        setString(2, request.legalName.trim())
        setString(3, request.industry.trim())
        setNullableString(4, request.website.trim().ifBlank { null })
        setBoolean(5, request.isTaxRegistered)
        setNullableString(6, request.taxNumber.trim().ifBlank { null })
        setString(7, request.taxLabel.trim())
        setString(8, request.addressLine.trim())
        setString(9, request.city.trim())
        setNullableString(10, request.region.trim().ifBlank { null })
        setString(11, request.country.trim())
        setNullableString(12, request.postalCode.trim().ifBlank { null })
        setNullableString(13, request.logoFileName.trim().ifBlank { null })
        setString(14, request.invoicePrefix.trim().uppercase())
        setString(15, request.nextInvoiceNumber.trim())
        setString(16, request.paymentTerms.trim())
        setString(17, request.invoiceFooter.trim())
        setString(18, request.currency.trim().uppercase())
        setString(19, request.taxMode.trim())
        setBoolean(20, request.pricesIncludeTax)
        setString(21, workspaceId)
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
            inviteCode = runCatching { getString("invite_code") }.getOrNull()
                ?: runCatching { getString("code") }.getOrNull(),
        )

    private fun ResultSet.toTeamInviteRecord(): TeamInviteRecord {
        val workspace = WorkspaceRecord(
            id = getString("id"),
            businessName = getString("business_name"),
            legalName = getString("legal_name"),
            role = getString("workspace_role"),
            onboardingComplete = getBoolean("onboarding_complete"),
            logoFileName = getString("logo_file_name"),
            inviteCode = getString("code"),
        )
        return TeamInviteRecord(
            code = getString("code"),
            workspace = workspace,
            inviteeName = getString("invitee_name") ?: "",
            inviteeEmail = getString("invitee_email"),
            inviteePhoneNumber = getString("invitee_phone_number"),
            role = getString("role").normalizedTeamRole(),
        )
    }

    private fun ResultSet.toTeamMemberRecord(): TeamMemberRecord =
        TeamMemberRecord(
            id = getString("member_id"),
            userId = getString("user_id"),
            displayName = getString("display_name"),
            email = getString("email"),
            phoneNumber = getString("phone_number"),
            role = getString("role").normalizedTeamRole(),
            status = getString("status"),
            joinedAt = getString("joined_at"),
        )

    private fun ResultSet.toTeamInviteListRecord(): TeamInviteListRecord =
        TeamInviteListRecord(
            code = getString("code"),
            inviteeName = getString("invitee_name") ?: "",
            inviteeEmail = getString("invitee_email"),
            inviteePhoneNumber = getString("invitee_phone_number"),
            role = getString("role").normalizedTeamRole(),
            status = getString("status"),
            createdAt = getString("created_at"),
            expiresAt = getString("expires_at"),
        )

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
        pendingInvite: TeamInviteRecord?,
    ): OnboardingSessionRecord {
        val accessPath = if (pendingInvite != null || workspace?.role == RoleTeamMember || role == RoleTeamMember) {
            RoleTeamMember
        } else {
            RoleBusinessOwner
        }
        val onboardingStatus = when {
            pendingInvite != null -> "invited_team_member"
            workspace == null -> "new_account"
            workspace.role == RoleTeamMember -> "team_member_ready"
            workspace.onboardingComplete -> "complete"
            else -> "business_setup_required"
        }
        val requiredStep = when (onboardingStatus) {
            "new_account" -> "owner"
            "invited_team_member" -> "team"
            "business_setup_required" -> "business_setup"
            else -> "complete"
        }
        return OnboardingSessionRecord(
            user = this,
            workspace = workspace,
            pendingInvite = pendingInvite,
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

    private fun String.normalizedInviteCode(): String =
        trim().uppercase().filter(Char::isLetterOrDigit).take(16)

    private fun String.normalizedPhoneForInvite(): String =
        trim().filter { it.isDigit() || it == '+' }.take(24)

    private fun TeamInviteRecord.canBeClaimedBy(user: AppUserRecord): Boolean {
        val inviteEmail = inviteeEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val invitePhone = inviteePhoneNumber?.normalizedPhoneForInvite()?.takeIf { it.isNotBlank() }
        if (inviteEmail == null && invitePhone == null) return true
        val userEmail = user.email?.trim()?.lowercase()
        val userPhone = user.phoneNumber?.normalizedPhoneForInvite()
        return (inviteEmail != null && inviteEmail == userEmail) ||
            (invitePhone != null && invitePhone == userPhone)
    }

    private fun generateInviteCode(businessName: String): String {
        val prefix = businessName
            .filter(Char::isLetterOrDigit)
            .uppercase()
            .take(4)
            .padEnd(4, 'X')
        val suffix = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(4)
            .uppercase()
        return (prefix + suffix).take(12)
    }

    private fun String.normalizedTeamRole(): String {
        val normalized = trim()
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedTeamRoles) normalized else RoleTeamMember
    }

    private companion object {
        const val RoleBusinessOwner = "business_owner"
        const val RoleTeamMember = "team_member"
        val AllowedTeamRoles = setOf(
            RoleTeamMember,
            "manager",
            "cashier",
            "accountant",
            "inventory_manager",
            "sales_staff",
        )
    }
}
