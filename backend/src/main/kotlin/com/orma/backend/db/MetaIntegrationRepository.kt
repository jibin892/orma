package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.models.MetaCatalogSyncResponse
import com.orma.backend.models.MetaConnectCompleteResponse
import com.orma.backend.models.MetaConnectStartResponse
import com.orma.backend.models.MetaConnectionRequest
import com.orma.backend.models.MetaConnectionStatusResponse
import com.orma.backend.models.MetaOrderUpdateRequest
import com.orma.backend.models.MetaOrderUpdateResponse
import com.orma.backend.models.MetaProductReadinessResponse
import com.orma.backend.models.MetaSystemUserConnectResponse
import com.orma.backend.models.MetaWebhookEventResponse
import com.orma.backend.meta.MetaAccessToken
import com.orma.backend.meta.MetaGraphCatalogProductRequest
import com.orma.backend.meta.MetaGraphClient
import com.orma.backend.meta.MetaGraphException
import com.orma.backend.meta.MetaTokenCrypto
import java.math.BigDecimal
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetaIntegrationRepository(
    private val dataSource: DataSource,
    private val config: AppConfig,
    private val graphClient: MetaGraphClient = MetaGraphClient(config),
) {
    suspend fun status(firebaseUser: VerifiedFirebaseUser): MetaConnectionStatusResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.metaStatus(access.workspaceId)
        }
    }

    suspend fun upsertConnection(
        firebaseUser: VerifiedFirebaseUser,
        request: MetaConnectionRequest,
    ): MetaConnectionStatusResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.upsertMetaConnection(access.workspaceId, request)
            connection.metaStatus(access.workspaceId)
        }
    }

    suspend fun startOAuth(firebaseUser: VerifiedFirebaseUser): MetaConnectStartResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            if (!config.metaOAuthConfigured) {
                return@withContext MetaConnectStartResponse(
                    ready = false,
                    scopes = config.metaOAuthScopes,
                    message = "Meta OAuth is not configured on the backend yet.",
                )
            }
            if (!config.metaTokenStorageConfigured) {
                return@withContext MetaConnectStartResponse(
                    ready = false,
                    scopes = config.metaOAuthScopes,
                    message = "Meta token encryption is not configured on the backend yet.",
                )
            }
            val state = UUID.randomUUID().toString()
            val expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES)
            connection.insertMetaOAuthState(access, state, expiresAt)
            MetaConnectStartResponse(
                ready = true,
                authorizationUrl = graphClient.authorizationUrl(state),
                state = state,
                expiresAt = expiresAt.toString(),
                scopes = config.metaOAuthScopes,
                message = "Open this URL to connect the business Meta account.",
            )
        }
    }

    suspend fun completeOAuth(state: String, code: String): MetaConnectCompleteResponse = withContext(Dispatchers.IO) {
        if (!config.metaOAuthConfigured) {
            return@withContext MetaConnectCompleteResponse(
                success = false,
                status = "not_configured",
                message = "Meta OAuth is not configured on the backend yet.",
            )
        }
        if (!config.metaTokenStorageConfigured) {
            return@withContext MetaConnectCompleteResponse(
                success = false,
                status = "token_storage_not_configured",
                message = "Meta token encryption is not configured on the backend yet.",
            )
        }
        val oauthState = dataSource.connection.use { connection ->
            connection.consumeMetaOAuthState(state)
        } ?: return@withContext MetaConnectCompleteResponse(
            success = false,
            status = "invalid_state",
            message = "This Meta connection link has expired. Start connection again from ORMA.",
        )

        val token = try {
            graphClient.exchangeCode(code)
        } catch (error: MetaGraphException) {
            dataSource.connection.use { connection ->
                connection.updateMetaLastError(oauthState.workspaceId, error.publicMetaMessage())
            }
            return@withContext MetaConnectCompleteResponse(
                success = false,
                status = "meta_auth_failed",
                message = "Meta could not complete the account connection. Try again.",
            )
        }

        dataSource.connection.use { connection ->
            connection.storeMetaAccessToken(
                workspaceId = oauthState.workspaceId,
                token = token,
                credentialSource = "oauth",
            )
        }
        MetaConnectCompleteResponse(
            success = true,
            status = "connected",
            message = "Meta account connected. Return to ORMA to finish WhatsApp setup.",
        )
    }

    suspend fun connectSystemUser(firebaseUser: VerifiedFirebaseUser): MetaSystemUserConnectResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            val connectionState = connection.findMetaConnection(access.workspaceId)
            if (!config.metaSystemUserTokenConfigured) {
                return@withContext MetaSystemUserConnectResponse(
                    connected = false,
                    status = "not_configured",
                    message = "Meta system-user token is not configured on the backend yet.",
                    connection = connection.metaStatus(access.workspaceId),
                )
            }
            if (connectionState == null || !connectionState.hasWorkspaceMetaIdentifiers) {
                return@withContext MetaSystemUserConnectResponse(
                    connected = false,
                    status = "setup_required",
                    message = "Save WhatsApp Business and phone number details before enabling backend credentials.",
                    connection = connection.metaStatus(access.workspaceId),
                )
            }
            connection.activateMetaSystemUserToken(access.workspaceId)
            MetaSystemUserConnectResponse(
                connected = true,
                status = "connected",
                message = "Backend Meta credentials are active for this workspace.",
                connection = connection.metaStatus(access.workspaceId),
            )
        }
    }

    suspend fun sendOrderUpdate(
        firebaseUser: VerifiedFirebaseUser,
        request: MetaOrderUpdateRequest,
    ): MetaOrderUpdateResponse? = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.orderWhatsAppContext(access.workspaceId, request.orderId)
        } ?: return@withContext MetaOrderUpdateResponse(
            sent = false,
            status = "order_not_found",
            message = "Could not find this order in the current workspace.",
        )

        val accessToken = context.resolveAccessToken()
            ?: return@withContext MetaOrderUpdateResponse(
                sent = false,
                status = "not_configured",
                message = "WhatsApp messaging is not connected for this workspace yet.",
            )
        val phoneNumberId = context.phoneNumberId?.takeIf { it.isNotBlank() }
            ?: return@withContext MetaOrderUpdateResponse(
                sent = false,
                status = "phone_number_missing",
                message = "Add the WhatsApp phone number ID before sending order updates.",
            )
        val recipient = request.recipientPhoneNumber?.takeIf { it.isNotBlank() } ?: context.customerPhoneNumber
        if (recipient.isNullOrBlank()) {
            return@withContext MetaOrderUpdateResponse(
                sent = false,
                status = "recipient_missing",
                message = "Add a customer phone number before sending WhatsApp updates.",
            )
        }

        val templateName = request.templateName?.trim()?.takeIf { it.isNotBlank() }
            ?: config.metaDefaultOrderTemplate
        val languageCode = request.languageCode?.trim()?.takeIf { it.isNotBlank() }
            ?: config.metaDefaultLanguageCode
        return@withContext try {
            val result = graphClient.sendWhatsAppTemplate(
                accessToken = accessToken,
                phoneNumberId = phoneNumberId,
                recipientPhoneNumber = recipient,
                templateName = templateName,
                languageCode = languageCode,
                parameters = listOf(
                    context.orderNumber,
                    context.customerName ?: "Customer",
                    "${context.currency} ${context.total}",
                    context.status,
                ),
            )
            dataSource.connection.use { connection ->
                connection.updateMetaMessagingStatus(context.workspaceId, "ready", null)
            }
            MetaOrderUpdateResponse(
                sent = true,
                status = "sent",
                message = "WhatsApp order update sent.",
                messageId = result.messageId,
            )
        } catch (error: MetaGraphException) {
            val publicMessage = error.publicMetaMessage()
            dataSource.connection.use { connection ->
                connection.updateMetaMessagingStatus(context.workspaceId, "send_failed", publicMessage)
            }
            MetaOrderUpdateResponse(
                sent = false,
                status = "send_failed",
                message = "WhatsApp could not send this update. Check the template and Meta account setup.",
            )
        }
    }

    suspend fun syncCatalog(firebaseUser: VerifiedFirebaseUser): MetaCatalogSyncResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val connectionState = connection.findMetaConnection(access.workspaceId)
                val connected = connectionState?.isConnected == true
                val pendingCredentials = connectionState != null && !connected
                val accessToken = connectionState?.resolveAccessToken()
                val catalogId = connectionState?.catalogId?.takeIf { it.isNotBlank() }
                val canSyncToMeta = connected && accessToken != null && catalogId != null
                val productReadiness = connection.listMetaCatalogProducts(access.workspaceId).map { product ->
                    val issues = product.readinessIssues(connected)
                    val status = when {
                        issues.isNotEmpty() -> "blocked"
                        canSyncToMeta -> "syncing"
                        connected -> "ready"
                        else -> "not_connected"
                    }
                    if (canSyncToMeta && issues.isEmpty()) {
                        try {
                            val result = graphClient.upsertCatalogProduct(
                                accessToken = accessToken,
                                catalogId = catalogId,
                                product = product.toMetaGraphRequest(access.workspaceId),
                            )
                            connection.upsertMetaProductReadiness(
                                workspaceId = access.workspaceId,
                                product = product,
                                status = "synced",
                                issues = emptyList(),
                                metaProductId = result.metaProductId,
                            )
                        } catch (error: MetaGraphException) {
                            connection.upsertMetaProductReadiness(
                                workspaceId = access.workspaceId,
                                product = product,
                                status = "sync_failed",
                                issues = listOf("Meta could not sync this item right now."),
                                lastError = error.publicMetaMessage(),
                            )
                        }
                    } else {
                        connection.upsertMetaProductReadiness(
                            workspaceId = access.workspaceId,
                            product = product,
                            status = status,
                            issues = issues,
                        )
                    }
                }
                val failedSyncCount = productReadiness.count { it.status == "sync_failed" }
                connection.touchMetaSync(
                    workspaceId = access.workspaceId,
                    connected = connected,
                    lastError = when {
                        failedSyncCount > 0 -> "$failedSyncCount products could not sync to Meta."
                        canSyncToMeta -> null
                        connected && catalogId == null -> "Add a Meta catalog ID before syncing products."
                        connected && accessToken == null -> "Backend Meta token is not available."
                        pendingCredentials -> "WhatsApp credentials are pending for this workspace."
                        else -> "Meta account is not connected yet."
                    },
                )
                connection.commit()
                MetaCatalogSyncResponse(
                    connected = connected,
                    productsReady = productReadiness.count { it.ready },
                    productsBlocked = productReadiness.count { !it.ready },
                    productsSynced = productReadiness.count { it.status == "synced" },
                    productReadiness = productReadiness,
                    message = if (connected) {
                        if (failedSyncCount > 0) {
                            "Catalog sync finished with products that need review."
                        } else if (canSyncToMeta) {
                            "Catalog sync completed for ready products."
                        } else {
                            "Catalog readiness checked. Complete catalog credentials before syncing products."
                        }
                    } else if (pendingCredentials) {
                        "WhatsApp setup is saved. Connect backend credentials before syncing products."
                    } else {
                        "Connect Meta before syncing products."
                    },
                )
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun recordWebhook(payload: String): MetaWebhookEventResponse = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            val phoneNumberId = payload.extractJsonString("phone_number_id")
            val workspaceId = phoneNumberId?.let { connection.findMetaWorkspaceByPhoneNumberId(it) }
            try {
                val response = connection.prepareStatement(
                    """
                    insert into meta_webhook_events (workspace_id, object_type, event_type, payload)
                    values (
                        ?::uuid,
                        nullif(?,''),
                        nullif(?,''),
                        ?::jsonb
                    )
                    returning id::text, status
                    """.trimIndent(),
                ).use { statement ->
                    statement.setStringOrNull(1, workspaceId)
                    statement.setString(2, payload.extractJsonString("object").orEmpty())
                    statement.setString(3, payload.extractWebhookEventType().orEmpty())
                    statement.setString(4, payload.ifBlank { "{}" })
                    statement.executeQuery().use { result ->
                        result.next()
                        MetaWebhookEventResponse(
                            id = result.getString("id"),
                            status = result.getString("status"),
                        )
                    }
                }
                if (workspaceId != null) {
                    connection.upsertMetaLeadFromWebhook(
                        workspaceId = workspaceId,
                        phoneNumberId = phoneNumberId,
                        waId = payload.extractJsonString("wa_id") ?: payload.extractJsonString("from"),
                        customerName = payload.extractContactProfileName(),
                        phoneNumber = payload.extractJsonString("from"),
                        preview = payload.extractMessagePreview(),
                    )
                }
                connection.commit()
                response
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun Connection.insertMetaOAuthState(
        access: DashboardWorkspaceAccess,
        state: String,
        expiresAt: Instant,
    ) {
        prepareStatement(
            """
            insert into meta_oauth_states (state, workspace_id, user_id, expires_at)
            values (?, ?::uuid, ?::uuid, ?::timestamptz)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, state)
            statement.setString(2, access.workspaceId)
            statement.setString(3, access.userId)
            statement.setString(4, expiresAt.toString())
            statement.executeUpdate()
        }
    }

    private fun Connection.consumeMetaOAuthState(state: String): MetaOAuthStateRow? {
        val cleanState = state.trim().takeIf { it.isNotBlank() } ?: return null
        return prepareStatement(
            """
            update meta_oauth_states
            set used_at = now()
            where state = ?
              and used_at is null
              and expires_at > now()
            returning workspace_id::text, user_id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, cleanState)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    MetaOAuthStateRow(
                        workspaceId = result.getString("workspace_id"),
                        userId = result.getString("user_id"),
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun Connection.storeMetaAccessToken(
        workspaceId: String,
        token: MetaAccessToken,
        credentialSource: String,
    ) {
        val secret = config.metaTokenEncryptionSecret
            ?: throw IllegalStateException("META_TOKEN_ENCRYPTION_SECRET is required before storing Meta tokens.")
        val encryptedToken = MetaTokenCrypto(secret).encrypt(token.accessToken)
        val expiresAt = token.expiresInSeconds
            ?.takeIf { it > 0 }
            ?.let { Instant.now().plus(it, ChronoUnit.SECONDS).toString() }
        val sql = """
            insert into meta_connections (
                workspace_id, status, connection_mode, access_token_status,
                credential_source, access_token_ciphertext, access_token_last4,
                token_expires_at, connected_at, messaging_status, updated_at
            )
            values (
                ?::uuid,
                'setup_pending',
                'oauth',
                'configured',
                ?,
                ?,
                ?,
                ?::timestamptz,
                now(),
                'setup_required',
                now()
            )
            on conflict (workspace_id) do update
            set status = case
                    when meta_connections.whatsapp_business_account_id is not null
                      or meta_connections.catalog_id is not null
                      or meta_connections.page_id is not null
                      or meta_connections.instagram_business_account_id is not null
                    then 'connected'
                    else 'setup_pending'
                end,
                connection_mode = 'oauth',
                access_token_status = 'configured',
                credential_source = excluded.credential_source,
                access_token_ciphertext = excluded.access_token_ciphertext,
                access_token_last4 = excluded.access_token_last4,
                token_expires_at = excluded.token_expires_at,
                connected_at = now(),
                messaging_status = case
                    when meta_connections.phone_number_id is not null then 'ready'
                    else 'setup_required'
                end,
                last_error = null,
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, credentialSource)
            statement.setString(3, encryptedToken)
            statement.setStringOrNull(4, token.accessToken.takeLast(4))
            statement.setStringOrNull(5, expiresAt)
            statement.executeUpdate()
        }
    }

    private fun Connection.activateMetaSystemUserToken(workspaceId: String) {
        val tokenLast4 = config.metaSystemUserAccessToken?.takeLast(4)
        val sql = """
            update meta_connections
            set status = 'connected',
                access_token_status = 'configured',
                credential_source = 'system_user_env',
                access_token_ciphertext = null,
                access_token_last4 = ?,
                connected_at = coalesce(connected_at, now()),
                messaging_status = case when phone_number_id is not null then 'ready' else 'setup_required' end,
                last_error = null,
                updated_at = now()
            where workspace_id = ?::uuid
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setStringOrNull(1, tokenLast4)
            statement.setString(2, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.updateMetaLastError(workspaceId: String, message: String?) {
        prepareStatement(
            """
            update meta_connections
            set last_error = ?, updated_at = now()
            where workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setStringOrNull(1, message)
            statement.setString(2, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.updateMetaMessagingStatus(
        workspaceId: String,
        status: String,
        error: String?,
    ) {
        prepareStatement(
            """
            update meta_connections
            set messaging_status = ?,
                last_error = ?,
                updated_at = now()
            where workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, status)
            statement.setStringOrNull(2, error)
            statement.setString(3, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.orderWhatsAppContext(
        workspaceId: String,
        orderId: String,
    ): MetaOrderWhatsAppContext? {
        val cleanOrderId = orderId.trim().takeIf { it.isNotBlank() } ?: return null
        val sql = """
            select
                o.id::text as order_id,
                o.workspace_id::text as workspace_id,
                o.order_number,
                o.status,
                o.total,
                o.currency,
                c.name as customer_name,
                c.phone_number as customer_phone_number,
                mc.phone_number_id,
                mc.credential_source,
                mc.access_token_ciphertext
            from orders o
            left join customers c on c.id = o.customer_id
            left join meta_connections mc on mc.workspace_id = o.workspace_id
            where o.id = ?::uuid
              and o.workspace_id = ?::uuid
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanOrderId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    MetaOrderWhatsAppContext(
                        workspaceId = result.getString("workspace_id"),
                        orderId = result.getString("order_id"),
                        orderNumber = result.getString("order_number"),
                        status = result.getString("status"),
                        total = (result.getBigDecimal("total") ?: BigDecimal.ZERO).toPlainString(),
                        currency = result.getString("currency") ?: "INR",
                        customerName = result.getString("customer_name"),
                        customerPhoneNumber = result.getString("customer_phone_number"),
                        phoneNumberId = result.getString("phone_number_id"),
                        credentialSource = result.getString("credential_source") ?: "none",
                        accessTokenCiphertext = result.getString("access_token_ciphertext"),
                    )
                }
            }
        }
    }

    private fun Connection.upsertMetaLeadFromWebhook(
        workspaceId: String,
        phoneNumberId: String?,
        waId: String?,
        customerName: String?,
        phoneNumber: String?,
        preview: String?,
    ) {
        val cleanWaId = waId?.trim()?.takeIf { it.isNotBlank() } ?: return
        val threadId = prepareStatement(
            """
            insert into meta_message_threads (
                workspace_id, phone_number_id, wa_id, customer_name, phone_number,
                last_message_preview, last_message_at, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, now(), now())
            on conflict (workspace_id, wa_id) where wa_id is not null do update
            set phone_number_id = coalesce(excluded.phone_number_id, meta_message_threads.phone_number_id),
                customer_name = coalesce(excluded.customer_name, meta_message_threads.customer_name),
                phone_number = coalesce(excluded.phone_number, meta_message_threads.phone_number),
                last_message_preview = coalesce(excluded.last_message_preview, meta_message_threads.last_message_preview),
                last_message_at = now(),
                updated_at = now()
            returning id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setStringOrNull(2, phoneNumberId)
            statement.setString(3, cleanWaId)
            statement.setStringOrNull(4, customerName)
            statement.setStringOrNull(5, phoneNumber)
            statement.setStringOrNull(6, preview)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
        prepareStatement(
            """
            insert into meta_leads (
                workspace_id, thread_id, customer_name, phone_number, last_message_preview, updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, now())
            on conflict (thread_id) where thread_id is not null do update
            set customer_name = coalesce(excluded.customer_name, meta_leads.customer_name),
                phone_number = coalesce(excluded.phone_number, meta_leads.phone_number),
                last_message_preview = coalesce(excluded.last_message_preview, meta_leads.last_message_preview),
                updated_at = now()
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, threadId)
            statement.setStringOrNull(3, customerName)
            statement.setStringOrNull(4, phoneNumber)
            statement.setStringOrNull(5, preview)
            statement.executeUpdate()
        }
    }

    private fun Connection.findMetaWorkspaceByPhoneNumberId(phoneNumberId: String): String? {
        val cleanPhoneNumberId = phoneNumberId.trim().takeIf { it.isNotBlank() } ?: return null
        return prepareStatement(
            """
            select workspace_id::text
            from meta_connections
            where phone_number_id = ?
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, cleanPhoneNumberId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("workspace_id") else null
            }
        }
    }

    private fun Connection.resolveMetaWorkspaceAccess(firebaseUser: VerifiedFirebaseUser): DashboardWorkspaceAccess? {
        val sql = """
            select
                au.id::text as user_id,
                bw.id::text as workspace_id,
                wm.role,
                bw.currency
            from app_users au
            join workspace_members wm on wm.user_id = au.id and wm.status = 'active'
            join business_workspaces bw on bw.id = wm.workspace_id
            where au.firebase_uid = ?
            order by case when wm.role = 'business_owner' then 0 else 1 end, wm.created_at
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, firebaseUser.uid)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    DashboardWorkspaceAccess(
                        userId = result.getString("user_id"),
                        workspaceId = result.getString("workspace_id"),
                        role = result.getString("role"),
                        currency = result.getString("currency") ?: "INR",
                    )
                }
            }
        }
    }

    private fun Connection.metaStatus(workspaceId: String): MetaConnectionStatusResponse {
        val connection = findMetaConnection(workspaceId)
        val readiness = listMetaProductReadiness(workspaceId)
        return MetaConnectionStatusResponse(
            connected = connection?.isConnected == true,
            status = connection?.status ?: "not_connected",
            connectionMode = connection?.connectionMode ?: "manual_setup",
            businessDisplayName = connection?.businessDisplayName,
            businessId = connection?.businessId,
            whatsappDisplayNumber = connection?.whatsappDisplayNumber,
            whatsappBusinessAccountId = connection?.whatsappBusinessAccountId,
            phoneNumberId = connection?.phoneNumberId,
            catalogId = connection?.catalogId,
            pageId = connection?.pageId,
            instagramBusinessAccountId = connection?.instagramBusinessAccountId,
            scopes = connection?.scopes.orEmpty(),
            accessTokenStatus = connection?.accessTokenStatus ?: "not_configured",
            tokenExpiresAt = connection?.tokenExpiresAt,
            webhookSubscribedAt = connection?.webhookSubscribedAt,
            messagingStatus = connection?.messagingStatus ?: "not_configured",
            lastSyncAt = connection?.lastSyncAt,
            lastError = connection?.lastError,
            productsReady = readiness.count { it.ready },
            productsBlocked = readiness.count { !it.ready },
            productsSynced = readiness.count { it.status == "synced" },
            productReadiness = readiness,
        )
    }

    private fun Connection.findMetaConnection(workspaceId: String): MetaConnectionRow? {
        val sql = """
            select
                status,
                connection_mode,
                business_display_name,
                business_id,
                whatsapp_display_number,
                whatsapp_business_account_id,
                phone_number_id,
                catalog_id,
                page_id,
                instagram_business_account_id,
                scopes,
                access_token_status,
                credential_source,
                access_token_ciphertext,
                access_token_last4,
                token_expires_at::text as token_expires_at,
                webhook_subscribed_at::text as webhook_subscribed_at,
                messaging_status,
                last_sync_at::text as last_sync_at,
                last_error
            from meta_connections
            where workspace_id = ?::uuid
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toMetaConnectionRow() else null
            }
        }
    }

    private fun Connection.upsertMetaConnection(workspaceId: String, request: MetaConnectionRequest) {
        val sql = """
            insert into meta_connections (
                workspace_id,
                status,
                connection_mode,
                business_display_name,
                business_id,
                whatsapp_display_number,
                whatsapp_business_account_id,
                phone_number_id,
                catalog_id,
                page_id,
                instagram_business_account_id,
                scopes,
                updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            on conflict (workspace_id) do update
            set status = excluded.status,
                connection_mode = excluded.connection_mode,
                business_display_name = excluded.business_display_name,
                business_id = excluded.business_id,
                whatsapp_display_number = excluded.whatsapp_display_number,
                whatsapp_business_account_id = excluded.whatsapp_business_account_id,
                phone_number_id = excluded.phone_number_id,
                catalog_id = excluded.catalog_id,
                page_id = excluded.page_id,
                instagram_business_account_id = excluded.instagram_business_account_id,
                scopes = excluded.scopes,
                last_error = null,
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, request.status.cleanMetaStatus())
            statement.setString(3, request.connectionMode.cleanMetaConnectionMode())
            statement.setStringOrNull(4, request.businessDisplayName)
            statement.setStringOrNull(5, request.businessId)
            statement.setStringOrNull(6, request.whatsappDisplayNumber)
            statement.setStringOrNull(7, request.whatsappBusinessAccountId)
            statement.setStringOrNull(8, request.phoneNumberId)
            statement.setStringOrNull(9, request.catalogId)
            statement.setStringOrNull(10, request.pageId)
            statement.setStringOrNull(11, request.instagramBusinessAccountId)
            statement.setArray(12, createArrayOf("text", request.scopes.map { it.trim() }.filter { it.isNotBlank() }.toTypedArray()))
            statement.executeUpdate()
        }
    }

    private fun Connection.listMetaCatalogProducts(workspaceId: String): List<MetaCatalogProductRow> {
        val sql = """
            select
                p.id::text as product_id,
                p.name,
                p.sku,
                p.barcode,
                p.description,
                p.unit,
                p.status,
                p.selling_price,
                p.currency,
                p.track_stock,
                p.stock_quantity,
                p.reorder_level,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = p.workspace_id
                      and pi.product_id = p.id::text
                      and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path
            from products p
            where p.workspace_id = ?::uuid
              and p.status = 'active'
            order by p.created_at desc
            limit 300
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(result.toMetaCatalogProductRow())
                    }
                }
            }
        }
    }

    private fun Connection.listMetaProductReadiness(workspaceId: String): List<MetaProductReadinessResponse> {
        val sql = """
            select
                p.id::text as product_id,
                p.name as product_name,
                coalesce(mps.status, 'not_synced') as sync_status,
                coalesce(mps.readiness_issues, '{}') as readiness_issues,
                mps.meta_product_id,
                mps.last_sync_at::text as last_sync_at
            from products p
            left join meta_product_sync mps
              on mps.workspace_id = p.workspace_id
             and mps.product_id = p.id
            where p.workspace_id = ?::uuid
              and p.status = 'active'
            order by p.created_at desc
            limit 300
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(result.toMetaProductReadinessResponse())
                    }
                }
            }
        }
    }

    private fun Connection.upsertMetaProductReadiness(
        workspaceId: String,
        product: MetaCatalogProductRow,
        status: String,
        issues: List<String>,
        metaProductId: String? = null,
        lastError: String? = null,
    ): MetaProductReadinessResponse {
        val sql = """
            insert into meta_product_sync (
                workspace_id,
                product_id,
                status,
                readiness_issues,
                meta_product_id,
                last_error,
                last_sync_at,
                updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, now(), now())
            on conflict (workspace_id, product_id) do update
            set status = excluded.status,
                readiness_issues = excluded.readiness_issues,
                meta_product_id = coalesce(excluded.meta_product_id, meta_product_sync.meta_product_id),
                last_sync_at = now(),
                last_error = excluded.last_error,
                updated_at = now()
            returning
                product_id::text,
                ? as product_name,
                status as sync_status,
                readiness_issues,
                meta_product_id,
                last_sync_at::text as last_sync_at
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, product.productId)
            statement.setString(3, status)
            statement.setArray(4, createArrayOf("text", issues.toTypedArray()))
            statement.setStringOrNull(5, metaProductId)
            statement.setStringOrNull(6, lastError)
            statement.setString(7, product.name)
            statement.executeQuery().use { result ->
                result.next()
                result.toMetaProductReadinessResponse()
            }
        }
    }

    private fun Connection.touchMetaSync(
        workspaceId: String,
        connected: Boolean,
        lastError: String?,
    ) {
        val sql = """
            insert into meta_connections (workspace_id, status, last_sync_at, last_error, updated_at)
            values (?::uuid, ?, now(), ?, now())
            on conflict (workspace_id) do update
            set last_sync_at = now(),
                last_error = excluded.last_error,
                updated_at = now()
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, if (connected) "connected" else "not_connected")
            statement.setString(3, lastError)
            statement.executeUpdate()
        }
    }

    private fun ResultSet.toMetaConnectionRow(): MetaConnectionRow =
        MetaConnectionRow(
            status = getString("status") ?: "not_connected",
            connectionMode = getString("connection_mode") ?: "manual_setup",
            businessDisplayName = getString("business_display_name"),
            businessId = getString("business_id"),
            whatsappDisplayNumber = getString("whatsapp_display_number"),
            whatsappBusinessAccountId = getString("whatsapp_business_account_id"),
            phoneNumberId = getString("phone_number_id"),
            catalogId = getString("catalog_id"),
            pageId = getString("page_id"),
            instagramBusinessAccountId = getString("instagram_business_account_id"),
            scopes = (getArray("scopes")?.array as? Array<*>)?.mapNotNull { it as? String }.orEmpty(),
            accessTokenStatus = getString("access_token_status") ?: "not_configured",
            credentialSource = getString("credential_source") ?: "none",
            accessTokenCiphertext = getString("access_token_ciphertext"),
            accessTokenLast4 = getString("access_token_last4"),
            tokenExpiresAt = getString("token_expires_at"),
            webhookSubscribedAt = getString("webhook_subscribed_at"),
            messagingStatus = getString("messaging_status") ?: "not_configured",
            lastSyncAt = getString("last_sync_at"),
            lastError = getString("last_error"),
        )

    private fun ResultSet.toMetaCatalogProductRow(): MetaCatalogProductRow =
        MetaCatalogProductRow(
            productId = getString("product_id"),
            name = getString("name"),
            sku = getString("sku"),
            barcode = getString("barcode"),
            description = getString("description"),
            unit = getString("unit"),
            status = getString("status"),
            sellingPrice = getBigDecimal("selling_price") ?: BigDecimal.ZERO,
            currency = getString("currency") ?: "INR",
            trackStock = getBoolean("track_stock"),
            stockQuantity = getBigDecimal("stock_quantity") ?: BigDecimal.ZERO,
            reorderLevel = getBigDecimal("reorder_level") ?: BigDecimal.ZERO,
            imageStoragePath = getString("image_storage_path"),
        )

    private fun ResultSet.toMetaProductReadinessResponse(): MetaProductReadinessResponse {
        val issues = (getArray("readiness_issues")?.array as? Array<*>)?.mapNotNull { it as? String }.orEmpty()
        val status = getString("sync_status") ?: "not_synced"
        return MetaProductReadinessResponse(
            productId = getString("product_id"),
            productName = getString("product_name"),
            ready = issues.isEmpty() && status in setOf("ready", "synced", "syncing"),
            status = status,
            issues = issues,
            metaProductId = getString("meta_product_id"),
            lastSyncAt = getString("last_sync_at"),
        )
    }

    private fun MetaConnectionRow.resolveAccessToken(): String? =
        when (credentialSource) {
            "system_user_env" -> config.metaSystemUserAccessToken?.trim()?.takeIf { it.isNotBlank() }
            "oauth", "embedded_signup" -> decryptStoredToken(accessTokenCiphertext)
            else -> decryptStoredToken(accessTokenCiphertext)
        }

    private fun MetaOrderWhatsAppContext.resolveAccessToken(): String? =
        when (credentialSource) {
            "system_user_env" -> config.metaSystemUserAccessToken?.trim()?.takeIf { it.isNotBlank() }
            "oauth", "embedded_signup" -> decryptStoredToken(accessTokenCiphertext)
            else -> decryptStoredToken(accessTokenCiphertext)
        }

    private fun decryptStoredToken(ciphertext: String?): String? {
        val cleanCiphertext = ciphertext?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val secret = config.metaTokenEncryptionSecret?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { MetaTokenCrypto(secret).decrypt(cleanCiphertext) }.getOrNull()
    }

    private fun MetaCatalogProductRow.toMetaGraphRequest(workspaceId: String): MetaGraphCatalogProductRequest =
        MetaGraphCatalogProductRequest(
            retailerId = sku?.takeIf { it.isNotBlank() }
                ?: barcode?.takeIf { it.isNotBlank() }
                ?: productId,
            name = name,
            description = description?.takeIf { it.isNotBlank() } ?: "$name from ORMA catalog",
            price = sellingPrice,
            currency = currency,
            imageUrl = imageStoragePath.toMetaMediaUrl().orEmpty(),
            productUrl = "${config.publicCatalogBaseUrl()}?catalog=$workspaceId&product=$productId",
            availability = if (trackStock && stockQuantity <= BigDecimal.ZERO) "out of stock" else "in stock",
        )

    private fun String?.toMetaMediaUrl(): String? {
        val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (value.startsWith("https://") || value.startsWith("http://")) return value
        if (config.activeMediaStorageProvider != "cloudinary") return null
        val cloudName = config.cloudinaryCloudName?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return "https://res.cloudinary.com/$cloudName/image/upload/${value.cloudinaryPathEncoded()}"
    }
}

private data class MetaConnectionRow(
    val status: String,
    val connectionMode: String,
    val businessDisplayName: String?,
    val businessId: String?,
    val whatsappDisplayNumber: String?,
    val whatsappBusinessAccountId: String?,
    val phoneNumberId: String?,
    val catalogId: String?,
    val pageId: String?,
    val instagramBusinessAccountId: String?,
    val scopes: List<String>,
    val accessTokenStatus: String,
    val credentialSource: String,
    val accessTokenCiphertext: String?,
    val accessTokenLast4: String?,
    val tokenExpiresAt: String?,
    val webhookSubscribedAt: String?,
    val messagingStatus: String,
    val lastSyncAt: String?,
    val lastError: String?,
) {
    val hasWorkspaceMetaIdentifiers: Boolean
        get() = !whatsappBusinessAccountId.isNullOrBlank() ||
            !phoneNumberId.isNullOrBlank() ||
            !catalogId.isNullOrBlank() ||
            !pageId.isNullOrBlank() ||
            !instagramBusinessAccountId.isNullOrBlank()

    val isConnected: Boolean
        get() = status == "connected" &&
            accessTokenStatus == "configured" &&
            hasWorkspaceMetaIdentifiers
}

private data class MetaCatalogProductRow(
    val productId: String,
    val name: String,
    val sku: String?,
    val barcode: String?,
    val description: String?,
    val unit: String?,
    val status: String,
    val sellingPrice: BigDecimal,
    val currency: String,
    val trackStock: Boolean,
    val stockQuantity: BigDecimal,
    val reorderLevel: BigDecimal,
    val imageStoragePath: String?,
)

private data class MetaOAuthStateRow(
    val workspaceId: String,
    val userId: String,
)

private data class MetaOrderWhatsAppContext(
    val workspaceId: String,
    val orderId: String,
    val orderNumber: String,
    val status: String,
    val total: String,
    val currency: String,
    val customerName: String?,
    val customerPhoneNumber: String?,
    val phoneNumberId: String?,
    val credentialSource: String,
    val accessTokenCiphertext: String?,
)

private fun MetaCatalogProductRow.readinessIssues(connected: Boolean): List<String> = buildList {
    if (!connected) add("Connect Meta before syncing this item.")
    if (status != "active") add("Set product status active.")
    if (sellingPrice <= BigDecimal.ZERO) add("Add a selling price.")
    if (imageStoragePath.isNullOrBlank()) add("Add a product image.")
    if (trackStock && stockQuantity <= BigDecimal.ZERO) add("Restock before promoting this item.")
    if (trackStock && stockQuantity <= reorderLevel) add("Stock is at or below the reorder level.")
}

private fun AppConfig.publicCatalogBaseUrl(): String =
    allowedOrigins.firstOrNull { it.startsWith("https://") && "vercel.app" in it }
        ?: allowedOrigins.firstOrNull { it.startsWith("https://") }
        ?: "https://orma-web-dist-dev-api.vercel.app"

private fun String.cloudinaryPathEncoded(): String =
    split("/")
        .filter { it.isNotBlank() }
        .joinToString("/") { segment ->
            java.net.URLEncoder.encode(segment, Charsets.UTF_8)
                .replace("+", "%20")
        }

private fun Throwable.publicMetaMessage(): String =
    message
        ?.take(240)
        ?.takeIf { it.isNotBlank() }
        ?: "Meta request failed."

private fun java.sql.PreparedStatement.setStringOrNull(index: Int, value: String?) {
    if (value.isNullOrBlank()) {
        setNull(index, Types.VARCHAR)
    } else {
        setString(index, value.trim())
    }
}

private fun String.cleanMetaStatus(): String =
    trim()
        .lowercase()
        .takeIf { it in setOf("connected", "not_connected", "setup_pending", "credentials_pending", "needs_review", "disabled") }
        ?: "credentials_pending"

private fun String.cleanMetaConnectionMode(): String =
    trim()
        .lowercase()
        .replace("-", "_")
        .takeIf { it in setOf("manual_setup", "embedded_signup", "oauth") }
        ?: "manual_setup"

private fun String.extractJsonString(key: String): String? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    return pattern.find(this)?.groupValues?.get(1)
}

private fun String.extractWebhookEventType(): String? {
    val field = Regex("\"field\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(this)?.groupValues?.get(1)
    val type = Regex("\"type\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(this)?.groupValues?.get(1)
    return field ?: type
}

private fun String.extractContactProfileName(): String? {
    val contactBlock = Regex("\"contacts\"\\s*:\\s*\\[(.*?)\\]", RegexOption.DOT_MATCHES_ALL)
        .find(this)
        ?.groupValues
        ?.get(1)
        ?: return null
    return Regex("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        .find(contactBlock)
        ?.groupValues
        ?.get(1)
}

private fun String.extractMessagePreview(): String? {
    val textBody = Regex("\"body\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        .find(this)
        ?.groupValues
        ?.get(1)
    if (!textBody.isNullOrBlank()) return textBody.take(160)
    val type = extractJsonString("type")
    return type?.let { "WhatsApp $it message" }
}
