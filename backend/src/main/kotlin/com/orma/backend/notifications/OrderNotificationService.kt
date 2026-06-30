package com.orma.backend.notifications

import com.orma.backend.config.AppConfig
import com.orma.backend.models.OrderResponse
import java.nio.file.Files
import java.nio.file.Path
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.sql.Connection
import java.sql.ResultSet
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

class OrderNotificationService(
    private val dataSource: DataSource,
    private val config: AppConfig,
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    suspend fun notifyOrderCreated(order: OrderResponse) {
        runCatching {
            notifyOrderEventInternal(order, OrderNotificationEventKind.Created)
        }.onFailure { error ->
            logger.warn("Order notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    suspend fun notifyOrderStatusUpdated(order: OrderResponse) {
        runCatching {
            notifyOrderEventInternal(order, OrderNotificationEventKind.StatusUpdated)
        }.onFailure { error ->
            logger.warn("Order status notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    suspend fun notifyOrderUpdated(order: OrderResponse) {
        runCatching {
            notifyOrderEventInternal(order, OrderNotificationEventKind.Updated)
        }.onFailure { error ->
            logger.warn("Order update notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    suspend fun notifyOrderChangeRequested(order: OrderResponse) {
        runCatching {
            notifyOrderEventInternal(order, OrderNotificationEventKind.ChangeRequested)
        }.onFailure { error ->
            logger.warn("Order change request notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    suspend fun notifyOrderChangeResolved(order: OrderResponse, approved: Boolean) {
        runCatching {
            notifyOrderEventInternal(
                order = order,
                kind = if (approved) OrderNotificationEventKind.ChangeApproved else OrderNotificationEventKind.ChangeRejected,
            )
        }.onFailure { error ->
            logger.warn("Order change resolution notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    private suspend fun notifyOrderEventInternal(
        order: OrderResponse,
        kind: OrderNotificationEventKind,
    ) = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            connection.orderNotificationContext(order.id)
        } ?: return@withContext

        val title = kind.title(context)
        val body = kind.body(context)
        val payload = mapOf(
            "type" to kind.eventType,
            "channel" to kind.channel,
            "orderId" to context.orderId,
            "orderNumber" to context.orderNumber,
            "orderType" to context.orderType,
            "status" to context.status,
            "workspaceId" to context.workspaceId,
        )
        val externalUserIds = dataSource.connection.use { connection ->
            connection.activeOneSignalExternalUserIds(context.workspaceId, kind.channel)
        }
        val apnsDeviceTokens = dataSource.connection.use { connection ->
            connection.activeApnsDeviceTokens(context.workspaceId, kind.channel)
        }
        val customerExternalUserIds = dataSource.connection.use { connection ->
            connection.activePublicCatalogCustomerOneSignalExternalUserIds(context, kind)
        }
        val customerApnsDeviceTokens = dataSource.connection.use { connection ->
            connection.activePublicCatalogCustomerApnsDeviceTokens(context, kind)
        }
        val targetCount = externalUserIds.size +
            apnsDeviceTokens.size +
            customerExternalUserIds.size +
            customerApnsDeviceTokens.size

        val eventId = dataSource.connection.use { connection ->
            connection.insertNotificationEvent(
                context = context,
                eventType = kind.eventType,
                title = title,
                body = body,
                payload = payload,
                targetCount = targetCount,
                status = when {
                    targetCount == 0 -> "no_targets"
                    !config.oneSignalPushConfigured && !config.apnsPushConfigured -> "not_configured"
                    else -> "queued"
                },
            )
        }

        if (targetCount == 0 || (!config.oneSignalPushConfigured && !config.apnsPushConfigured)) return@withContext

        var successCount = 0
        var failureCount = 0

        if (externalUserIds.isNotEmpty()) {
            if (config.oneSignalPushConfigured) {
                val sent = runCatching {
                    sendOneSignalNotification(
                        externalUserIds = externalUserIds,
                        title = title,
                        body = body,
                        payload = payload,
                    )
                }.onFailure { error ->
                    logger.warn("OneSignal send failed for ${kind.eventType} ${context.orderNumber}: ${error.message}")
                }.isSuccess
                if (sent) {
                    successCount += externalUserIds.size
                } else {
                    failureCount += externalUserIds.size
                }
            } else {
                failureCount += externalUserIds.size
            }
        }

        if (customerExternalUserIds.isNotEmpty()) {
            if (config.oneSignalPushConfigured) {
                val sent = runCatching {
                    sendOneSignalNotification(
                        externalUserIds = customerExternalUserIds,
                        title = kind.customerTitle(context),
                        body = kind.customerBody(context),
                        payload = payload + ("audience" to "customer"),
                    )
                }.onFailure { error ->
                    logger.warn("OneSignal customer send failed for ${kind.eventType} ${context.orderNumber}: ${error.message}")
                }.isSuccess
                if (sent) {
                    successCount += customerExternalUserIds.size
                } else {
                    failureCount += customerExternalUserIds.size
                }
            } else {
                failureCount += customerExternalUserIds.size
            }
        }

        if (apnsDeviceTokens.isNotEmpty()) {
            if (config.apnsPushConfigured) {
                apnsDeviceTokens.forEach { token ->
                    val sent = runCatching {
                        sendApnsNotification(
                            deviceToken = token,
                            title = title,
                            body = body,
                            payload = payload,
                        )
                    }.onFailure { error ->
                        logger.warn("APNs send failed for ${kind.eventType} ${context.orderNumber}: ${error.message}")
                    }.isSuccess
                    if (sent) {
                        successCount += 1
                    } else {
                        failureCount += 1
                    }
                }
            } else {
                failureCount += apnsDeviceTokens.size
            }
        }

        if (customerApnsDeviceTokens.isNotEmpty()) {
            if (config.apnsPushConfigured) {
                customerApnsDeviceTokens.forEach { token ->
                    val sent = runCatching {
                        sendApnsNotification(
                            deviceToken = token,
                            title = kind.customerTitle(context),
                            body = kind.customerBody(context),
                            payload = payload + ("audience" to "customer"),
                        )
                    }.onFailure { error ->
                        logger.warn("APNs customer send failed for ${kind.eventType} ${context.orderNumber}: ${error.message}")
                    }.isSuccess
                    if (sent) {
                        successCount += 1
                    } else {
                        failureCount += 1
                    }
                }
            } else {
                failureCount += customerApnsDeviceTokens.size
            }
        }

        dataSource.connection.use { connection ->
            connection.updateNotificationEventDelivery(
                eventId = eventId,
                status = when {
                    successCount > 0 && failureCount == 0 -> "sent"
                    successCount > 0 -> "partial"
                    else -> "failed"
                },
                successCount = successCount,
                failureCount = failureCount,
            )
        }
    }

    private fun sendOneSignalNotification(
        externalUserIds: List<String>,
        title: String,
        body: String,
        payload: Map<String, String>,
    ) {
        val oneSignalAppId = config.oneSignalAppId?.takeIf { it.isNotBlank() }
            ?: error("ONESIGNAL_APP_ID is not configured")
        val oneSignalRestApiKey = config.oneSignalRestApiKey?.takeIf { it.isNotBlank() }
            ?: error("ONESIGNAL_REST_API_KEY is not configured")
        val requestBody = buildJsonObject {
            put("app_id", oneSignalAppId)
            put("target_channel", "push")
            put(
                "include_aliases",
                buildJsonObject {
                    put(
                        "external_id",
                        buildJsonArray {
                            externalUserIds.distinct().forEach { add(JsonPrimitive(it)) }
                        },
                    )
                },
            )
            put("headings", buildJsonObject { put("en", title) })
            put("contents", buildJsonObject { put("en", body) })
            put("data", buildJsonObject { payload.forEach { (key, value) -> put(key, value) } })
            put("ios_sound", "default")
            put("priority", 10)
        }.toString()
        val request = HttpRequest.newBuilder(URI.create(config.oneSignalNotificationsUrl))
            .timeout(Duration.ofSeconds(20))
            .header("Authorization", "Key $oneSignalRestApiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            val responseBody = response.body().orEmpty().take(500)
            error("OneSignal returned HTTP ${response.statusCode()}: $responseBody")
        }
    }

    private fun sendApnsNotification(
        deviceToken: String,
        title: String,
        body: String,
        payload: Map<String, String>,
    ) {
        val bundleId = config.apnsBundleId?.takeIf { it.isNotBlank() }
            ?: error("APNS_BUNDLE_ID is not configured")
        val token = deviceToken.trim().removePrefix("apns:").takeIf { it.isNotBlank() }
            ?: error("APNs device token is blank")
        val requestBody = buildJsonObject {
            put(
                "aps",
                buildJsonObject {
                    put(
                        "alert",
                        buildJsonObject {
                            put("title", title)
                            put("body", body)
                        },
                    )
                    put("sound", "default")
                },
            )
            payload.forEach { (key, value) -> put(key, value) }
        }.toString()
        val host = if (config.apnsUseSandbox) {
            "https://api.sandbox.push.apple.com"
        } else {
            "https://api.push.apple.com"
        }
        val request = HttpRequest.newBuilder(URI.create("$host/3/device/$token"))
            .timeout(Duration.ofSeconds(20))
            .header("Authorization", "bearer ${apnsJwt()}")
            .header("Content-Type", "application/json")
            .header("apns-topic", bundleId)
            .header("apns-push-type", "alert")
            .header("apns-priority", "10")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            val responseBody = response.body().orEmpty().take(500)
            error("APNs returned HTTP ${response.statusCode()}: $responseBody")
        }
    }

    private fun apnsJwt(): String {
        val teamId = config.apnsTeamId?.takeIf { it.isNotBlank() }
            ?: error("APNS_TEAM_ID is not configured")
        val keyId = config.apnsKeyId?.takeIf { it.isNotBlank() }
            ?: error("APNS_KEY_ID is not configured")
        val header = """{"alg":"ES256","kid":"$keyId"}""".base64Url()
        val claims = """{"iss":"$teamId","iat":${Instant.now().epochSecond}}""".base64Url()
        val signingInput = "$header.$claims"
        val signature = Signature.getInstance("SHA256withECDSA").run {
            initSign(apnsPrivateKey())
            update(signingInput.toByteArray(Charsets.UTF_8))
            sign().derEcdsaSignatureToJose().base64Url()
        }
        return "$signingInput.$signature"
    }

    private fun apnsPrivateKey(): PrivateKey {
        val pem = config.apnsPrivateKey?.takeIf { it.isNotBlank() }
            ?: config.apnsPrivateKeyPath
                ?.takeIf { it.isNotBlank() }
                ?.let { Files.readString(Path.of(it)) }
            ?: error("APNS_PRIVATE_KEY or APNS_PRIVATE_KEY_PATH is not configured")
        val keyBytes = Base64.getDecoder().decode(
            pem.lineSequence()
                .filterNot { it.startsWith("-----") }
                .joinToString("")
                .trim(),
        )
        return KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
    }

    private fun Connection.orderNotificationContext(orderId: String): OrderNotificationContext? {
        val sql = """
            select
                o.id::text as order_id,
                o.workspace_id::text as workspace_id,
                o.order_number,
                o.order_type,
                o.source,
                o.status,
                c.name as customer_name,
                c.email as customer_email,
                c.phone_number as customer_phone_number,
                o.total,
                o.currency
            from orders o
            left join customers c on c.id = o.customer_id
            where o.id = ?::uuid
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, orderId)
            statement.executeQuery().use { result ->
                if (!result.next()) return null
                result.toOrderNotificationContext()
            }
        }
    }

    private fun Connection.activeOneSignalExternalUserIds(workspaceId: String, channel: String): List<String> {
        val channelColumn = channel.notificationPreferenceColumn()
        val sql = """
            select distinct au.firebase_uid as external_user_id
            from notification_device_tokens ndt
            join app_users au on au.id = ndt.user_id
            join workspace_members wm
              on wm.user_id = ndt.user_id
              and wm.workspace_id = ndt.workspace_id
             and wm.status = 'active'
            where ndt.workspace_id = ?::uuid
              and ndt.enabled = true
              and au.notifications_enabled = true
              and au.$channelColumn = true
              and lower(ndt.platform) in ('android', 'web')
              and coalesce(au.firebase_uid, '') <> ''
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        result.getString("external_user_id")?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }
        }
    }

    private fun Connection.activeApnsDeviceTokens(workspaceId: String, channel: String): List<String> {
        val channelColumn = channel.notificationPreferenceColumn()
        val sql = """
            select distinct regexp_replace(ndt.token, '^apns:', '') as device_token
            from notification_device_tokens ndt
            join app_users au on au.id = ndt.user_id
            join workspace_members wm
              on wm.user_id = ndt.user_id
             and wm.workspace_id = ndt.workspace_id
             and wm.status = 'active'
            where ndt.workspace_id = ?::uuid
              and ndt.enabled = true
              and au.notifications_enabled = true
              and au.$channelColumn = true
              and lower(ndt.platform) = 'ios'
              and ndt.token like 'apns:%'
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        result.getString("device_token")?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }
        }
    }

    private fun Connection.activePublicCatalogCustomerOneSignalExternalUserIds(
        context: OrderNotificationContext,
        kind: OrderNotificationEventKind,
    ): List<String> {
        if (!kind.notifiesPublicCatalogCustomer || context.source != "public_catalog") return emptyList()
        val customerEmail = context.customerEmail.orEmpty().lowercase()
        val customerPhone = context.customerPhoneNumber.orEmpty()
        if (customerEmail.isBlank() && customerPhone.isBlank()) return emptyList()
        val sql = """
            select distinct regexp_replace(token, '^onesignal-external:', '') as external_user_id
            from public_catalog_notification_device_tokens
            where workspace_id = ?::uuid
              and enabled = true
              and lower(platform) in ('android', 'web')
              and token like 'onesignal-external:%'
              and (
                (? <> '' and lower(coalesce(email, '')) = ?)
                or (? <> '' and coalesce(phone_number, '') = ?)
              )
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, context.workspaceId)
            statement.setString(2, customerEmail)
            statement.setString(3, customerEmail)
            statement.setString(4, customerPhone)
            statement.setString(5, customerPhone)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        result.getString("external_user_id")?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }
        }
    }

    private fun Connection.activePublicCatalogCustomerApnsDeviceTokens(
        context: OrderNotificationContext,
        kind: OrderNotificationEventKind,
    ): List<String> {
        if (!kind.notifiesPublicCatalogCustomer || context.source != "public_catalog") return emptyList()
        val customerEmail = context.customerEmail.orEmpty().lowercase()
        val customerPhone = context.customerPhoneNumber.orEmpty()
        if (customerEmail.isBlank() && customerPhone.isBlank()) return emptyList()
        val sql = """
            select distinct regexp_replace(token, '^apns:', '') as device_token
            from public_catalog_notification_device_tokens
            where workspace_id = ?::uuid
              and enabled = true
              and lower(platform) = 'ios'
              and token like 'apns:%'
              and (
                (? <> '' and lower(coalesce(email, '')) = ?)
                or (? <> '' and coalesce(phone_number, '') = ?)
              )
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, context.workspaceId)
            statement.setString(2, customerEmail)
            statement.setString(3, customerEmail)
            statement.setString(4, customerPhone)
            statement.setString(5, customerPhone)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        result.getString("device_token")?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }
        }
    }

    private fun Connection.insertNotificationEvent(
        context: OrderNotificationContext,
        eventType: String,
        title: String,
        body: String,
        payload: Map<String, String>,
        targetCount: Int,
        status: String,
    ): String {
        val sql = """
            insert into notification_events (
                workspace_id, order_id, event_type, title, body, payload,
                status, target_count, updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?::jsonb, ?, ?, now())
            returning id::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, context.workspaceId)
            statement.setString(2, context.orderId)
            statement.setString(3, eventType)
            statement.setString(4, title)
            statement.setString(5, body)
            statement.setString(6, payload.toJsonObjectString())
            statement.setString(7, status)
            statement.setInt(8, targetCount)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
    }

    private fun Connection.updateNotificationEventDelivery(
        eventId: String,
        status: String,
        successCount: Int,
        failureCount: Int,
    ) {
        prepareStatement(
            """
            update notification_events
            set
                status = ?,
                success_count = ?,
                failure_count = ?,
                updated_at = now()
            where id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, status)
            statement.setInt(2, successCount)
            statement.setInt(3, failureCount)
            statement.setString(4, eventId)
            statement.executeUpdate()
        }
    }

    private fun ResultSet.toOrderNotificationContext(): OrderNotificationContext =
        OrderNotificationContext(
            orderId = getString("order_id"),
            workspaceId = getString("workspace_id"),
            orderNumber = getString("order_number"),
            orderType = getString("order_type"),
            source = getString("source"),
            status = getString("status"),
            customerName = getString("customer_name"),
            customerEmail = getString("customer_email"),
            customerPhoneNumber = getString("customer_phone_number"),
            total = getBigDecimal("total").setScale(2).toPlainString(),
            currency = getString("currency"),
        )

    private enum class OrderNotificationEventKind(
        val eventType: String,
        val channel: String,
        val notifiesPublicCatalogCustomer: Boolean = false,
    ) {
        Created("order_created", "catalog_orders") {
            override fun title(context: OrderNotificationContext): String {
                val workLabel = orderWorkLabel(context.orderType)
                return if (context.source == "public_catalog") {
                    "New catalog $workLabel ${context.orderNumber}"
                } else {
                    "New $workLabel ${context.orderNumber}"
                }
            }

            override fun body(context: OrderNotificationContext): String = buildString {
                append(context.customerName?.takeIf { it.isNotBlank() } ?: "A customer")
                append(" placed a ")
                append(orderWorkLabel(context.orderType))
                append(" for ")
                append("${context.currency} ${context.total}")
            }
        },
        Updated("order_updated", "status_updates", notifiesPublicCatalogCustomer = true) {
            override fun title(context: OrderNotificationContext): String =
                "${orderWorkLabel(context.orderType).replaceFirstChar { it.uppercase() }} ${context.orderNumber} updated"

            override fun body(context: OrderNotificationContext): String =
                "${context.customerName?.takeIf { it.isNotBlank() } ?: "Customer"}'s ${orderWorkLabel(context.orderType)} details were updated."

            override fun customerTitle(context: OrderNotificationContext): String =
                "Your ${orderWorkLabel(context.orderType)} was updated"

            override fun customerBody(context: OrderNotificationContext): String =
                "The business updated ${context.orderNumber}. Open details to review the latest items and payment."
        },
        ChangeRequested("order_change_requested", "catalog_orders") {
            override fun title(context: OrderNotificationContext): String =
                "Change requested for ${context.orderNumber}"

            override fun body(context: OrderNotificationContext): String =
                "${context.customerName?.takeIf { it.isNotBlank() } ?: "Customer"} requested an item or payment change."
        },
        ChangeApproved("order_change_approved", "status_updates", notifiesPublicCatalogCustomer = true) {
            override fun title(context: OrderNotificationContext): String =
                "Change approved for ${context.orderNumber}"

            override fun body(context: OrderNotificationContext): String =
                "${context.orderNumber} customer change request was approved."

            override fun customerTitle(context: OrderNotificationContext): String =
                "Your change request was approved"

            override fun customerBody(context: OrderNotificationContext): String =
                "The business updated ${context.orderNumber}. Open details to check the latest total and payment."
        },
        ChangeRejected("order_change_rejected", "status_updates", notifiesPublicCatalogCustomer = true) {
            override fun title(context: OrderNotificationContext): String =
                "Change rejected for ${context.orderNumber}"

            override fun body(context: OrderNotificationContext): String =
                "${context.orderNumber} customer change request was rejected."

            override fun customerTitle(context: OrderNotificationContext): String =
                "Your change request was not accepted"

            override fun customerBody(context: OrderNotificationContext): String =
                "The business did not apply the requested change for ${context.orderNumber}."
        },
        StatusUpdated("order_status_updated", "status_updates", notifiesPublicCatalogCustomer = true) {
            override fun title(context: OrderNotificationContext): String =
                "${orderWorkLabel(context.orderType).replaceFirstChar { it.uppercase() }} ${context.orderNumber} ${orderStatusLabel(context.status)}"

            override fun body(context: OrderNotificationContext): String = buildString {
                append(context.customerName?.takeIf { it.isNotBlank() } ?: "Customer")
                append("'s ")
                append(orderWorkLabel(context.orderType))
                append(" is now ")
                append(orderStatusLabel(context.status).lowercase())
                append(".")
            }

            override fun customerTitle(context: OrderNotificationContext): String =
                "${orderWorkLabel(context.orderType).replaceFirstChar { it.uppercase() }} ${context.orderNumber} ${orderStatusLabel(context.status)}"

            override fun customerBody(context: OrderNotificationContext): String =
                "Your ${orderWorkLabel(context.orderType)} is now ${orderStatusLabel(context.status).lowercase()}."
        };

        abstract fun title(context: OrderNotificationContext): String
        abstract fun body(context: OrderNotificationContext): String

        open fun customerTitle(context: OrderNotificationContext): String = title(context)

        open fun customerBody(context: OrderNotificationContext): String = body(context)

        protected fun orderWorkLabel(orderType: String): String =
            when (orderType.trim().lowercase()) {
                "appointment" -> "appointment"
                "service" -> "service request"
                else -> "order"
            }

        protected fun orderStatusLabel(status: String): String =
            status.trim()
                .replace('_', ' ')
                .lowercase()
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
                .ifBlank { "Updated" }
    }

    private fun String.notificationPreferenceColumn(): String =
        when (trim().lowercase()) {
            "status_updates" -> "notification_status_updates_enabled"
            "billing" -> "notification_billing_enabled"
            "stock" -> "notification_stock_enabled"
            "team" -> "notification_team_enabled"
            "marketing" -> "notification_marketing_enabled"
            else -> "notification_catalog_orders_enabled"
        }

    private fun Map<String, String>.toJsonObjectString(): String =
        entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${key.jsonEscaped()}\":\"${value.jsonEscaped()}\""
        }

    private fun String.base64Url(): String =
        toByteArray(Charsets.UTF_8).base64Url()

    private fun ByteArray.base64Url(): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(this)

    private fun ByteArray.derEcdsaSignatureToJose(partLength: Int = 32): ByteArray {
        var offset = 0
        require(this[offset++].toInt() == 0x30) { "APNs ECDSA signature is not a DER sequence." }
        offset = skipDerLength(offset)
        require(this[offset++].toInt() == 0x02) { "APNs ECDSA signature is missing r." }
        val rLength = readDerLength(offset)
        offset = rLength.nextOffset
        val r = copyOfRange(offset, offset + rLength.length).toJoseInteger(partLength)
        offset += rLength.length
        require(this[offset++].toInt() == 0x02) { "APNs ECDSA signature is missing s." }
        val sLength = readDerLength(offset)
        offset = sLength.nextOffset
        val s = copyOfRange(offset, offset + sLength.length).toJoseInteger(partLength)
        return r + s
    }

    private fun ByteArray.skipDerLength(offset: Int): Int =
        readDerLength(offset).nextOffset

    private fun ByteArray.readDerLength(offset: Int): DerLength {
        val first = this[offset].toInt() and 0xff
        if (first < 0x80) return DerLength(length = first, nextOffset = offset + 1)
        val byteCount = first and 0x7f
        var length = 0
        repeat(byteCount) { index ->
            length = (length shl 8) or (this[offset + 1 + index].toInt() and 0xff)
        }
        return DerLength(length = length, nextOffset = offset + 1 + byteCount)
    }

    private fun ByteArray.toJoseInteger(partLength: Int): ByteArray {
        val unsigned = dropWhile { it == 0.toByte() }.toByteArray()
        return when {
            unsigned.size == partLength -> unsigned
            unsigned.size > partLength -> unsigned.copyOfRange(unsigned.size - partLength, unsigned.size)
            else -> ByteArray(partLength - unsigned.size) + unsigned
        }
    }

    private data class DerLength(
        val length: Int,
        val nextOffset: Int,
    )

    private fun String.jsonEscaped(): String =
        buildString {
            this@jsonEscaped.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }

    private data class OrderNotificationContext(
        val orderId: String,
        val workspaceId: String,
        val orderNumber: String,
        val orderType: String,
        val source: String,
        val status: String,
        val customerName: String?,
        val customerEmail: String?,
        val customerPhoneNumber: String?,
        val total: String,
        val currency: String,
    )

    private companion object {
        val logger = LoggerFactory.getLogger(OrderNotificationService::class.java)
    }
}
