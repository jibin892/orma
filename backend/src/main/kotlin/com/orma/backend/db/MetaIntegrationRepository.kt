package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.MetaCatalogSyncResponse
import com.orma.backend.models.MetaConnectionRequest
import com.orma.backend.models.MetaConnectionStatusResponse
import com.orma.backend.models.MetaProductReadinessResponse
import com.orma.backend.models.MetaWebhookEventResponse
import java.math.BigDecimal
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetaIntegrationRepository(
    private val dataSource: DataSource,
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
                val productReadiness = connection.listMetaCatalogProducts(access.workspaceId).map { product ->
                    val issues = product.readinessIssues(connected)
                    val status = when {
                        issues.isNotEmpty() -> "blocked"
                        connected -> "ready"
                        else -> "not_connected"
                    }
                    connection.upsertMetaProductReadiness(
                        workspaceId = access.workspaceId,
                        product = product,
                        status = status,
                        issues = issues,
                    )
                }
                connection.touchMetaSync(access.workspaceId, connected)
                connection.commit()
                MetaCatalogSyncResponse(
                    connected = connected,
                    productsReady = productReadiness.count { it.ready },
                    productsBlocked = productReadiness.count { !it.ready },
                    productsSynced = productReadiness.count { it.status == "synced" },
                    productReadiness = productReadiness,
                    message = if (connected) {
                        "Catalog readiness checked."
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
            connection.prepareStatement(
                """
                insert into meta_webhook_events (object_type, event_type, payload)
                values (
                    nullif(?,''),
                    nullif(?,''),
                    ?::jsonb
                )
                returning id::text, status
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, payload.extractJsonString("object").orEmpty())
                statement.setString(2, payload.extractWebhookEventType().orEmpty())
                statement.setString(3, payload.ifBlank { "{}" })
                statement.executeQuery().use { result ->
                    result.next()
                    MetaWebhookEventResponse(
                        id = result.getString("id"),
                        status = result.getString("status"),
                    )
                }
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
            businessId = connection?.businessId,
            whatsappBusinessAccountId = connection?.whatsappBusinessAccountId,
            phoneNumberId = connection?.phoneNumberId,
            catalogId = connection?.catalogId,
            pageId = connection?.pageId,
            instagramBusinessAccountId = connection?.instagramBusinessAccountId,
            scopes = connection?.scopes.orEmpty(),
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
                business_id,
                whatsapp_business_account_id,
                phone_number_id,
                catalog_id,
                page_id,
                instagram_business_account_id,
                scopes,
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
                business_id,
                whatsapp_business_account_id,
                phone_number_id,
                catalog_id,
                page_id,
                instagram_business_account_id,
                scopes,
                updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, now())
            on conflict (workspace_id) do update
            set status = excluded.status,
                business_id = excluded.business_id,
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
            statement.setStringOrNull(3, request.businessId)
            statement.setStringOrNull(4, request.whatsappBusinessAccountId)
            statement.setStringOrNull(5, request.phoneNumberId)
            statement.setStringOrNull(6, request.catalogId)
            statement.setStringOrNull(7, request.pageId)
            statement.setStringOrNull(8, request.instagramBusinessAccountId)
            statement.setArray(9, createArrayOf("text", request.scopes.map { it.trim() }.filter { it.isNotBlank() }.toTypedArray()))
            statement.executeUpdate()
        }
    }

    private fun Connection.listMetaCatalogProducts(workspaceId: String): List<MetaCatalogProductRow> {
        val sql = """
            select
                p.id::text as product_id,
                p.name,
                p.status,
                p.selling_price,
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
    ): MetaProductReadinessResponse {
        val sql = """
            insert into meta_product_sync (
                workspace_id,
                product_id,
                status,
                readiness_issues,
                last_sync_at,
                updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, now(), now())
            on conflict (workspace_id, product_id) do update
            set status = excluded.status,
                readiness_issues = excluded.readiness_issues,
                last_sync_at = now(),
                last_error = null,
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
            statement.setString(5, product.name)
            statement.executeQuery().use { result ->
                result.next()
                result.toMetaProductReadinessResponse()
            }
        }
    }

    private fun Connection.touchMetaSync(workspaceId: String, connected: Boolean) {
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
            statement.setString(3, if (connected) null else "Meta account is not connected yet.")
            statement.executeUpdate()
        }
    }

    private fun ResultSet.toMetaConnectionRow(): MetaConnectionRow =
        MetaConnectionRow(
            status = getString("status") ?: "not_connected",
            businessId = getString("business_id"),
            whatsappBusinessAccountId = getString("whatsapp_business_account_id"),
            phoneNumberId = getString("phone_number_id"),
            catalogId = getString("catalog_id"),
            pageId = getString("page_id"),
            instagramBusinessAccountId = getString("instagram_business_account_id"),
            scopes = (getArray("scopes")?.array as? Array<*>)?.mapNotNull { it as? String }.orEmpty(),
            lastSyncAt = getString("last_sync_at"),
            lastError = getString("last_error"),
        )

    private fun ResultSet.toMetaCatalogProductRow(): MetaCatalogProductRow =
        MetaCatalogProductRow(
            productId = getString("product_id"),
            name = getString("name"),
            status = getString("status"),
            sellingPrice = getBigDecimal("selling_price") ?: BigDecimal.ZERO,
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
            ready = issues.isEmpty() && status != "not_connected" && status != "blocked",
            status = status,
            issues = issues,
            metaProductId = getString("meta_product_id"),
            lastSyncAt = getString("last_sync_at"),
        )
    }
}

private data class MetaConnectionRow(
    val status: String,
    val businessId: String?,
    val whatsappBusinessAccountId: String?,
    val phoneNumberId: String?,
    val catalogId: String?,
    val pageId: String?,
    val instagramBusinessAccountId: String?,
    val scopes: List<String>,
    val lastSyncAt: String?,
    val lastError: String?,
) {
    val isConnected: Boolean
        get() = status == "connected" && (
            !whatsappBusinessAccountId.isNullOrBlank() ||
                !catalogId.isNullOrBlank() ||
                !pageId.isNullOrBlank() ||
                !instagramBusinessAccountId.isNullOrBlank()
            )
}

private data class MetaCatalogProductRow(
    val productId: String,
    val name: String,
    val status: String,
    val sellingPrice: BigDecimal,
    val trackStock: Boolean,
    val stockQuantity: BigDecimal,
    val reorderLevel: BigDecimal,
    val imageStoragePath: String?,
)

private fun MetaCatalogProductRow.readinessIssues(connected: Boolean): List<String> = buildList {
    if (!connected) add("Connect Meta before syncing this item.")
    if (status != "active") add("Set product status active.")
    if (sellingPrice <= BigDecimal.ZERO) add("Add a selling price.")
    if (imageStoragePath.isNullOrBlank()) add("Add a product image.")
    if (trackStock && stockQuantity <= BigDecimal.ZERO) add("Restock before promoting this item.")
    if (trackStock && stockQuantity <= reorderLevel) add("Stock is at or below the reorder level.")
}

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
        .takeIf { it in setOf("connected", "not_connected", "needs_review", "disabled") }
        ?: "connected"

private fun String.extractJsonString(key: String): String? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    return pattern.find(this)?.groupValues?.get(1)
}

private fun String.extractWebhookEventType(): String? {
    val field = Regex("\"field\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(this)?.groupValues?.get(1)
    val type = Regex("\"type\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(this)?.groupValues?.get(1)
    return field ?: type
}
