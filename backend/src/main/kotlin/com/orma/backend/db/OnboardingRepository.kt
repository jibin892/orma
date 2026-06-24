package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.BusinessSetupRequest
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
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
    val status: String,
    val joinedAt: String,
)

data class TeamOverviewRecord(
    val workspace: WorkspaceRecord,
    val canInviteMembers: Boolean,
    val members: List<TeamMemberRecord>,
)

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
                val workspace = connection.upsertOwnerWorkspace(user.id, request)
                connection.ensureOwnerMembership(workspace.id, user.id)
                val updatedUser = connection.markUserOwnerComplete(user.id, request.ownerName)
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
            TeamOverviewRecord(
                workspace = workspace,
                canInviteMembers = false,
                members = connection.listWorkspaceMembers(workspace.id),
            )
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
            val updatedUser = connection.updateNotificationPreference(user.id, enabled)
            val workspace = connection.findPrimaryWorkspace(updatedUser.id)
            if (!enabled) {
                connection.disableNotificationDeviceTokens(updatedUser.id)
            } else if (workspace != null) {
                connection.upsertNotificationDeviceToken(
                    userId = updatedUser.id,
                    workspaceId = workspace.id,
                    token = deviceToken,
                    platform = platform,
                    deviceName = deviceName,
                )
            }
            updatedUser.toSession(workspace)
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
                bw.cover_file_name
            from workspace_members wm
            join business_workspaces bw on bw.id = wm.workspace_id
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
                'business_owner' as role,
                onboarding_completed_at is not null as onboarding_complete,
                logo_file_name,
                cover_file_name,
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
        workspaceId: String,
        token: String?,
        platform: String?,
        deviceName: String?,
    ) {
        val cleanToken = token?.trim()?.takeIf { it.isNotBlank() } ?: return
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
            statement.setString(2, workspaceId)
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
        )

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

    private fun String.cleanActivityType(): String =
        trim()
            .lowercase()
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(60)
            .ifBlank { "activity" }

    private fun String.sellableActivityLabel(): String =
        when (cleanActivityType()) {
            "service" -> "Service"
            "appointment" -> "Appointment"
            else -> "Product"
        }

    private fun String.normalizedTeamRole(): String {
        val normalized = trim()
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedTeamRoles) normalized else RoleTeamMember
    }

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
        val AllowedTeamRoles = setOf(
            RoleTeamMember,
            "manager",
            "cashier",
            "accountant",
            "inventory_manager",
            "sales_staff",
        )
        val AllowedBusinessModes = setOf("product_selling", "service_selling", "appointment", "mixed")
    }
}
