package com.orma.backend.notifications

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.orma.backend.auth.FirebaseAppProvider
import com.orma.backend.config.AppConfig
import com.orma.backend.models.OrderResponse
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class OrderNotificationService(
    private val dataSource: DataSource,
    private val config: AppConfig,
) {
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
        val deliveryPayload = payload + ("sound" to "default")
        val tokens = dataSource.connection.use { connection ->
            connection.activeNotificationTokens(context.workspaceId)
        }

        val eventId = dataSource.connection.use { connection ->
            connection.insertNotificationEvent(
                context = context,
                title = title,
                body = body,
                payload = payload,
                targetCount = tokens.size,
                status = when {
                    tokens.isEmpty() -> "no_targets"
                    !config.firebaseMessagingConfigured -> "not_configured"
                    else -> "queued"
                },
            )
        }

        if (tokens.isEmpty() || !config.firebaseMessagingConfigured) return@withContext

        var successCount = 0
        var failureCount = 0
        val messaging = FirebaseMessaging.getInstance(FirebaseAppProvider.app(config))
        tokens.forEach { token ->
            val message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build(),
                )
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(
                            AndroidNotification.builder()
                                .setChannelId("orma_workspace_alerts")
                                .setSound("default")
                                .build(),
                        )
                        .build(),
                )
                .setApnsConfig(
                    ApnsConfig.builder()
                        .setAps(
                            Aps.builder()
                                .setSound("default")
                                .build(),
                        )
                        .build(),
                )
                .putAllData(deliveryPayload)
                .build()
            runCatching {
                messaging.send(message)
            }.onSuccess {
                successCount += 1
            }.onFailure { error ->
                failureCount += 1
                logger.warn("FCM token send failed for order ${context.orderNumber}: ${error.message}")
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

    private fun Connection.activeNotificationTokens(workspaceId: String): List<String> {
        val sql = """
            select distinct ndt.token
            from notification_device_tokens ndt
            join app_users au on au.id = ndt.user_id
            join workspace_members wm
              on wm.user_id = ndt.user_id
             and wm.workspace_id = ndt.workspace_id
             and wm.status = 'active'
            where ndt.workspace_id = ?::uuid
              and ndt.enabled = true
              and lower(ndt.platform) in ('android', 'web', 'ios')
              and au.notifications_enabled = true
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        result.getString("token")?.takeIf { it.isNotBlank() }?.let(::add)
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
