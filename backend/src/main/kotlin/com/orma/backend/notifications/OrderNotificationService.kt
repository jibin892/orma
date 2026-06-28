package com.orma.backend.notifications

import com.orma.backend.config.AppConfig
import com.orma.backend.models.OrderResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.Connection
import java.sql.ResultSet
import java.time.Duration
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
            notifyOrderCreatedInternal(order)
        }.onFailure { error ->
            logger.warn("Order notification failed for order ${order.id}: ${error.message}", error)
        }
    }

    private suspend fun notifyOrderCreatedInternal(order: OrderResponse) = withContext(Dispatchers.IO) {
        val context = dataSource.connection.use { connection ->
            connection.orderNotificationContext(order.id)
        } ?: return@withContext

        val workLabel = context.orderType.orderWorkLabel()
        val title = if (context.source == "public_catalog") {
            "New catalog $workLabel ${context.orderNumber}"
        } else {
            "New $workLabel ${context.orderNumber}"
        }
        val body = buildString {
            append(context.customerName?.takeIf { it.isNotBlank() } ?: "A customer")
            append(" placed a ")
            append(workLabel)
            append(" for ")
            append("${context.currency} ${context.total}")
        }
        val payload = mapOf(
            "type" to "order_created",
            "orderId" to context.orderId,
            "orderNumber" to context.orderNumber,
            "orderType" to context.orderType,
            "workspaceId" to context.workspaceId,
        )
        val externalUserIds = dataSource.connection.use { connection ->
            connection.activeOneSignalExternalUserIds(context.workspaceId)
        }

        val eventId = dataSource.connection.use { connection ->
            connection.insertNotificationEvent(
                context = context,
                title = title,
                body = body,
                payload = payload,
                targetCount = externalUserIds.size,
                status = when {
                    externalUserIds.isEmpty() -> "no_targets"
                    !config.oneSignalPushConfigured -> "not_configured"
                    else -> "queued"
                },
            )
        }

        if (externalUserIds.isEmpty() || !config.oneSignalPushConfigured) return@withContext

        val sent = runCatching {
            sendOneSignalNotification(
                externalUserIds = externalUserIds,
                title = title,
                body = body,
                payload = payload,
            )
        }.onFailure { error ->
            logger.warn("OneSignal send failed for order ${context.orderNumber}: ${error.message}")
        }.isSuccess

        val successCount = if (sent) externalUserIds.size else 0
        val failureCount = if (sent) 0 else externalUserIds.size
        dataSource.connection.use { connection ->
            connection.updateNotificationEventDelivery(
                eventId = eventId,
                status = if (sent) "sent" else "failed",
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
            put("android_channel_id", "orma_workspace_alerts")
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

    private fun Connection.orderNotificationContext(orderId: String): OrderNotificationContext? {
        val sql = """
            select
                o.id::text as order_id,
                o.workspace_id::text as workspace_id,
                o.order_number,
                o.order_type,
                o.source,
                c.name as customer_name,
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

    private fun Connection.activeOneSignalExternalUserIds(workspaceId: String): List<String> {
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
              and lower(ndt.platform) in ('android', 'web', 'ios')
              and coalesce(au.firebase_uid, '') <> ''
              and au.notifications_enabled = true
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

    private fun Connection.insertNotificationEvent(
        context: OrderNotificationContext,
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
            statement.setString(3, "order_created")
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
            customerName = getString("customer_name"),
            total = getBigDecimal("total").setScale(2).toPlainString(),
            currency = getString("currency"),
        )

    private fun String.orderWorkLabel(): String =
        when (trim().lowercase()) {
            "appointment" -> "appointment"
            "service" -> "service request"
            else -> "order"
        }

    private fun Map<String, String>.toJsonObjectString(): String =
        entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${key.jsonEscaped()}\":\"${value.jsonEscaped()}\""
        }

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
        val customerName: String?,
        val total: String,
        val currency: String,
    )

    private companion object {
        val logger = LoggerFactory.getLogger(OrderNotificationService::class.java)
    }
}
