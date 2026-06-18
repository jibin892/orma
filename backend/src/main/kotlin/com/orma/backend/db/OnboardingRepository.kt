package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.BusinessSetupRequest
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
    val inviteCode: String?,
)

data class OnboardingSessionRecord(
    val user: AppUserRecord,
    val workspace: WorkspaceRecord?,
    val onboardingStatus: String,
    val requiredStep: String,
    val accessPath: String,
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
                val workspace = connection.upsertOwnerWorkspace(user.id, request)
                connection.ensureOwnerMembership(workspace.id, user.id)
                val inviteCode = connection.ensurePilotInvite(workspace.id, user.id, request.businessName)
                val updatedUser = connection.markUserOwnerComplete(user.id, request.ownerName)
                connection.commit()
                updatedUser.toSession(workspace.copy(inviteCode = inviteCode))
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

    suspend fun joinInvite(
        firebaseUser: VerifiedFirebaseUser,
        code: String,
    ): OnboardingSessionRecord? = withContext(Dispatchers.IO) {
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
                val workspace = connection.findInviteWorkspace(code.normalizedInviteCode())
                if (workspace == null) {
                    connection.rollback()
                    return@withContext null
                }
                connection.ensureTeamMembership(workspace.id, user.id)
                val updatedUser = connection.markUserTeamComplete(user.id)
                connection.commit()
                updatedUser.toSession(workspace.copy(role = RoleTeamMember))
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
            updatedUser.toSession(workspace)
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

    private fun Connection.ensureTeamMembership(workspaceId: String, userId: String) {
        val sql = """
            insert into workspace_members (workspace_id, user_id, role, status, updated_at)
            values (?::uuid, ?::uuid, 'team_member', 'active', now())
            on conflict (workspace_id, user_id) do update set
                role = excluded.role,
                status = 'active',
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, userId)
            statement.executeUpdate()
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

    private fun Connection.findInviteWorkspace(code: String): WorkspaceRecord? {
        val sql = """
            select
                bw.id::text,
                bw.business_name,
                bw.legal_name,
                ti.role,
                bw.onboarding_completed_at is not null as onboarding_complete,
                ti.code as invite_code
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
                if (result.next()) result.toWorkspaceRecord() else null
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

    private fun Connection.markUserTeamComplete(userId: String): AppUserRecord {
        val sql = """
            update app_users
            set
                role = 'team_member',
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
            statement.setString(1, userId)
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
            role = getString("role"),
            onboardingComplete = getBoolean("onboarding_complete"),
            inviteCode = getString("invite_code"),
        )

    private fun AppUserRecord.toSession(workspace: WorkspaceRecord?): OnboardingSessionRecord {
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
            "team_member_ready" -> "team"
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

    private fun String.normalizedInviteCode(): String =
        trim().uppercase().filter(Char::isLetterOrDigit).take(16)

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

    private companion object {
        const val RoleBusinessOwner = "business_owner"
        const val RoleTeamMember = "team_member"
    }
}
