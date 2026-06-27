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
import com.orma.backend.models.MetaWhatsAppCreatedTemplateListResponse
import com.orma.backend.models.MetaWhatsAppCreatedTemplateResponse
import com.orma.backend.models.MetaWhatsAppTemplateCreateRequest
import com.orma.backend.models.MetaWhatsAppTemplateCreateResponse
import com.orma.backend.models.MetaWhatsAppTemplateListResponse
import com.orma.backend.models.MetaWhatsAppTemplateResponse
import com.orma.backend.models.MetaWhatsAppTemplateSyncItemResponse
import com.orma.backend.models.MetaWhatsAppTemplateSyncResponse
import com.orma.backend.meta.MetaAccessToken
import com.orma.backend.meta.MetaGraphCatalogProductRequest
import com.orma.backend.meta.MetaGraphClient
import com.orma.backend.meta.MetaGraphException
import com.orma.backend.meta.MetaGraphWhatsAppTemplate
import com.orma.backend.meta.MetaGraphWhatsAppTemplateRequest
import com.orma.backend.meta.MetaTokenCrypto
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class MetaConnectionValidationException(
    val code: String,
    publicMessage: String,
) : IllegalArgumentException(publicMessage)

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
            connection.ensureMetaPhoneNumberAvailable(access.workspaceId, request.phoneNumberId)
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

    fun defaultWhatsAppTemplates(): MetaWhatsAppTemplateListResponse =
        MetaWhatsAppTemplateListResponse(
            templates = OrmaWhatsAppTemplates.map { it.toResponse(config.metaDefaultLanguageCode) },
        )

    suspend fun listCreatedWhatsAppTemplates(
        firebaseUser: VerifiedFirebaseUser,
    ): MetaWhatsAppCreatedTemplateListResponse? = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            MetaTemplateSyncContext(
                workspaceId = access.workspaceId,
                connection = connection.findMetaConnection(access.workspaceId),
            )
        }
        val connectionState = context.connection
        val accessToken = connectionState?.resolveAccessToken()
        val whatsappBusinessAccountId = connectionState
            ?.whatsappBusinessAccountId
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        if (accessToken.isNullOrBlank() || whatsappBusinessAccountId == null) {
            return@withContext MetaWhatsAppCreatedTemplateListResponse(
                connected = false,
                message = "Connect WhatsApp Business Account and backend Meta token before loading templates.",
            )
        }

        try {
            val templates = graphClient.listWhatsAppTemplates(
                accessToken = accessToken,
                whatsappBusinessAccountId = whatsappBusinessAccountId,
            )
            dataSource.connection.use { connection ->
                connection.updateMetaMessagingStatus(context.workspaceId, "ready", null)
            }
            MetaWhatsAppCreatedTemplateListResponse(
                connected = true,
                templates = templates.map { it.toResponse() },
                message = "Loaded ${templates.size} WhatsApp template(s) from Meta.",
            )
        } catch (error: MetaGraphException) {
            val publicMessage = error.publicMetaMessage()
            dataSource.connection.use { connection ->
                connection.updateMetaMessagingStatus(context.workspaceId, "template_list_failed", publicMessage)
            }
            MetaWhatsAppCreatedTemplateListResponse(
                connected = false,
                message = "Meta could not load WhatsApp templates. $publicMessage",
            )
        }
    }

    suspend fun createWhatsAppTemplate(
        firebaseUser: VerifiedFirebaseUser,
        request: MetaWhatsAppTemplateCreateRequest,
    ): MetaWhatsAppTemplateCreateResponse? = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            MetaTemplateSyncContext(
                workspaceId = access.workspaceId,
                connection = connection.findMetaConnection(access.workspaceId),
            )
        }
        val connectionState = context.connection
        val accessToken = connectionState?.resolveAccessToken()
        val whatsappBusinessAccountId = connectionState
            ?.whatsappBusinessAccountId
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        if (accessToken.isNullOrBlank() || whatsappBusinessAccountId == null) {
            return@withContext MetaWhatsAppTemplateCreateResponse(
                created = false,
                status = "setup_required",
                message = "Connect WhatsApp Business Account and backend Meta token before creating templates.",
            )
        }

        val templateName = request.name.cleanMetaTemplateName()
        val bodyText = request.bodyText.cleanMetaTemplateBody()
        if (templateName.length < 3 || bodyText.length < 10) {
            return@withContext MetaWhatsAppTemplateCreateResponse(
                created = false,
                status = "invalid_request",
                message = "Template name and body are required. Name must be at least 3 characters and body at least 10 characters.",
            )
        }

        val graphRequest = MetaGraphWhatsAppTemplateRequest(
            name = templateName,
            category = request.category.cleanMetaTemplateCategory(),
            languageCode = request.languageCode.cleanMetaTemplateLanguage(config.metaDefaultLanguageCode),
            bodyText = bodyText,
            sampleParameters = bodyText.metaTemplateSamples(request.sampleParameters),
        )

        try {
            val result = graphClient.createWhatsAppTemplate(
                accessToken = accessToken,
                whatsappBusinessAccountId = whatsappBusinessAccountId,
                template = graphRequest,
            )
            dataSource.connection.use { connection ->
                connection.updateMetaMessagingStatus(context.workspaceId, "templates_submitted", null)
            }
            MetaWhatsAppTemplateCreateResponse(
                created = true,
                status = result.status?.lowercase()?.takeIf { it.isNotBlank() } ?: "submitted",
                template = MetaWhatsAppCreatedTemplateResponse(
                    id = result.id,
                    name = graphRequest.name,
                    status = result.status?.lowercase()?.takeIf { it.isNotBlank() } ?: "submitted",
                    category = graphRequest.category,
                    languageCode = graphRequest.languageCode,
                    bodyText = graphRequest.bodyText,
                ),
                message = "Template submitted to Meta for approval.",
            )
        } catch (error: MetaGraphException) {
            val publicMessage = error.publicMetaMessage()
            if (!publicMessage.isTemplateAlreadyCreatedError()) {
                dataSource.connection.use { connection ->
                    connection.updateMetaMessagingStatus(context.workspaceId, "template_create_failed", publicMessage)
                }
            }
            MetaWhatsAppTemplateCreateResponse(
                created = false,
                status = if (publicMessage.isTemplateAlreadyCreatedError()) "exists" else "failed",
                message = if (publicMessage.isTemplateAlreadyCreatedError()) {
                    "Template already exists in Meta."
                } else {
                    publicMessage
                },
            )
        }
    }

    suspend fun syncWhatsAppTemplates(firebaseUser: VerifiedFirebaseUser): MetaWhatsAppTemplateSyncResponse? = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            val access = connection.resolveMetaWorkspaceAccess(firebaseUser) ?: return@withContext null
            MetaTemplateSyncContext(
                workspaceId = access.workspaceId,
                connection = connection.findMetaConnection(access.workspaceId),
            )
        }
        val connectionState = context.connection
        val accessToken = connectionState?.resolveAccessToken()
        val whatsappBusinessAccountId = connectionState
            ?.whatsappBusinessAccountId
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        if (accessToken.isNullOrBlank() || whatsappBusinessAccountId == null) {
            return@withContext MetaWhatsAppTemplateSyncResponse(
                connected = false,
                created = 0,
                failed = 0,
                templates = OrmaWhatsAppTemplates.map {
                    MetaWhatsAppTemplateSyncItemResponse(
                        name = it.name,
                        status = "setup_required",
                        message = "Connect WhatsApp Business Account and backend Meta token before creating templates.",
                    )
                },
                message = "WhatsApp template sync needs a connected WhatsApp Business Account and backend Meta token.",
            )
        }

        val results = mutableListOf<MetaWhatsAppTemplateSyncItemResponse>()
        var created = 0
        var failed = 0
        OrmaWhatsAppTemplates.forEach { template ->
            try {
                val result = graphClient.createWhatsAppTemplate(
                    accessToken = accessToken,
                    whatsappBusinessAccountId = whatsappBusinessAccountId,
                    template = template.toGraphRequest(config.metaDefaultLanguageCode),
                )
                created += 1
                results += MetaWhatsAppTemplateSyncItemResponse(
                    name = template.name,
                    status = result.status?.lowercase()?.takeIf { it.isNotBlank() } ?: "submitted",
                    message = "Submitted to Meta for approval.",
                )
            } catch (error: MetaGraphException) {
                val publicMessage = error.publicMetaMessage()
                if (publicMessage.isTemplateAlreadyCreatedError()) {
                    results += MetaWhatsAppTemplateSyncItemResponse(
                        name = template.name,
                        status = "exists",
                        message = "Template already exists in Meta.",
                    )
                } else {
                    failed += 1
                    results += MetaWhatsAppTemplateSyncItemResponse(
                        name = template.name,
                        status = "failed",
                        message = publicMessage,
                    )
                }
            }
        }

        dataSource.connection.use { connection ->
            connection.updateMetaMessagingStatus(
                workspaceId = context.workspaceId,
                status = if (failed == 0) "templates_submitted" else "template_sync_failed",
                error = if (failed == 0) null else "$failed WhatsApp templates could not be submitted.",
            )
        }

        MetaWhatsAppTemplateSyncResponse(
            connected = true,
            created = created,
            failed = failed,
            templates = results,
            message = if (failed == 0) {
                "WhatsApp templates submitted to Meta. Approval happens inside Meta."
            } else {
                "Template sync finished with failures. Review each failed template message."
            },
        )
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
            ?: context.defaultWhatsAppTemplateName(config.metaDefaultOrderTemplate)
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
        val webhook = payload.toMetaWebhookPayload()
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            val phoneNumberId = webhook.phoneNumberId ?: payload.extractJsonString("phone_number_id")
            val workspaceId = phoneNumberId?.let { connection.findMetaWorkspaceByPhoneNumberId(it) }
            try {
                val messages = webhook.messages.ifEmpty {
                    listOf(
                        MetaWebhookMessage(
                            id = webhook.statusMessageId,
                            from = null,
                            waId = null,
                            customerName = null,
                            type = webhook.eventType ?: payload.extractWebhookEventType(),
                            preview = webhook.eventType?.let { "WhatsApp $it update" } ?: "WhatsApp webhook update",
                            order = null,
                        ),
                    )
                }
                var response: MetaWebhookEventResponse? = null
                messages.forEach { message ->
                    val event = connection.upsertMetaWebhookEvent(
                        workspaceId = workspaceId,
                        objectType = webhook.objectType ?: payload.extractJsonString("object"),
                        eventType = webhook.eventType ?: payload.extractWebhookEventType(),
                        externalMessageId = message.id,
                        payload = payload,
                    )
                    var status = event.status
                    var convertedOrderId = event.convertedOrderId
                    var responseMessage = "Webhook received."

                    if (workspaceId != null && message.waId != null) {
                        val lead = connection.upsertMetaLeadFromWebhook(
                            workspaceId = workspaceId,
                            phoneNumberId = phoneNumberId,
                            waId = message.waId,
                            customerName = message.customerName,
                            phoneNumber = message.from,
                            preview = message.preview,
                        )
                        if (message.order != null) {
                            if (convertedOrderId == null) {
                                convertedOrderId = connection.createWhatsAppDraftOrder(
                                    workspaceId = workspaceId,
                                    lead = lead,
                                    message = message,
                                )
                                if (convertedOrderId != null) {
                                    connection.markMetaWebhookEvent(
                                        eventId = event.id,
                                        status = "processed",
                                        convertedOrderId = convertedOrderId,
                                        processingError = null,
                                    )
                                    connection.markMetaLeadConverted(
                                        leadId = lead.leadId,
                                        orderId = convertedOrderId,
                                    )
                                    status = "processed"
                                    responseMessage = "WhatsApp order created in ORMA."
                                } else {
                                    connection.markMetaWebhookEvent(
                                        eventId = event.id,
                                        status = "needs_review",
                                        convertedOrderId = null,
                                        processingError = "WhatsApp order did not include usable product items.",
                                    )
                                    status = "needs_review"
                                    responseMessage = "WhatsApp order was saved as a lead and needs review."
                                }
                            } else {
                                status = "processed"
                                responseMessage = "WhatsApp order was already created in ORMA."
                            }
                        }
                    } else if (workspaceId == null) {
                        connection.markMetaWebhookEvent(
                            eventId = event.id,
                            status = "unmatched_workspace",
                            convertedOrderId = null,
                            processingError = "No ORMA workspace is connected to this WhatsApp phone number ID.",
                        )
                        status = "unmatched_workspace"
                        responseMessage = "Webhook received, but no workspace matched this WhatsApp number."
                    }

                    if (response == null || convertedOrderId != null) {
                        response = MetaWebhookEventResponse(
                            id = event.id,
                            status = status,
                            convertedOrderId = convertedOrderId,
                            message = responseMessage,
                        )
                    }
                }
                if (workspaceId != null) {
                    connection.updateMetaMessagingStatus(workspaceId, "ready", null)
                }
                connection.commit()
                response ?: MetaWebhookEventResponse(id = "", status = "received")
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

    private fun Connection.upsertMetaWebhookEvent(
        workspaceId: String?,
        objectType: String?,
        eventType: String?,
        externalMessageId: String?,
        payload: String,
    ): MetaWebhookEventRow {
        val sql = """
            insert into meta_webhook_events (
                workspace_id, object_type, event_type, external_message_id, payload
            )
            values (
                ?::uuid,
                nullif(?,''),
                nullif(?,''),
                nullif(?,''),
                ?::jsonb
            )
            on conflict (workspace_id, external_message_id)
                where workspace_id is not null and external_message_id is not null
            do update
            set payload = excluded.payload
            returning id::text, status, converted_order_id::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setStringOrNull(1, workspaceId)
            statement.setString(2, objectType.orEmpty())
            statement.setString(3, eventType.orEmpty())
            statement.setString(4, externalMessageId.orEmpty())
            statement.setString(5, payload.ifBlank { "{}" })
            statement.executeQuery().use { result ->
                result.next()
                MetaWebhookEventRow(
                    id = result.getString("id"),
                    status = result.getString("status") ?: "received",
                    convertedOrderId = result.getString("converted_order_id"),
                )
            }
        }
    }

    private fun Connection.markMetaWebhookEvent(
        eventId: String,
        status: String,
        convertedOrderId: String?,
        processingError: String?,
    ) {
        val sql = """
            update meta_webhook_events
            set status = ?,
                processed_at = now(),
                converted_order_id = ?::uuid,
                processing_error = ?
            where id = ?::uuid
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, status)
            statement.setStringOrNull(2, convertedOrderId)
            statement.setStringOrNull(3, processingError)
            statement.setString(4, eventId)
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
                o.order_type,
                o.status,
                o.fulfillment_type,
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
                        orderType = result.getString("order_type") ?: "sale",
                        status = result.getString("status"),
                        fulfillmentType = result.getString("fulfillment_type") ?: "standard",
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
    ): MetaLeadContext {
        val cleanWaId = waId?.trim()?.takeIf { it.isNotBlank() }
            ?: return MetaLeadContext(
                threadId = null,
                leadId = null,
                convertedOrderId = null,
            )
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
        return prepareStatement(
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
            returning id::text, converted_order_id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, threadId)
            statement.setStringOrNull(3, customerName)
            statement.setStringOrNull(4, phoneNumber)
            statement.setStringOrNull(5, preview)
            statement.executeQuery().use { result ->
                result.next()
                MetaLeadContext(
                    threadId = threadId,
                    leadId = result.getString("id"),
                    convertedOrderId = result.getString("converted_order_id"),
                )
            }
        }
    }

    private fun Connection.markMetaLeadConverted(
        leadId: String?,
        orderId: String,
    ) {
        val cleanLeadId = leadId?.trim()?.takeIf { it.isNotBlank() } ?: return
        prepareStatement(
            """
            update meta_leads
            set status = 'converted',
                converted_order_id = ?::uuid,
                updated_at = now()
            where id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, orderId)
            statement.setString(2, cleanLeadId)
            statement.executeUpdate()
        }
    }

    private fun Connection.createWhatsAppDraftOrder(
        workspaceId: String,
        lead: MetaLeadContext,
        message: MetaWebhookMessage,
    ): String? {
        val order = message.order ?: return null
        val preparedItems = order.items.mapNotNull { item ->
            val quantity = item.quantity.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
            val product = findWhatsAppOrderProduct(workspaceId, item.productRetailerId)
            val unitPrice = item.itemPrice
                ?.takeIf { it >= BigDecimal.ZERO }
                ?: product?.sellingPrice
                ?: BigDecimal.ZERO
            MetaPreparedWhatsAppOrderItem(
                productId = product?.productId,
                description = product?.name ?: "WhatsApp item ${item.productRetailerId.take(80)}",
                quantity = quantity,
                unitPrice = unitPrice.scaled(),
                taxRate = product?.taxRate ?: BigDecimal.ZERO,
            )
        }
        if (preparedItems.isEmpty()) return null

        val customerId = findOrCreateWhatsAppCustomer(
            workspaceId = workspaceId,
            customerName = message.customerName,
            phoneNumber = message.from,
        )
        val subtotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineSubtotal) }.scaled()
        val taxTotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineTax) }.scaled()
        val total = preparedItems.fold(BigDecimal.ZERO) { sum, item -> sum.add(item.lineTotal) }.scaled()
        val currency = order.items.firstNotNullOfOrNull { it.currency?.trim()?.uppercase()?.takeIf(String::isNotBlank) }
            ?: findWorkspaceCurrency(workspaceId)
        val orderNumber = "WA-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        val notes = buildString {
            append("Created from WhatsApp API")
            message.id?.takeIf { it.isNotBlank() }?.let { append(" message $it") }
            lead.threadId?.takeIf { it.isNotBlank() }?.let { append(". Thread $it") }
            order.text?.takeIf { it.isNotBlank() }?.let { append(". Customer note: ${it.take(180)}") }
        }
        val orderId = prepareStatement(
            """
            insert into orders (
                workspace_id, customer_id, order_number, order_type, status, scheduled_at,
                subtotal, tax_total, discount_total, paid_total, total, currency,
                notes, inventory_applied, created_by_user_id, source, fulfillment_type, payment_mode, updated_at
            )
            values (
                ?::uuid, ?::uuid, ?, 'sale', 'draft', null,
                ?, ?, 0, 0, ?, ?, ?, false, null, 'whatsapp', 'standard', 'pay_on_spot', now()
            )
            returning id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setStringOrNull(2, customerId)
            statement.setString(3, orderNumber)
            statement.setBigDecimal(4, subtotal)
            statement.setBigDecimal(5, taxTotal)
            statement.setBigDecimal(6, total)
            statement.setString(7, currency)
            statement.setString(8, notes)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
        preparedItems.forEach { item ->
            insertWhatsAppOrderItem(orderId, item)
        }
        return orderId
    }

    private fun Connection.findOrCreateWhatsAppCustomer(
        workspaceId: String,
        customerName: String?,
        phoneNumber: String?,
    ): String? {
        val cleanPhone = phoneNumber.toWhatsAppCustomerPhone()
        if (cleanPhone != null) {
            prepareStatement(
                """
                select id::text
                from customers
                where workspace_id = ?::uuid
                  and status = 'active'
                  and phone_number = ?
                limit 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, workspaceId)
                statement.setString(2, cleanPhone)
                statement.executeQuery().use { result ->
                    if (result.next()) return result.getString("id")
                }
            }
        }
        return prepareStatement(
            """
            insert into customers (
                workspace_id, name, phone_number, notes, updated_at
            )
            values (?::uuid, ?, ?, 'Created from WhatsApp API order.', now())
            returning id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, customerName.cleanWhatsAppCustomerName())
            statement.setStringOrNull(3, cleanPhone)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
    }

    private fun Connection.findWhatsAppOrderProduct(
        workspaceId: String,
        productRetailerId: String,
    ): MetaWhatsAppOrderProduct? {
        val cleanRetailerId = productRetailerId.trim().takeIf { it.isNotBlank() } ?: return null
        val sql = """
            select
                id::text,
                name,
                selling_price,
                currency,
                tax_rate
            from products
            where workspace_id = ?::uuid
              and status = 'active'
              and (
                id::text = ?
                or upper(coalesce(sku, '')) = upper(?)
                or coalesce(barcode, '') = ?
              )
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, cleanRetailerId)
            statement.setString(3, cleanRetailerId)
            statement.setString(4, cleanRetailerId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    MetaWhatsAppOrderProduct(
                        productId = result.getString("id"),
                        name = result.getString("name"),
                        sellingPrice = result.getBigDecimal("selling_price") ?: BigDecimal.ZERO,
                        currency = result.getString("currency") ?: "INR",
                        taxRate = result.getBigDecimal("tax_rate") ?: BigDecimal.ZERO,
                    )
                }
            }
        }
    }

    private fun Connection.insertWhatsAppOrderItem(
        orderId: String,
        item: MetaPreparedWhatsAppOrderItem,
    ) {
        prepareStatement(
            """
            insert into order_items (
                order_id, product_id, description, quantity, unit_price, tax_rate,
                line_subtotal, line_tax, line_total
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, orderId)
            statement.setStringOrNull(2, item.productId)
            statement.setString(3, item.description.cleanWhatsAppDescription())
            statement.setBigDecimal(4, item.quantity)
            statement.setBigDecimal(5, item.unitPrice)
            statement.setBigDecimal(6, item.taxRate)
            statement.setBigDecimal(7, item.lineSubtotal)
            statement.setBigDecimal(8, item.lineTax)
            statement.setBigDecimal(9, item.lineTotal)
            statement.executeUpdate()
        }
    }

    private fun Connection.findWorkspaceCurrency(workspaceId: String): String =
        prepareStatement("select currency from business_workspaces where id = ?::uuid limit 1").use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("currency") ?: "INR" else "INR"
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
                wm.permissions,
                bw.currency,
                bw.business_mode
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
                        permissions = result.getStringArray("permissions"),
                        currency = result.getString("currency") ?: "INR",
                        businessMode = result.getString("business_mode") ?: "product_selling",
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
            credentialSource = connection?.credentialSource ?: "none",
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

    private fun Connection.ensureMetaPhoneNumberAvailable(
        workspaceId: String,
        phoneNumberId: String?,
    ) {
        val cleanPhoneNumberId = phoneNumberId?.trim()?.takeIf { it.isNotBlank() } ?: return
        val sql = """
            select workspace_id::text
            from meta_connections
            where phone_number_id = ?
              and workspace_id <> ?::uuid
            limit 1
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, cleanPhoneNumberId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    throw MetaConnectionValidationException(
                        code = "meta_phone_number_already_connected",
                        publicMessage = "This WhatsApp Phone Number ID is already connected to another ORMA business.",
                    )
                }
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
    val orderType: String,
    val status: String,
    val fulfillmentType: String,
    val total: String,
    val currency: String,
    val customerName: String?,
    val customerPhoneNumber: String?,
    val phoneNumberId: String?,
    val credentialSource: String,
    val accessTokenCiphertext: String?,
)

private data class MetaTemplateSyncContext(
    val workspaceId: String,
    val connection: MetaConnectionRow?,
)

private data class MetaWebhookEventRow(
    val id: String,
    val status: String,
    val convertedOrderId: String?,
)

private data class MetaLeadContext(
    val threadId: String?,
    val leadId: String?,
    val convertedOrderId: String?,
)

private data class MetaWhatsAppOrderProduct(
    val productId: String,
    val name: String,
    val sellingPrice: BigDecimal,
    val currency: String,
    val taxRate: BigDecimal,
)

private data class MetaPreparedWhatsAppOrderItem(
    val productId: String?,
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal,
) {
    val lineSubtotal: BigDecimal = quantity.multiply(unitPrice).scaled()
    val lineTax: BigDecimal = lineSubtotal
        .multiply(taxRate)
        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .scaled()
    val lineTotal: BigDecimal = lineSubtotal.add(lineTax).scaled()
}

private data class OrmaWhatsAppTemplateSpec(
    val name: String,
    val bodyText: String,
    val category: String = "UTILITY",
    val sampleParameters: List<String> = listOf("ORD-123456", "Jibin Cherian", "INR 1500.00", "confirmed"),
) {
    fun toResponse(languageCode: String): MetaWhatsAppTemplateResponse =
        MetaWhatsAppTemplateResponse(
            name = name,
            category = category,
            languageCode = languageCode,
            bodyText = bodyText,
            sampleParameters = sampleParameters,
        )

    fun toGraphRequest(languageCode: String): MetaGraphWhatsAppTemplateRequest =
        MetaGraphWhatsAppTemplateRequest(
            name = name,
            category = category,
            languageCode = languageCode,
            bodyText = bodyText,
            sampleParameters = sampleParameters,
        )
}

private fun MetaGraphWhatsAppTemplate.toResponse(): MetaWhatsAppCreatedTemplateResponse =
    MetaWhatsAppCreatedTemplateResponse(
        id = id,
        name = name,
        status = status.lowercase(),
        category = category,
        languageCode = languageCode,
        bodyText = bodyText,
        rejectedReason = rejectedReason,
    )

private fun String.cleanMetaTemplateName(): String =
    lowercase()
        .map { char -> if (char.isLetterOrDigit()) char else '_' }
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')
        .take(512)

private fun String.cleanMetaTemplateBody(): String =
    trim()
        .replace(Regex("\\s+"), " ")
        .take(1024)

private fun String?.cleanMetaTemplateCategory(): String =
    when (this?.trim()?.uppercase()) {
        "AUTHENTICATION" -> "AUTHENTICATION"
        "MARKETING" -> "MARKETING"
        else -> "UTILITY"
    }

private fun String?.cleanMetaTemplateLanguage(defaultLanguageCode: String): String {
    val clean = this
        ?.trim()
        ?.replace("-", "_")
        ?.filter { it.isLetterOrDigit() || it == '_' }
        ?.take(12)
        ?.takeIf { it.isNotBlank() }
    return clean ?: defaultLanguageCode
}

private fun String.metaTemplateSamples(requestSamples: List<String>): List<String> {
    val parameterCount = Regex("""\{\{\s*(\d+)\s*}}""")
        .findAll(this)
        .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
        .maxOrNull()
        ?: 0
    if (parameterCount <= 0) return emptyList()
    val samples = requestSamples
        .map { it.trim().take(120) }
        .filter { it.isNotBlank() }
        .toMutableList()
    MetaTemplateSampleDefaults.forEach { sample ->
        if (samples.size < parameterCount) samples += sample
    }
    while (samples.size < parameterCount) {
        samples += "Sample ${samples.size + 1}"
    }
    return samples.take(parameterCount)
}

private val MetaTemplateSampleDefaults = listOf(
    "ORD-123456",
    "Jibin Cherian",
    "INR 1500.00",
    "confirmed",
    "Tomorrow 10 AM",
)

private data class MetaWebhookPayload(
    val objectType: String?,
    val eventType: String?,
    val phoneNumberId: String?,
    val statusMessageId: String?,
    val messages: List<MetaWebhookMessage>,
)

private data class MetaWebhookContact(
    val waId: String?,
    val name: String?,
)

private data class MetaWebhookMessage(
    val id: String?,
    val from: String?,
    val waId: String?,
    val customerName: String?,
    val type: String?,
    val preview: String,
    val order: MetaWebhookOrder?,
)

private data class MetaWebhookOrder(
    val catalogId: String?,
    val text: String?,
    val items: List<MetaWebhookOrderItem>,
)

private data class MetaWebhookOrderItem(
    val productRetailerId: String,
    val quantity: BigDecimal,
    val itemPrice: BigDecimal?,
    val currency: String?,
)

private val OrmaWhatsAppTemplates = listOf(
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_order_confirmed",
        bodyText = "Hi {{2}}, your product order {{1}} is confirmed. Total: {{3}}. Current status: {{4}}. We will update you here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_payment_reminder",
        bodyText = "Hi {{2}}, payment is pending for order {{1}}. Amount: {{3}}. Current status: {{4}}. Please complete payment to continue.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_payment_received",
        bodyText = "Hi {{2}}, payment is received for order {{1}}. Amount: {{3}}. Current status: {{4}}. Thank you.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_order_ready",
        bodyText = "Hi {{2}}, your order {{1}} is ready. Total: {{3}}. Current status: {{4}}. We will share the next update here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_order_dispatched",
        bodyText = "Hi {{2}}, your order {{1}} has been dispatched. Total: {{3}}. Current status: {{4}}. We will update you after completion.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_order_completed",
        bodyText = "Hi {{2}}, your order {{1}} is completed. Total: {{3}}. Current status: {{4}}. Thank you for ordering with us.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_product_order_cancelled",
        bodyText = "Hi {{2}}, your order {{1}} is cancelled. Total: {{3}}. Current status: {{4}}. Reply here if this needs review.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_service_request_confirmed",
        bodyText = "Hi {{2}}, your service request {{1}} is confirmed. Estimate: {{3}}. Current status: {{4}}. We will update you here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_service_scheduled",
        bodyText = "Hi {{2}}, your service request {{1}} is scheduled. Estimate: {{3}}. Current status: {{4}}. We will share updates here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_service_in_progress",
        bodyText = "Hi {{2}}, work has started for service request {{1}}. Estimate: {{3}}. Current status: {{4}}. We will update you here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_service_completed",
        bodyText = "Hi {{2}}, service request {{1}} is completed. Total: {{3}}. Current status: {{4}}. Thank you.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_service_cancelled",
        bodyText = "Hi {{2}}, service request {{1}} is cancelled. Estimate: {{3}}. Current status: {{4}}. Reply here if this needs review.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_appointment_confirmed",
        bodyText = "Hi {{2}}, your appointment booking {{1}} is confirmed. Amount: {{3}}. Current status: {{4}}. We will update you here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_appointment_reminder",
        bodyText = "Hi {{2}}, this is a reminder for appointment {{1}}. Amount: {{3}}. Current status: {{4}}. Reply here if you need help.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_appointment_rescheduled",
        bodyText = "Hi {{2}}, appointment {{1}} has been rescheduled. Amount: {{3}}. Current status: {{4}}. We will update you here.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_appointment_completed",
        bodyText = "Hi {{2}}, appointment {{1}} is completed. Total: {{3}}. Current status: {{4}}. Thank you.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_appointment_cancelled",
        bodyText = "Hi {{2}}, appointment {{1}} is cancelled. Amount: {{3}}. Current status: {{4}}. Reply here if this needs review.",
    ),
    OrmaWhatsAppTemplateSpec(
        name = "orma_whatsapp_inquiry_received",
        bodyText = "Hi {{2}}, we received your WhatsApp inquiry {{1}}. Estimated amount: {{3}}. Current status: {{4}}. We will respond here.",
    ),
)

private val MetaWebhookJson = Json { ignoreUnknownKeys = true }

private fun String.toMetaWebhookPayload(): MetaWebhookPayload {
    val root = runCatching {
        MetaWebhookJson.parseToJsonElement(ifBlank { "{}" }) as? JsonObject
    }.getOrNull()
    val messages = mutableListOf<MetaWebhookMessage>()
    var eventType: String? = null
    var phoneNumberId: String? = null
    var statusMessageId: String? = null

    root?.objects("entry").orEmpty().forEach { entry ->
        entry.objects("changes").forEach { change ->
            eventType = eventType ?: change.string("field")
            val value = change.obj("value") ?: return@forEach
            phoneNumberId = phoneNumberId ?: value.obj("metadata")?.string("phone_number_id")
            val contacts = value.objects("contacts").map { contact ->
                MetaWebhookContact(
                    waId = contact.string("wa_id"),
                    name = contact.obj("profile")?.string("name"),
                )
            }
            statusMessageId = statusMessageId ?: value.objects("statuses").firstOrNull()?.string("id")
            value.objects("messages").forEach { message ->
                val from = message.string("from")
                val contact = contacts.firstOrNull { it.waId == from } ?: contacts.firstOrNull()
                val type = message.string("type")
                val textBody = message.obj("text")?.string("body")
                val order = message.obj("order")?.toMetaWebhookOrder()
                messages += MetaWebhookMessage(
                    id = message.string("id"),
                    from = from,
                    waId = contact?.waId ?: from,
                    customerName = contact?.name,
                    type = type,
                    preview = when {
                        order != null -> "WhatsApp order with ${order.items.size} item(s)"
                        !textBody.isNullOrBlank() -> textBody.take(160)
                        !type.isNullOrBlank() -> "WhatsApp $type message"
                        else -> "WhatsApp message"
                    },
                    order = order,
                )
            }
        }
    }

    return MetaWebhookPayload(
        objectType = root?.string("object"),
        eventType = eventType,
        phoneNumberId = phoneNumberId,
        statusMessageId = statusMessageId,
        messages = messages,
    )
}

private fun JsonObject.toMetaWebhookOrder(): MetaWebhookOrder =
    MetaWebhookOrder(
        catalogId = string("catalog_id"),
        text = string("text"),
        items = objects("product_items").mapNotNull { item ->
            val retailerId = item.string("product_retailer_id")?.trim()?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            MetaWebhookOrderItem(
                productRetailerId = retailerId,
                quantity = item.string("quantity").metaDecimalOrNull()?.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE,
                itemPrice = item.string("item_price").metaDecimalOrNull(),
                currency = item.string("currency"),
            )
        },
    )

private fun JsonObject.obj(key: String): JsonObject? =
    get(key) as? JsonObject

private fun JsonObject.objects(key: String): List<JsonObject> =
    (get(key) as? JsonArray)
        ?.mapNotNull { it as? JsonObject }
        .orEmpty()

private fun JsonObject.string(key: String): String? =
    (get(key) as? JsonPrimitive)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.takeIf { it.isNotBlank() }

private fun String?.metaDecimalOrNull(): BigDecimal? =
    this
        ?.trim()
        ?.replace(",", "")
        ?.toBigDecimalOrNull()
        ?.scaled()

private fun String?.toWhatsAppCustomerPhone(): String? {
    val digits = this?.filter(Char::isDigit).orEmpty()
    return digits.takeIf { it.length >= 7 }?.let { "+$it" }
}

private fun String?.cleanWhatsAppCustomerName(): String =
    this?.trim()?.take(180)?.ifBlank { null } ?: "WhatsApp customer"

private fun String.cleanWhatsAppDescription(): String =
    trim().take(240).ifBlank { "WhatsApp item" }

private fun BigDecimal.scaled(): BigDecimal =
    setScale(2, RoundingMode.HALF_UP)

private fun MetaCatalogProductRow.readinessIssues(connected: Boolean): List<String> = buildList {
    if (!connected) add("Connect Meta before syncing this item.")
    if (status != "active") add("Set product status active.")
    if (sellingPrice <= BigDecimal.ZERO) add("Add a selling price.")
    if (imageStoragePath.isNullOrBlank()) add("Add a product image.")
    if (trackStock && stockQuantity <= BigDecimal.ZERO) add("Restock before promoting this item.")
    if (trackStock && stockQuantity <= reorderLevel) add("Stock is at or below the reorder level.")
}

private fun MetaOrderWhatsAppContext.defaultWhatsAppTemplateName(fallback: String): String {
    val statusKey = status.lowercase()
    return when (orderType.lowercase()) {
        "appointment" -> when (statusKey) {
            "completed" -> "orma_appointment_completed"
            "cancelled" -> "orma_appointment_cancelled"
            "draft" -> "orma_appointment_reminder"
            else -> "orma_appointment_confirmed"
        }

        "service" -> when (statusKey) {
            "completed" -> "orma_service_completed"
            "cancelled" -> "orma_service_cancelled"
            "confirmed" -> if (fulfillmentType == "scheduled") {
                "orma_service_scheduled"
            } else {
                "orma_service_request_confirmed"
            }
            else -> "orma_service_request_confirmed"
        }

        else -> when (statusKey) {
            "draft" -> "orma_product_payment_reminder"
            "paid", "part_paid" -> "orma_product_payment_received"
            "completed" -> "orma_product_order_completed"
            "cancelled" -> "orma_product_order_cancelled"
            "confirmed" -> "orma_product_order_confirmed"
            else -> fallback.trim().takeIf { it.isNotBlank() } ?: "orma_product_order_confirmed"
        }
    }
}

private fun String.isTemplateAlreadyCreatedError(): Boolean {
    val message = lowercase()
    return "already exists" in message ||
        "duplicate" in message ||
        "name must be unique" in message ||
        "same name" in message
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

private fun ResultSet.getStringArray(column: String): List<String> =
    (getArray(column)?.array as? Array<*>)
        ?.mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
        .orEmpty()

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
