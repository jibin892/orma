package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.CustomerResponse
import com.orma.backend.models.DashboardSummaryResponse
import com.orma.backend.models.OrderItemRequest
import com.orma.backend.models.OrderItemResponse
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.OrderResponse
import com.orma.backend.models.ProductRequest
import com.orma.backend.models.ProductResponse
import com.orma.backend.models.StockAdjustmentRequest
import com.orma.backend.models.StockMovementResponse
import com.orma.backend.models.SupplierRequest
import com.orma.backend.models.SupplierResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DashboardWorkspaceAccess(
    val userId: String,
    val workspaceId: String,
    val role: String,
    val currency: String,
)

class DashboardRepository(
    private val dataSource: DataSource,
) {
    suspend fun summary(firebaseUser: VerifiedFirebaseUser): DashboardSummaryResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            DashboardSummaryResponse(
                currency = access.currency,
                totalCustomers = connection.scalarInt(
                    "select count(*) from customers where workspace_id = ?::uuid and status = 'active'",
                    access.workspaceId,
                ),
                totalPaidAmount = connection.scalarDecimal(
                    """
                    select coalesce(sum(paid_total), 0)
                    from orders
                    where workspace_id = ?::uuid and status <> 'cancelled'
                    """.trimIndent(),
                    access.workspaceId,
                ).moneyString(),
                ordersCount = connection.scalarInt(
                    "select count(*) from orders where workspace_id = ?::uuid and status <> 'cancelled'",
                    access.workspaceId,
                ),
                bookingsCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders
                    where workspace_id = ?::uuid
                      and status <> 'cancelled'
                      and scheduled_at is not null
                    """.trimIndent(),
                    access.workspaceId,
                ),
                productsInStock = connection.scalarInt(
                    """
                    select count(*)
                    from products
                    where workspace_id = ?::uuid
                      and status = 'active'
                      and (track_stock = false or stock_quantity > 0)
                    """.trimIndent(),
                    access.workspaceId,
                ),
                lowStockProducts = connection.scalarInt(
                    """
                    select count(*)
                    from products
                    where workspace_id = ?::uuid
                      and status = 'active'
                      and track_stock = true
                      and stock_quantity <= reorder_level
                    """.trimIndent(),
                    access.workspaceId,
                ),
                recentOrders = connection.listOrders(access.workspaceId, limit = 5, includeItems = false),
                lowStockItems = connection.listProducts(
                    workspaceId = access.workspaceId,
                    limit = 5,
                    lowStockOnly = true,
                ),
            )
        }
    }

    suspend fun customers(firebaseUser: VerifiedFirebaseUser): List<CustomerResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listCustomers(access.workspaceId)
        }
    }

    suspend fun createCustomer(
        firebaseUser: VerifiedFirebaseUser,
        request: CustomerRequest,
    ): CustomerResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.insertCustomer(access.workspaceId, request)
        }
    }

    suspend fun updateCustomer(
        firebaseUser: VerifiedFirebaseUser,
        customerId: String,
        request: CustomerRequest,
    ): CustomerResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.updateCustomer(access.workspaceId, customerId, request)
        }
    }

    suspend fun suppliers(firebaseUser: VerifiedFirebaseUser): List<SupplierResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listSuppliers(access.workspaceId)
        }
    }

    suspend fun createSupplier(
        firebaseUser: VerifiedFirebaseUser,
        request: SupplierRequest,
    ): SupplierResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.insertSupplier(access.workspaceId, request)
        }
    }

    suspend fun updateSupplier(
        firebaseUser: VerifiedFirebaseUser,
        supplierId: String,
        request: SupplierRequest,
    ): SupplierResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.updateSupplier(access.workspaceId, supplierId, request)
        }
    }

    suspend fun products(firebaseUser: VerifiedFirebaseUser): List<ProductResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listProducts(access.workspaceId)
        }
    }

    suspend fun createProduct(
        firebaseUser: VerifiedFirebaseUser,
        request: ProductRequest,
    ): ProductResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val product = connection.insertProduct(access, request)
                if (request.trackStock && request.stockQuantity.decimalOrZero() != BigDecimal.ZERO) {
                    connection.insertStockMovement(
                        access = access,
                        productId = product.id,
                        movementType = "opening",
                        quantityDelta = request.stockQuantity.decimalOrZero(),
                        balanceAfter = request.stockQuantity.decimalOrZero(),
                        note = "Opening stock",
                    )
                }
                connection.commit()
                product
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateProduct(
        firebaseUser: VerifiedFirebaseUser,
        productId: String,
        request: ProductRequest,
    ): ProductResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.updateProduct(access, productId, request)
        }
    }

    suspend fun adjustStock(
        firebaseUser: VerifiedFirebaseUser,
        productId: String,
        request: StockAdjustmentRequest,
    ): ProductResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val product = connection.adjustProductStock(access, productId, request)
                connection.commit()
                product
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun orders(firebaseUser: VerifiedFirebaseUser): List<OrderResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listOrders(access.workspaceId, includeItems = false)
        }
    }

    suspend fun order(
        firebaseUser: VerifiedFirebaseUser,
        orderId: String,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.getOrder(access.workspaceId, orderId, includeItems = true)
        }
    }

    suspend fun createOrder(
        firebaseUser: VerifiedFirebaseUser,
        request: OrderRequest,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val order = connection.insertOrder(access, request)
                connection.commit()
                order
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateOrderStatus(
        firebaseUser: VerifiedFirebaseUser,
        orderId: String,
        status: String,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val updated = connection.updateOrderStatus(access, orderId, status)
                connection.commit()
                updated
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun Connection.resolveWorkspaceAccess(firebaseUser: VerifiedFirebaseUser): DashboardWorkspaceAccess? {
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

    private fun Connection.scalarInt(sql: String, workspaceId: String): Int =
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                result.getInt(1)
            }
        }

    private fun Connection.scalarDecimal(sql: String, workspaceId: String): BigDecimal =
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                result.getBigDecimal(1) ?: BigDecimal.ZERO
            }
        }

    private fun Connection.listCustomers(workspaceId: String): List<CustomerResponse> {
        val sql = """
            select *
            from customers
            where workspace_id = ?::uuid and status = 'active'
            order by created_at desc
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toCustomerResponse())
                }
            }
        }
    }

    private fun Connection.insertCustomer(workspaceId: String, request: CustomerRequest): CustomerResponse {
        val sql = """
            insert into customers (
                workspace_id, name, phone_number, email, address_line, city, region,
                country, postal_code, notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.bindCustomerRequest(request, startIndex = 2)
            statement.executeQuery().use { result ->
                result.next()
                result.toCustomerResponse()
            }
        }
    }

    private fun Connection.updateCustomer(
        workspaceId: String,
        customerId: String,
        request: CustomerRequest,
    ): CustomerResponse? {
        val sql = """
            update customers
            set name = ?, phone_number = ?, email = ?, address_line = ?, city = ?,
                region = ?, country = ?, postal_code = ?, notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindCustomerRequest(request, startIndex = 1)
            statement.setString(10, customerId)
            statement.setString(11, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toCustomerResponse() else null
            }
        }
    }

    private fun Connection.listSuppliers(workspaceId: String): List<SupplierResponse> {
        val sql = """
            select *
            from suppliers
            where workspace_id = ?::uuid and status = 'active'
            order by created_at desc
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toSupplierResponse())
                }
            }
        }
    }

    private fun Connection.insertSupplier(workspaceId: String, request: SupplierRequest): SupplierResponse {
        val sql = """
            insert into suppliers (
                workspace_id, name, phone_number, email, tax_number, address_line, notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, now())
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.bindSupplierRequest(request, startIndex = 2)
            statement.executeQuery().use { result ->
                result.next()
                result.toSupplierResponse()
            }
        }
    }

    private fun Connection.updateSupplier(
        workspaceId: String,
        supplierId: String,
        request: SupplierRequest,
    ): SupplierResponse? {
        val sql = """
            update suppliers
            set name = ?, phone_number = ?, email = ?, tax_number = ?,
                address_line = ?, notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindSupplierRequest(request, startIndex = 1)
            statement.setString(7, supplierId)
            statement.setString(8, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toSupplierResponse() else null
            }
        }
    }

    private fun Connection.listProducts(
        workspaceId: String,
        limit: Int? = null,
        lowStockOnly: Boolean = false,
    ): List<ProductResponse> {
        val sql = buildString {
            append(
                """
                select p.*, s.name as supplier_name
                from products p
                left join suppliers s on s.id = p.supplier_id
                where p.workspace_id = ?::uuid and p.status = 'active'
                """.trimIndent(),
            )
            if (lowStockOnly) {
                append(" and p.track_stock = true and p.stock_quantity <= p.reorder_level")
            }
            append(" order by p.created_at desc")
            if (limit != null) append(" limit $limit")
        }
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toProductResponse())
                }
            }
        }
    }

    private fun Connection.insertProduct(
        access: DashboardWorkspaceAccess,
        request: ProductRequest,
    ): ProductResponse {
        val sql = """
            insert into products (
                workspace_id, supplier_id, name, sku, barcode, description, unit,
                selling_price, cost_price, currency, tax_rate, prices_include_tax,
                stock_quantity, reorder_level, track_stock, updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            returning *, null::text as supplier_name
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.bindProductRequest(access, request, startIndex = 2)
            statement.executeQuery().use { result ->
                result.next()
                result.toProductResponse()
            }
        }
    }

    private fun Connection.updateProduct(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: ProductRequest,
    ): ProductResponse? {
        val sql = """
            update products
            set supplier_id = ?::uuid, name = ?, sku = ?, barcode = ?, description = ?,
                unit = ?, selling_price = ?, cost_price = ?, currency = ?, tax_rate = ?,
                prices_include_tax = ?, reorder_level = ?, track_stock = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *, null::text as supplier_name
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setNullableUuid(1, request.supplierId)
            statement.setString(2, request.name.cleanName())
            statement.setNullableString(3, request.sku?.cleanOptionalUpper())
            statement.setNullableString(4, request.barcode?.cleanOptional())
            statement.setNullableString(5, request.description?.cleanOptional())
            statement.setString(6, request.unit.cleanOptional() ?: "pcs")
            statement.setBigDecimal(7, request.sellingPrice.moneyOrZero())
            statement.setBigDecimal(8, request.costPrice.moneyOrZero())
            statement.setString(9, request.currency?.cleanOptionalUpper() ?: access.currency)
            statement.setBigDecimal(10, request.taxRate.decimalOrZero())
            statement.setBoolean(11, request.pricesIncludeTax)
            statement.setBigDecimal(12, request.reorderLevel.decimalOrZero())
            statement.setBoolean(13, request.trackStock)
            statement.setString(14, productId)
            statement.setString(15, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toProductResponse() else null
            }
        }
    }

    private fun Connection.adjustProductStock(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: StockAdjustmentRequest,
    ): ProductResponse? {
        val quantityDelta = request.quantityDelta.decimalOrZero()
        val current = prepareStatement(
            """
            select stock_quantity
            from products
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            for update
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, productId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getBigDecimal("stock_quantity") else return null
            }
        }
        val next = current.add(quantityDelta).scaled()
        val updated = prepareStatement(
            """
            update products
            set stock_quantity = ?, track_stock = true, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *, null::text as supplier_name
            """.trimIndent(),
        ).use { statement ->
            statement.setBigDecimal(1, next)
            statement.setString(2, productId)
            statement.setString(3, access.workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                result.toProductResponse()
            }
        }
        insertStockMovement(
            access = access,
            productId = productId,
            movementType = "adjustment",
            quantityDelta = quantityDelta,
            balanceAfter = next,
            note = request.note?.cleanOptional(),
        )
        return updated
    }

    private fun Connection.listOrders(
        workspaceId: String,
        limit: Int? = null,
        includeItems: Boolean,
    ): List<OrderResponse> {
        val sql = buildString {
            append(orderSelectSql())
            append(" where o.workspace_id = ?::uuid")
            append(" group by o.id, c.name")
            append(" order by o.created_at desc")
            if (limit != null) append(" limit $limit")
        }
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val order = result.toOrderResponse(emptyList())
                        add(if (includeItems) order.copy(items = listOrderItems(order.id)) else order)
                    }
                }
            }
        }
    }

    private fun Connection.getOrder(
        workspaceId: String,
        orderId: String,
        includeItems: Boolean,
    ): OrderResponse? {
        val sql = orderSelectSql() + " where o.workspace_id = ?::uuid and o.id = ?::uuid group by o.id, c.name"
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    val order = result.toOrderResponse(emptyList())
                    if (includeItems) order.copy(items = listOrderItems(order.id)) else order
                }
            }
        }
    }

    private fun Connection.insertOrder(
        access: DashboardWorkspaceAccess,
        request: OrderRequest,
    ): OrderResponse {
        val status = request.status.normalizedOrderStatus()
        val currency = request.currency?.cleanOptionalUpper() ?: access.currency
        val customerId = request.customerId.cleanUuidOrNull()
            ?: request.customerName?.cleanOptional()?.let { name ->
                insertCustomer(
                    workspaceId = access.workspaceId,
                    request = CustomerRequest(name = name),
                ).id
            }
        val preparedItems = request.items.map { it.toPreparedOrderItem() }
        val subtotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineSubtotal) }.scaled()
        val taxTotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineTax) }.scaled()
        val total = preparedItems.fold(BigDecimal.ZERO) { sum, item -> sum.add(item.lineTotal) }.scaled()
        val paidTotal = request.paidTotal.moneyOrZero().coerceAtMost(total)
        val inventoryApplied = status.appliesInventory()
        val orderId: String
        val orderNumber = "ORD-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"

        val insertOrderSql = """
            insert into orders (
                workspace_id, customer_id, order_number, status, scheduled_at,
                subtotal, tax_total, discount_total, paid_total, total, currency,
                notes, inventory_applied, created_by_user_id, updated_at
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?::timestamptz,
                ?, ?, 0, ?, ?, ?, ?, ?, ?::uuid, now()
            )
            returning id::text
        """.trimIndent()
        orderId = prepareStatement(insertOrderSql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setNullableUuid(2, customerId)
            statement.setString(3, orderNumber)
            statement.setString(4, status)
            statement.setNullableString(5, request.scheduledAt?.cleanOptional())
            statement.setBigDecimal(6, subtotal)
            statement.setBigDecimal(7, taxTotal)
            statement.setBigDecimal(8, paidTotal)
            statement.setBigDecimal(9, total)
            statement.setString(10, currency)
            statement.setNullableString(11, request.notes?.cleanOptional())
            statement.setBoolean(12, inventoryApplied)
            statement.setString(13, access.userId)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }

        preparedItems.forEach { item ->
            insertOrderItem(orderId, item)
            if (inventoryApplied && item.productId != null) {
                applyOrderStockMovement(access, item.productId, item.quantity.negate(), "Order $orderNumber")
            }
        }

        return getOrder(access.workspaceId, orderId, includeItems = true) ?: error("Created order was not found.")
    }

    private fun Connection.updateOrderStatus(
        access: DashboardWorkspaceAccess,
        orderId: String,
        requestedStatus: String,
    ): OrderResponse? {
        val status = requestedStatus.normalizedOrderStatus()
        val currentSql = """
            select status, inventory_applied
            from orders
            where id = ?::uuid and workspace_id = ?::uuid
            for update
        """.trimIndent()
        val current = prepareStatement(currentSql).use { statement ->
            statement.setString(1, orderId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) return null
                result.getString("status") to result.getBoolean("inventory_applied")
            }
        }
        val shouldApplyInventory = status.appliesInventory() && !current.second
        prepareStatement(
            """
            update orders
            set status = ?, inventory_applied = inventory_applied or ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, status)
            statement.setBoolean(2, shouldApplyInventory)
            statement.setString(3, orderId)
            statement.setString(4, access.workspaceId)
            statement.executeUpdate()
        }
        if (shouldApplyInventory) {
            listOrderItems(orderId).forEach { item ->
                item.productId?.let { productId ->
                    applyOrderStockMovement(access, productId, item.quantity.decimalOrZero().negate(), "Order status: $status")
                }
            }
        }
        return getOrder(access.workspaceId, orderId, includeItems = true)
    }

    private fun Connection.insertOrderItem(orderId: String, item: PreparedOrderItem) {
        val sql = """
            insert into order_items (
                order_id, product_id, description, quantity, unit_price, tax_rate,
                line_subtotal, line_tax, line_total
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, orderId)
            statement.setNullableUuid(2, item.productId)
            statement.setString(3, item.description)
            statement.setBigDecimal(4, item.quantity)
            statement.setBigDecimal(5, item.unitPrice)
            statement.setBigDecimal(6, item.taxRate)
            statement.setBigDecimal(7, item.lineSubtotal)
            statement.setBigDecimal(8, item.lineTax)
            statement.setBigDecimal(9, item.lineTotal)
            statement.executeUpdate()
        }
    }

    private fun Connection.listOrderItems(orderId: String): List<OrderItemResponse> {
        val sql = """
            select
                oi.id::text,
                oi.product_id::text,
                p.name as product_name,
                oi.description,
                oi.quantity,
                oi.unit_price,
                oi.tax_rate,
                oi.line_subtotal,
                oi.line_tax,
                oi.line_total
            from order_items oi
            left join products p on p.id = oi.product_id
            where oi.order_id = ?::uuid
            order by oi.id
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, orderId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toOrderItemResponse())
                }
            }
        }
    }

    private fun Connection.applyOrderStockMovement(
        access: DashboardWorkspaceAccess,
        productId: String,
        quantityDelta: BigDecimal,
        note: String,
    ) {
        val current = prepareStatement(
            """
            select stock_quantity
            from products
            where id = ?::uuid and workspace_id = ?::uuid and track_stock = true and status = 'active'
            for update
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, productId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getBigDecimal("stock_quantity") else return
            }
        }
        val next = current.add(quantityDelta).scaled()
        prepareStatement(
            """
            update products
            set stock_quantity = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setBigDecimal(1, next)
            statement.setString(2, productId)
            statement.setString(3, access.workspaceId)
            statement.executeUpdate()
        }
        insertStockMovement(access, productId, "order", quantityDelta, next, note)
    }

    private fun Connection.insertStockMovement(
        access: DashboardWorkspaceAccess,
        productId: String,
        movementType: String,
        quantityDelta: BigDecimal,
        balanceAfter: BigDecimal,
        note: String?,
    ): StockMovementResponse {
        val sql = """
            insert into stock_movements (
                workspace_id, product_id, movement_type, quantity_delta,
                balance_after, note, created_by_user_id
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?::uuid)
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setString(2, productId)
            statement.setString(3, movementType)
            statement.setBigDecimal(4, quantityDelta.scaled())
            statement.setBigDecimal(5, balanceAfter.scaled())
            statement.setNullableString(6, note)
            statement.setString(7, access.userId)
            statement.executeQuery().use { result ->
                result.next()
                result.toStockMovementResponse()
            }
        }
    }

    private fun orderSelectSql(): String =
        """
        select
            o.id::text,
            o.order_number,
            o.customer_id::text,
            c.name as customer_name,
            o.status,
            o.scheduled_at::text,
            o.subtotal,
            o.tax_total,
            o.discount_total,
            o.paid_total,
            o.total,
            o.currency,
            o.notes,
            o.created_at::text,
            o.updated_at::text,
            count(oi.id)::int as item_count
        from orders o
        left join customers c on c.id = o.customer_id
        left join order_items oi on oi.order_id = o.id
        """.trimIndent()

    private fun PreparedStatement.bindCustomerRequest(request: CustomerRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setNullableString(startIndex + 1, request.phoneNumber?.cleanOptional())
        setNullableString(startIndex + 2, request.email?.cleanOptional()?.lowercase())
        setNullableString(startIndex + 3, request.addressLine?.cleanOptional())
        setNullableString(startIndex + 4, request.city?.cleanOptional())
        setNullableString(startIndex + 5, request.region?.cleanOptional())
        setNullableString(startIndex + 6, request.country?.cleanOptional())
        setNullableString(startIndex + 7, request.postalCode?.cleanOptional())
        setNullableString(startIndex + 8, request.notes?.cleanOptional())
    }

    private fun PreparedStatement.bindSupplierRequest(request: SupplierRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setNullableString(startIndex + 1, request.phoneNumber?.cleanOptional())
        setNullableString(startIndex + 2, request.email?.cleanOptional()?.lowercase())
        setNullableString(startIndex + 3, request.taxNumber?.cleanOptionalUpper())
        setNullableString(startIndex + 4, request.addressLine?.cleanOptional())
        setNullableString(startIndex + 5, request.notes?.cleanOptional())
    }

    private fun PreparedStatement.bindProductRequest(
        access: DashboardWorkspaceAccess,
        request: ProductRequest,
        startIndex: Int,
    ) {
        setNullableUuid(startIndex, request.supplierId)
        setString(startIndex + 1, request.name.cleanName())
        setNullableString(startIndex + 2, request.sku?.cleanOptionalUpper())
        setNullableString(startIndex + 3, request.barcode?.cleanOptional())
        setNullableString(startIndex + 4, request.description?.cleanOptional())
        setString(startIndex + 5, request.unit.cleanOptional() ?: "pcs")
        setBigDecimal(startIndex + 6, request.sellingPrice.moneyOrZero())
        setBigDecimal(startIndex + 7, request.costPrice.moneyOrZero())
        setString(startIndex + 8, request.currency?.cleanOptionalUpper() ?: access.currency)
        setBigDecimal(startIndex + 9, request.taxRate.decimalOrZero())
        setBoolean(startIndex + 10, request.pricesIncludeTax)
        setBigDecimal(startIndex + 11, request.stockQuantity.decimalOrZero())
        setBigDecimal(startIndex + 12, request.reorderLevel.decimalOrZero())
        setBoolean(startIndex + 13, request.trackStock)
    }

    private fun ResultSet.toCustomerResponse(): CustomerResponse =
        CustomerResponse(
            id = getString("id"),
            name = getString("name"),
            phoneNumber = getString("phone_number"),
            email = getString("email"),
            addressLine = getString("address_line"),
            city = getString("city"),
            region = getString("region"),
            country = getString("country"),
            postalCode = getString("postal_code"),
            notes = getString("notes"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toSupplierResponse(): SupplierResponse =
        SupplierResponse(
            id = getString("id"),
            name = getString("name"),
            phoneNumber = getString("phone_number"),
            email = getString("email"),
            taxNumber = getString("tax_number"),
            addressLine = getString("address_line"),
            notes = getString("notes"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toProductResponse(): ProductResponse {
        val stockQuantity = getBigDecimal("stock_quantity") ?: BigDecimal.ZERO
        val reorderLevel = getBigDecimal("reorder_level") ?: BigDecimal.ZERO
        val trackStock = getBoolean("track_stock")
        return ProductResponse(
            id = getString("id"),
            supplierId = getString("supplier_id"),
            supplierName = getString("supplier_name"),
            name = getString("name"),
            sku = getString("sku"),
            barcode = getString("barcode"),
            description = getString("description"),
            unit = getString("unit"),
            sellingPrice = getBigDecimal("selling_price").moneyString(),
            costPrice = getBigDecimal("cost_price").moneyString(),
            currency = getString("currency"),
            taxRate = getBigDecimal("tax_rate").decimalString(),
            pricesIncludeTax = getBoolean("prices_include_tax"),
            stockQuantity = stockQuantity.decimalString(),
            reorderLevel = reorderLevel.decimalString(),
            trackStock = trackStock,
            lowStock = trackStock && stockQuantity <= reorderLevel,
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )
    }

    private fun ResultSet.toStockMovementResponse(): StockMovementResponse =
        StockMovementResponse(
            id = getString("id"),
            productId = getString("product_id"),
            movementType = getString("movement_type"),
            quantityDelta = getBigDecimal("quantity_delta").decimalString(),
            balanceAfter = getBigDecimal("balance_after").decimalString(),
            note = getString("note"),
            createdAt = getString("created_at"),
        )

    private fun ResultSet.toOrderResponse(items: List<OrderItemResponse>): OrderResponse =
        OrderResponse(
            id = getString("id"),
            orderNumber = getString("order_number"),
            customerId = getString("customer_id"),
            customerName = getString("customer_name"),
            status = getString("status"),
            scheduledAt = getString("scheduled_at"),
            subtotal = getBigDecimal("subtotal").moneyString(),
            taxTotal = getBigDecimal("tax_total").moneyString(),
            discountTotal = getBigDecimal("discount_total").moneyString(),
            paidTotal = getBigDecimal("paid_total").moneyString(),
            total = getBigDecimal("total").moneyString(),
            currency = getString("currency"),
            notes = getString("notes"),
            itemCount = getInt("item_count"),
            items = items,
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toOrderItemResponse(): OrderItemResponse =
        OrderItemResponse(
            id = getString("id"),
            productId = getString("product_id"),
            productName = getString("product_name"),
            description = getString("description"),
            quantity = getBigDecimal("quantity").decimalString(),
            unitPrice = getBigDecimal("unit_price").moneyString(),
            taxRate = getBigDecimal("tax_rate").decimalString(),
            lineSubtotal = getBigDecimal("line_subtotal").moneyString(),
            lineTax = getBigDecimal("line_tax").moneyString(),
            lineTotal = getBigDecimal("line_total").moneyString(),
        )

    private fun OrderItemRequest.toPreparedOrderItem(): PreparedOrderItem {
        val quantity = quantity.decimalOrZero().takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
        val unitPrice = unitPrice.moneyOrZero()
        val taxRate = taxRate.decimalOrZero()
        val lineSubtotal = quantity.multiply(unitPrice).scaled()
        val lineTax = lineSubtotal.multiply(taxRate).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP).scaled()
        return PreparedOrderItem(
            productId = productId.cleanUuidOrNull(),
            description = description.cleanName(),
            quantity = quantity,
            unitPrice = unitPrice,
            taxRate = taxRate,
            lineSubtotal = lineSubtotal,
            lineTax = lineTax,
            lineTotal = lineSubtotal.add(lineTax).scaled(),
        )
    }

    private fun PreparedStatement.setNullableString(index: Int, value: String?) {
        if (value.isNullOrBlank()) {
            setNull(index, Types.VARCHAR)
        } else {
            setString(index, value)
        }
    }

    private fun PreparedStatement.setNullableUuid(index: Int, value: String?) {
        if (value.cleanUuidOrNull() == null) {
            setNull(index, Types.OTHER)
        } else {
            setString(index, value)
        }
    }

    private fun String?.cleanUuidOrNull(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
            runCatching { UUID.fromString(value).toString() }.getOrNull()
        }

    private fun String.cleanName(): String =
        trim().take(180).ifBlank { "Untitled" }

    private fun String?.cleanOptional(): String? =
        this?.trim()?.take(240)?.ifBlank { null }

    private fun String?.cleanOptionalUpper(): String? =
        cleanOptional()?.uppercase()

    private fun String.decimalOrZero(): BigDecimal =
        trim()
            .replace(",", "")
            .toBigDecimalOrNull()
            ?.scaled()
            ?: BigDecimal.ZERO

    private fun String.moneyOrZero(): BigDecimal =
        decimalOrZero().coerceAtLeast(BigDecimal.ZERO).scaled()

    private fun BigDecimal?.moneyString(): String =
        (this ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun BigDecimal?.decimalString(): String =
        (this ?: BigDecimal.ZERO).stripTrailingZeros().toPlainString()

    private fun BigDecimal.scaled(): BigDecimal =
        setScale(2, RoundingMode.HALF_UP)

    private fun BigDecimal.coerceAtLeast(minimum: BigDecimal): BigDecimal =
        if (this < minimum) minimum else this

    private fun BigDecimal.coerceAtMost(maximum: BigDecimal): BigDecimal =
        if (this > maximum) maximum else this

    private fun String.normalizedOrderStatus(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedOrderStatuses) normalized else "confirmed"
    }

    private fun String.appliesInventory(): Boolean =
        this in InventoryApplyingStatuses

    private data class PreparedOrderItem(
        val productId: String?,
        val description: String,
        val quantity: BigDecimal,
        val unitPrice: BigDecimal,
        val taxRate: BigDecimal,
        val lineSubtotal: BigDecimal,
        val lineTax: BigDecimal,
        val lineTotal: BigDecimal,
    )

    private companion object {
        val AllowedOrderStatuses = setOf("draft", "confirmed", "paid", "part_paid", "completed", "cancelled")
        val InventoryApplyingStatuses = setOf("confirmed", "paid", "part_paid", "completed")
    }
}
