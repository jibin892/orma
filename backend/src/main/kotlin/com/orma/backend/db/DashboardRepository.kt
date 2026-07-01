package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.CustomerResponse
import com.orma.backend.models.DashboardActivityResponse
import com.orma.backend.models.DashboardBreakdownResponse
import com.orma.backend.models.DashboardNotificationPreviewResponse
import com.orma.backend.models.DashboardSummaryResponse
import com.orma.backend.models.DashboardRevenuePointResponse
import com.orma.backend.models.DashboardTaskResponse
import com.orma.backend.models.DashboardTopItemResponse
import com.orma.backend.models.NotificationPreferenceRequest
import com.orma.backend.models.OrderChangeRequestItemResponse
import com.orma.backend.models.OrderChangeRequestRequest
import com.orma.backend.models.OrderChangeRequestResolveRequest
import com.orma.backend.models.OrderChangeRequestResponse
import com.orma.backend.models.OrderItemRequest
import com.orma.backend.models.OrderItemResponse
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.OrderResponse
import com.orma.backend.models.OrderSessionRequest
import com.orma.backend.models.OrderSessionResponse
import com.orma.backend.models.PaginationResponse
import com.orma.backend.models.PrinterProfileRequest
import com.orma.backend.models.PrinterProfileResponse
import com.orma.backend.models.ProductCategoryRequest
import com.orma.backend.models.ProductCategoryResponse
import com.orma.backend.models.ProductOfferRequest
import com.orma.backend.models.ProductOfferResponse
import com.orma.backend.models.PublicCatalogOrderRequest
import com.orma.backend.models.PublicCatalogOrderResponse
import com.orma.backend.models.PublicCatalogPaymentMethodResponse
import com.orma.backend.models.PublicCatalogProductResponse
import com.orma.backend.models.PublicCatalogResponse
import com.orma.backend.models.PublicCatalogWorkspaceResponse
import com.orma.backend.models.PublicCatalogCategoryResponse
import com.orma.backend.models.PublicCatalogOfferResponse
import com.orma.backend.models.ProductExportResponse
import com.orma.backend.models.ProductImportErrorResponse
import com.orma.backend.models.ProductImportRequest
import com.orma.backend.models.ProductImportResponse
import com.orma.backend.models.ProductImportRowRequest
import com.orma.backend.models.ProductImportTemplateResponse
import com.orma.backend.models.ProductRequest
import com.orma.backend.models.ProductResponse
import com.orma.backend.models.ProductVariantAddonRequest
import com.orma.backend.models.ProductVariantAddonResponse
import com.orma.backend.models.ProductVariantComponentRequest
import com.orma.backend.models.ProductVariantComponentResponse
import com.orma.backend.models.ProductVariantRequest
import com.orma.backend.models.ProductVariantResponse
import com.orma.backend.models.StockAdjustmentRequest
import com.orma.backend.models.StockMovementResponse
import com.orma.backend.models.SupplierRequest
import com.orma.backend.models.SupplierResponse
import com.orma.backend.models.WorkspacePaymentMethodRequest
import com.orma.backend.models.WorkspacePaymentMethodResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.util.UUID
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val DashboardRepositoryJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

data class DashboardWorkspaceAccess(
    val userId: String,
    val workspaceId: String,
    val role: String,
    val permissions: List<String>,
    val currency: String,
    val businessMode: String,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
)

data class DashboardQueryFilters(
    val query: String? = null,
    val status: String? = null,
    val itemType: String? = null,
    val orderType: String? = null,
    val datePreset: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val page: Int = 1,
    val limit: Int = 50,
    val lowStockOnly: Boolean = false,
    val supplierId: String? = null,
    val categoryId: String? = null,
    val barcode: String? = null,
    val scheduledOnly: Boolean = false,
    val excludeCancelled: Boolean = false,
)

class DashboardOrderValidationException(
    val code: String,
    publicMessage: String,
) : IllegalArgumentException(publicMessage)

data class PagedResult<T>(
    val items: List<T>,
    val pagination: PaginationResponse,
)

private data class PublicCatalogCustomerIdentifiers(
    val email: String?,
    val phoneNumber: String?,
)

sealed interface PublicCatalogOrderSubmitResult {
    data class Success(
        val response: PublicCatalogOrderResponse,
    ) : PublicCatalogOrderSubmitResult

    data object WorkspaceNotFound : PublicCatalogOrderSubmitResult

    data object ItemsUnavailable : PublicCatalogOrderSubmitResult

    data object AppointmentTimeRequired : PublicCatalogOrderSubmitResult
}

private val ProductCsvColumns = listOf(
    "name",
    "itemType",
    "categoryName",
    "sku",
    "barcode",
    "description",
    "unit",
    "sellingPrice",
    "costPrice",
    "currency",
    "taxRate",
    "pricesIncludeTax",
    "stockQuantity",
    "reorderLevel",
    "trackStock",
    "durationMinutes",
    "bookingRequired",
    "expiryDate",
    "supplierName",
    "status",
)

private val RequiredProductCsvColumns = listOf("name")

private fun productImportTemplateCsv(
    businessMode: String,
    currency: String,
): String {
    val itemTypes = businessMode.templateItemTypes()
    val examples = itemTypes.map { itemType -> productImportTemplateRow(itemType, currency.ifBlank { "INR" }) }
    return (listOf(ProductCsvColumns) + examples)
        .joinToString("\n") { row -> row.joinToString(",") { it.csvTemplateEscaped() } } + "\n"
}

private fun String.csvTemplateEscaped(): String =
    if (any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else {
        this
    }

private fun String.templateItemTypes(): List<String> =
    when (trim().lowercase().replace("-", "_")) {
        "service_selling", "services" -> listOf("service")
        "appointment", "appointments", "booking" -> listOf("appointment")
        "mixed", "multi", "hybrid" -> listOf("product", "service", "appointment")
        else -> listOf("product")
    }

private fun productImportTemplateRow(itemType: String, currency: String): List<String> =
    when (itemType) {
        "service" -> listOf(
            "",
            "service",
            "",
            "",
            "",
            "",
            "service",
            "",
            "",
            currency,
            "",
            "false",
            "",
            "",
            "false",
            "60",
            "false",
            "",
            "Preferred supplier",
            "active",
        )
        "appointment" -> listOf(
            "",
            "appointment",
            "",
            "",
            "",
            "",
            "booking",
            "",
            "",
            currency,
            "",
            "false",
            "",
            "",
            "false",
            "30",
            "true",
            "",
            "",
            "active",
        )
        else -> listOf(
            "",
            "product",
            "",
            "",
            "",
            "",
            "pcs",
            "",
            "",
            currency,
            "",
            "false",
            "",
            "",
            "true",
            "",
            "false",
            "",
            "",
            "active",
        )
    }

class DashboardRepository(
    private val dataSource: DataSource,
    private val config: AppConfig,
) {
    suspend fun publicCatalog(
        workspaceId: String,
    ): PublicCatalogResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val workspace = connection.findPublicCatalogWorkspace(workspaceId) ?: return@withContext null
            PublicCatalogResponse(
                workspace = workspace,
                categories = connection.listPublicCatalogCategories(workspace.id),
                paymentMethods = connection.listPublicCatalogPaymentMethods(workspace.id),
                products = connection.listPublicCatalogProducts(workspace.id),
            )
        }
    }

    suspend fun publicCatalogOrder(
        workspaceId: String,
        orderId: String,
    ): PublicCatalogOrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: return@withContext null
            val cleanOrderId = orderId.cleanUuidOrNull() ?: return@withContext null
            val order = connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                ?.takeIf { it.source == "public_catalog" }
                ?: return@withContext null
            val balanceDue = order.paymentBalanceDue()
            val paymentMethod = if (
                balanceDue > BigDecimal.ZERO &&
                order.publicCatalogCanCollectBalancePayment()
            ) {
                connection.defaultPublicCatalogPaymentMethod(access.workspaceId)
            } else {
                null
            }
            PublicCatalogOrderResponse(
                message = order.publicCatalogStatusMessage(),
                order = order,
                paymentLink = paymentMethod?.toUpiPaymentLink(
                    amount = balanceDue.moneyString(),
                    currency = order.currency,
                    note = "ORMA ${order.orderNumber}",
                ),
                paymentMethod = paymentMethod,
            )
        }
    }

    suspend fun createPublicCatalogOrderChangeRequest(
        firebaseUser: VerifiedFirebaseUser?,
        workspaceId: String,
        orderId: String,
        request: OrderChangeRequestRequest,
    ): PublicCatalogOrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val cleanOrderId = orderId.cleanUuidOrNull() ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val order = connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                    ?.takeIf { it.source == "public_catalog" }
                    ?: run {
                        connection.rollback()
                        return@withContext null
                    }
                if (!order.status.allowsPublicCatalogChangeRequest()) {
                    throw DashboardOrderValidationException(
                        code = "public_order_change_closed",
                        publicMessage = "This request is already closed. Contact the business directly for changes.",
                    )
                }
                if (order.changeRequests.any { it.status == "pending" }) {
                    throw DashboardOrderValidationException(
                        code = "public_order_change_pending",
                        publicMessage = "A change request is already waiting for business review.",
                    )
                }
                val changeItems = connection.prepareOrderChangeRequestItems(order, request)
                val requestedPaymentMode = request.requestedPaymentMode?.cleanOptional()?.cleanPaymentMode()
                val requestedPaidTotal = request.requestedPaidTotal
                    ?.cleanOptional()
                    ?.moneyOrZero()
                    ?.coerceAtLeast(BigDecimal.ZERO)
                    ?.scaled()
                val paymentReference = request.paymentReference.cleanOptional()
                val customerNotes = request.notes.cleanOptional()
                val hasPaymentChange = requestedPaymentMode != null || requestedPaidTotal != null || paymentReference != null
                if (changeItems.isEmpty() && !hasPaymentChange && customerNotes == null) {
                    throw DashboardOrderValidationException(
                        code = "public_order_change_empty",
                        publicMessage = "Change a quantity, payment detail, or note before sending the request.",
                    )
                }
                val requestId = connection.insertOrderChangeRequest(
                    workspaceId = access.workspaceId,
                    order = order,
                    firebaseUid = firebaseUser?.uid,
                    items = changeItems,
                    requestedPaymentMode = requestedPaymentMode,
                    requestedPaidTotal = requestedPaidTotal,
                    paymentReference = paymentReference,
                    customerNotes = customerNotes,
                )
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "order_change_requested",
                    entityType = "order",
                    entityId = order.id,
                    entityLabel = order.orderNumber,
                    title = "Order change requested",
                    body = "${order.customerName ?: "Customer"} requested an update for ${order.orderNumber}.",
                    tone = "warning",
                    actorUserId = null,
                    actorDisplayName = order.customerName,
                    actorEmail = order.customerEmail,
                    actorPhoneNumber = order.customerPhoneNumber,
                    actorRole = "public_catalog",
                )
                connection.commit()
                publicCatalogOrder(workspaceId, cleanOrderId)?.copy(
                    message = "Change request sent. The business will review it shortly.",
                    order = connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                        ?: order.copy(changeRequests = listOf(connection.getOrderChangeRequest(access.workspaceId, requestId)!!)),
                )
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun publicCatalogCustomerOrders(
        firebaseUser: VerifiedFirebaseUser,
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(limit = 20),
    ): PagedResult<OrderResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: return@withContext null
            val identifiers = firebaseUser.publicCatalogCustomerIdentifiers()
            if (identifiers.email == null && identifiers.phoneNumber == null) {
                return@withContext emptyList<OrderResponse>().toPagedResult(filters, totalItems = 0)
            }
            connection.listPublicCatalogCustomerOrders(
                workspaceId = access.workspaceId,
                identifiers = identifiers,
                filters = filters,
                includeItems = true,
            )
        }
    }

    suspend fun updatePublicCatalogNotificationPreference(
        firebaseUser: VerifiedFirebaseUser,
        workspaceId: String,
        request: NotificationPreferenceRequest,
    ): Boolean? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: return@withContext null
            val identifiers = firebaseUser.publicCatalogCustomerIdentifiers()
            if (request.enabled) {
                connection.upsertPublicCatalogNotificationDeviceToken(
                    workspaceId = access.workspaceId,
                    firebaseUser = firebaseUser,
                    identifiers = identifiers,
                    request = request,
                )
            } else {
                connection.disablePublicCatalogNotificationDeviceToken(
                    workspaceId = access.workspaceId,
                    firebaseUser = firebaseUser,
                    identifiers = identifiers,
                    token = request.deviceToken,
                )
            }
            request.enabled
        }
    }

    suspend fun createPublicCatalogOrder(
        workspaceId: String,
        request: PublicCatalogOrderRequest,
        firebaseUser: VerifiedFirebaseUser? = null,
    ): PublicCatalogOrderSubmitResult = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: run {
                    connection.rollback()
                    return@withContext PublicCatalogOrderSubmitResult.WorkspaceNotFound
                }
                val publicItems = connection.publicOrderItems(access.workspaceId, request)
                val requestedItemCount = request.items
                    .take(50)
                    .count { it.productId.cleanUuidOrNull() != null && it.quantity.decimalOrZero() > BigDecimal.ZERO }
                if (publicItems.isEmpty() || publicItems.size != requestedItemCount) {
                    connection.rollback()
                    return@withContext PublicCatalogOrderSubmitResult.ItemsUnavailable
                }
                val orderType = publicItems.derivedOrderType()
                if (orderType == "appointment" && request.scheduledAt.cleanOptional() == null) {
                    connection.rollback()
                    return@withContext PublicCatalogOrderSubmitResult.AppointmentTimeRequired
                }
                val publicCustomerEmail = request.customerEmail.cleanOptional()?.lowercase()
                    ?: firebaseUser?.email.cleanOptional()?.lowercase()
                val publicCustomerPhone = request.phoneNumber.cleanOptional()
                    ?: firebaseUser?.phoneNumber.cleanOptional()
                    ?: request.phoneNumber
                val existingCustomer = connection.findCustomerByPhone(
                    workspaceId = access.workspaceId,
                    phoneNumber = publicCustomerPhone,
                )
                val customer = existingCustomer?.let { customer ->
                    connection.updatePublicCatalogCustomerContact(
                        workspaceId = access.workspaceId,
                        customer = customer,
                        customerName = request.customerName,
                        phoneNumber = publicCustomerPhone,
                        email = publicCustomerEmail,
                    )
                } ?: connection.insertCustomer(
                    workspaceId = access.workspaceId,
                    request = CustomerRequest(
                        name = request.customerName,
                        phoneNumber = publicCustomerPhone,
                        email = publicCustomerEmail,
                        notes = "Created from public catalog.",
                    ),
                ).also { createdCustomer ->
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "customer_created",
                        entityType = "customer",
                        entityId = createdCustomer.id,
                        entityLabel = createdCustomer.name,
                        title = "Customer created",
                        body = createdCustomer.name,
                        tone = "success",
                        actorUserId = null,
                        actorDisplayName = request.customerName,
                        actorEmail = publicCustomerEmail,
                        actorPhoneNumber = publicCustomerPhone,
                        actorRole = "public_catalog",
                    )
                }
                val fulfillment = when (orderType) {
                    "appointment" -> "booking"
                    "service" -> if (request.scheduledAt.cleanOptional() != null) "scheduled" else "standard"
                    else -> request.fulfillmentType.cleanFulfillmentType()
                }
                val paymentMode = request.paymentMode.cleanPaymentMode()
                val publicNote = buildList {
                    add("Public catalog request.")
                    add("Fulfillment: ${fulfillment.replace('_', ' ')}.")
                    add("Payment: ${paymentMode.replace('_', ' ')}.")
                    request.notes.cleanOptional()?.let { add(it) }
                }.joinToString(" ")
                val order = connection.insertOrder(
                    access = access,
                    request = OrderRequest(
                        customerId = customer.id,
                        orderType = orderType,
                        status = "new",
                        scheduledAt = request.scheduledAt?.cleanOptional(),
                        paidTotal = "0",
                        currency = access.currency,
                        notes = publicNote,
                        fulfillmentType = fulfillment,
                        paymentMode = paymentMode,
                        source = "public_catalog",
                        clientRequestId = request.clientRequestId,
                        items = publicItems.map { it.orderItem },
                    ),
                    discountTotalOverride = publicItems.fold(BigDecimal.ZERO) { total, item ->
                        total.add(item.discountTotal)
                    }.scaled(),
                )
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "${order.orderType.cleanActivityType()}_created",
                    entityType = "order",
                    entityId = order.id,
                    entityLabel = order.orderNumber,
                    title = order.activityCreatedTitle(),
                    body = order.activitySummaryBody(),
                    tone = order.status.dashboardTone(),
                    actorUserId = null,
                    actorDisplayName = request.customerName,
                    actorEmail = publicCustomerEmail,
                    actorPhoneNumber = publicCustomerPhone,
                    actorRole = "public_catalog",
                )
                val balanceDue = order.paymentBalanceDue()
                val paymentMethod = if (
                    balanceDue > BigDecimal.ZERO &&
                    order.publicCatalogCanCollectBalancePayment()
                ) {
                    connection.defaultPublicCatalogPaymentMethod(access.workspaceId)
                } else {
                    null
                }
                connection.commit()
                PublicCatalogOrderSubmitResult.Success(
                    PublicCatalogOrderResponse(
                        message = "Request received. The business will review it shortly.",
                        order = order,
                        paymentLink = paymentMethod?.toUpiPaymentLink(
                            amount = balanceDue.moneyString(),
                            currency = order.currency,
                            note = "ORMA ${order.orderNumber}",
                        ),
                        paymentMethod = paymentMethod,
                    ),
                )
            } catch (error: DashboardOrderValidationException) {
                connection.rollback()
                if (error.code == "public_catalog_stock_unavailable") {
                    return@withContext PublicCatalogOrderSubmitResult.ItemsUnavailable
                }
                throw error
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun summary(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): DashboardSummaryResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            fun orderParams(): Array<String> =
                filters.withOrderDateParams(access.workspaceId).toTypedArray()
            val supplierPayableAmount = connection.supplierFinancialAmount(
                workspaceId = access.workspaceId,
                filters = filters,
                amountColumn = "payable_amount",
                supplierAmountColumn = "payable_total",
                supplierDateExpression = "s.created_at",
            )
            val supplierSpentAmount = connection.supplierFinancialAmount(
                workspaceId = access.workspaceId,
                filters = filters,
                amountColumn = "paid_amount",
                supplierAmountColumn = "paid_total",
                supplierDateExpression = "coalesce(s.last_payment_at, s.updated_at, s.created_at)",
            )
            val supplierBalanceAmount = (supplierPayableAmount - supplierSpentAmount).coerceAtLeast(BigDecimal.ZERO)

            DashboardSummaryResponse(
                currency = access.currency,
                businessMode = access.businessMode,
                totalCustomers = connection.scalarInt(
                    """
                    select count(*)
                    from customers
                    where workspace_id = ?::uuid and status = 'active'
                    ${filters.createdDateWhereSql("created_at")}
                    """.trimIndent(),
                    *filters.withCreatedDateParams(access.workspaceId).toTypedArray(),
                ),
                totalSalesAmount = connection.scalarDecimal(
                    """
                    select coalesce(sum(o.total), 0)
                    from orders o
                    where o.workspace_id = ?::uuid and o.status <> 'cancelled'
                    ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ).moneyString(),
                totalPaidAmount = connection.scalarDecimal(
                    """
                    select coalesce(sum(${effectivePaidTotalSql("o")}), 0)
                    from orders o
                    where o.workspace_id = ?::uuid and o.status <> 'cancelled'
                    ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ).moneyString(),
                totalOutstandingAmount = connection.scalarDecimal(
                    """
                    select coalesce(sum(greatest(coalesce(o.total, 0) - ${effectivePaidTotalSql("o")}, 0)), 0)
                    from orders o
                    where o.workspace_id = ?::uuid and o.status <> 'cancelled'
                    ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ).moneyString(),
                supplierPayableAmount = supplierPayableAmount.moneyString(),
                supplierSpentAmount = supplierSpentAmount.moneyString(),
                supplierBalanceAmount = supplierBalanceAmount.moneyString(),
                ordersCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders o
                    where o.workspace_id = ?::uuid
                      and o.status <> 'cancelled'
                      ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ),
                bookingsCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders o
                    where o.workspace_id = ?::uuid
                      and status <> 'cancelled'
                      and (order_type = 'appointment' or scheduled_at is not null)
                      ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ),
                salesCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders o
                    where o.workspace_id = ?::uuid
                      and o.status <> 'cancelled'
                      and o.order_type = 'sale'
                      ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ),
                serviceOrdersCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders o
                    where o.workspace_id = ?::uuid
                      and o.status <> 'cancelled'
                      and o.order_type = 'service'
                      ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ),
                appointmentsCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders o
                    where o.workspace_id = ?::uuid
                      and o.status <> 'cancelled'
                      and o.order_type = 'appointment'
                      ${filters.orderDateWhereSql("o")}
                    """.trimIndent(),
                    *orderParams(),
                ),
                todayAppointmentsCount = connection.scalarInt(
                    """
                    select count(*)
                    from orders
                    where workspace_id = ?::uuid
                      and status <> 'cancelled'
                      and order_type = 'appointment'
                      and scheduled_at is not null
                      and scheduled_at >= date_trunc('day', now())
                      and scheduled_at < date_trunc('day', now()) + interval '1 day'
                    """.trimIndent(),
                    access.workspaceId,
                ),
                productsInStock = connection.scalarInt(
                    """
                    select count(*)
                    from products
                    where workspace_id = ?::uuid
                      and status = 'active'
                      and item_type = 'product'
                      and (track_stock = false or stock_quantity > 0)
                      ${filters.createdDateWhereSql("created_at")}
                    """.trimIndent(),
                    *filters.withCreatedDateParams(access.workspaceId).toTypedArray(),
                ),
                lowStockProducts = connection.scalarInt(
                    """
                    select count(*)
                    from products
                    where workspace_id = ?::uuid
                      and status = 'active'
                      and track_stock = true
                      and stock_quantity <= reorder_level
                      ${filters.createdDateWhereSql("created_at")}
                    """.trimIndent(),
                    *filters.withCreatedDateParams(access.workspaceId).toTypedArray(),
                ),
                recentOrders = connection.listOrders(
                    workspaceId = access.workspaceId,
                    filters = filters.copy(limit = 5, page = 1),
                    includeItems = false,
                    excludeCancelledWhenStatusAll = true,
                ).items,
                lowStockItems = connection.listProducts(
                    workspaceId = access.workspaceId,
                    filters = DashboardQueryFilters(limit = 5, lowStockOnly = true),
                ).items,
                revenueSeries = connection.listDashboardRevenueSeries(access.workspaceId, filters),
                orderStatusBreakdown = connection.listOrderStatusBreakdown(access.workspaceId, filters),
                orderTypeBreakdown = connection.listOrderTypeBreakdown(access.workspaceId, filters),
                topItems = connection.listDashboardTopItems(access.workspaceId, filters),
                recentActivity = connection.listDashboardActivity(access.workspaceId, filters),
                dashboardTasks = connection.listDashboardTasks(access.workspaceId),
                notificationPreview = connection.listDashboardNotificationPreview(access.workspaceId),
            )
        }
    }

    suspend fun notificationEvents(
        firebaseUser: VerifiedFirebaseUser,
        limit: Int = 20,
    ): List<DashboardNotificationPreviewResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listDashboardNotificationPreview(
                workspaceId = access.workspaceId,
                limit = limit.coerceIn(1, 50),
            )
        }
    }

    suspend fun customers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<CustomerResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listCustomers(access.workspaceId, filters)
        }
    }

    suspend fun customerOrders(
        firebaseUser: VerifiedFirebaseUser,
        customerId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<OrderResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listCustomerOrders(
                workspaceId = access.workspaceId,
                customerId = customerId,
                filters = filters,
                includeItems = true,
            )
        }
    }

    suspend fun createCustomer(
        firebaseUser: VerifiedFirebaseUser,
        request: CustomerRequest,
    ): CustomerResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageCustomers, "create customers")
                val customer = connection.insertCustomer(access.workspaceId, request)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "customer_created",
                    entityType = "customer",
                    entityId = customer.id,
                    entityLabel = customer.name,
                    title = "Customer created",
                    body = customer.name,
                    tone = "success",
                )
                connection.commit()
                customer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateCustomer(
        firebaseUser: VerifiedFirebaseUser,
        customerId: String,
        request: CustomerRequest,
    ): CustomerResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageCustomers, "update customers")
                val customer = connection.updateCustomer(access.workspaceId, customerId, request)
                if (customer != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "customer_updated",
                        entityType = "customer",
                        entityId = customer.id,
                        entityLabel = customer.name,
                        title = "Customer updated",
                        body = customer.name,
                    )
                }
                connection.commit()
                customer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun suppliers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<SupplierResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listSuppliers(access.workspaceId, filters)
        }
    }

    suspend fun createSupplier(
        firebaseUser: VerifiedFirebaseUser,
        request: SupplierRequest,
    ): SupplierResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageStock, "create suppliers")
                val supplier = connection.insertSupplier(access.workspaceId, request)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "supplier_created",
                    entityType = "supplier",
                    entityId = supplier.id,
                    entityLabel = supplier.name,
                    title = "Supplier created",
                    body = supplier.name,
                    tone = "success",
                )
                connection.commit()
                supplier
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateSupplier(
        firebaseUser: VerifiedFirebaseUser,
        supplierId: String,
        request: SupplierRequest,
    ): SupplierResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageStock, "update suppliers")
                val supplier = connection.updateSupplier(access.workspaceId, supplierId, request)
                if (supplier != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "supplier_updated",
                        entityType = "supplier",
                        entityId = supplier.id,
                        entityLabel = supplier.name,
                        title = "Supplier updated",
                        body = supplier.name,
                    )
                }
                connection.commit()
                supplier
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun productCategories(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductCategoryResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listProductCategories(access.workspaceId, filters)
        }
    }

    suspend fun createProductCategory(
        firebaseUser: VerifiedFirebaseUser,
        request: ProductCategoryRequest,
    ): ProductCategoryResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requireAnyPermission(
                    permissions = listOf(PermissionCreateProduct, PermissionCreateService, PermissionCreateAppointment),
                    actionLabel = "create categories",
                )
                val category = connection.insertProductCategory(access.workspaceId, request)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "category_created",
                    entityType = "product_category",
                    entityId = category.id,
                    entityLabel = category.name,
                    title = "Category created",
                    body = category.name,
                    tone = "success",
                )
                connection.commit()
                category
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun productOffers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductOfferResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listProductOffers(access.workspaceId, filters)
        }
    }

    suspend fun createProductOffer(
        firebaseUser: VerifiedFirebaseUser,
        request: ProductOfferRequest,
    ): ProductOfferResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionCreateOffer, "create offers")
                val offer = connection.insertProductOffer(access.workspaceId, request)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "offer_created",
                    entityType = "product_offer",
                    entityId = offer.id,
                    entityLabel = offer.name,
                    title = "Offer created",
                    body = offer.name,
                    tone = "success",
                )
                connection.commit()
                offer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updateProductOffer(
        firebaseUser: VerifiedFirebaseUser,
        offerId: String,
        request: ProductOfferRequest,
    ): ProductOfferResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionCreateOffer, "update offers")
                val offer = connection.updateProductOffer(access.workspaceId, offerId, request)
                if (offer != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "offer_updated",
                        entityType = "product_offer",
                        entityId = offer.id,
                        entityLabel = offer.name,
                        title = "Offer updated",
                        body = offer.name,
                        tone = "info",
                    )
                }
                connection.commit()
                offer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun paymentMethods(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<WorkspacePaymentMethodResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listPaymentMethods(access.workspaceId, filters)
        }
    }

    suspend fun createPaymentMethod(
        firebaseUser: VerifiedFirebaseUser,
        request: WorkspacePaymentMethodRequest,
    ): WorkspacePaymentMethodResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "create payment methods")
                val method = connection.insertPaymentMethod(access.workspaceId, request)
                connection.ensureOneDefaultPaymentMethod(access.workspaceId)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "payment_method_created",
                    entityType = "payment_method",
                    entityId = method.id,
                    entityLabel = method.label,
                    title = "Payment method created",
                    body = method.label,
                    tone = "success",
                )
                connection.commit()
                method
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updatePaymentMethod(
        firebaseUser: VerifiedFirebaseUser,
        paymentMethodId: String,
        request: WorkspacePaymentMethodRequest,
    ): WorkspacePaymentMethodResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "update payment methods")
                val method = connection.updatePaymentMethod(access.workspaceId, paymentMethodId, request) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                connection.ensureOneDefaultPaymentMethod(access.workspaceId)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "payment_method_updated",
                    entityType = "payment_method",
                    entityId = method.id,
                    entityLabel = method.label,
                    title = "UPI ID updated",
                    body = method.label,
                    tone = "info",
                )
                connection.commit()
                method
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun setDefaultPaymentMethod(
        firebaseUser: VerifiedFirebaseUser,
        paymentMethodId: String,
    ): WorkspacePaymentMethodResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "set default payment methods")
                val method = connection.setDefaultPaymentMethod(access.workspaceId, paymentMethodId) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "payment_method_defaulted",
                    entityType = "payment_method",
                    entityId = method.id,
                    entityLabel = method.label,
                    title = "Default UPI changed",
                    body = method.label,
                    tone = "success",
                )
                connection.commit()
                method
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun deletePaymentMethod(
        firebaseUser: VerifiedFirebaseUser,
        paymentMethodId: String,
    ): WorkspacePaymentMethodResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "delete payment methods")
                val method = connection.deletePaymentMethod(access.workspaceId, paymentMethodId) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                connection.ensureOneDefaultPaymentMethod(access.workspaceId)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "payment_method_deleted",
                    entityType = "payment_method",
                    entityId = method.id,
                    entityLabel = method.label,
                    title = "UPI ID deleted",
                    body = method.label,
                    tone = "warning",
                )
                connection.commit()
                method
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun products(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listProducts(access.workspaceId, filters = filters)
        }
    }

    suspend fun exportProducts(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): ProductExportResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            val products = connection.listProducts(access.workspaceId, filters = filters.copy(page = 1, limit = 500)).items
            ProductExportResponse(
                fileName = "orma-products-${access.workspaceId.take(8)}.csv",
                count = products.size,
                columns = ProductCsvColumns,
                csv = products.toProductCsv(),
            )
        }
    }

    suspend fun productImportTemplate(
        firebaseUser: VerifiedFirebaseUser,
    ): ProductImportTemplateResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            ProductImportTemplateResponse(
                fileName = "orma-products-template-${access.workspaceId.take(8)}.csv",
                columns = ProductCsvColumns,
                requiredColumns = RequiredProductCsvColumns,
                csv = productImportTemplateCsv(
                    businessMode = access.businessMode,
                    currency = access.currency,
                ),
            )
        }
    }

    suspend fun importProductsCsv(
        firebaseUser: VerifiedFirebaseUser,
        csv: String,
    ): ProductImportResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val request = try {
                    csv.toProductImportRequest(defaultCurrency = access.currency)
                } catch (error: ProductCsvParseException) {
                    connection.rollback()
                    return@withContext ProductImportResponse(
                        created = 0,
                        skipped = 0,
                        errors = listOf(ProductImportErrorResponse(row = error.row, message = error.publicMessage)),
                    )
                }
                if (request.rows.isEmpty()) {
                    connection.rollback()
                    return@withContext ProductImportResponse(
                        created = 0,
                        skipped = 0,
                        errors = listOf(
                            ProductImportErrorResponse(
                                row = 2,
                                message = "Add at least one product row below the header.",
                            ),
                        ),
                    )
                }
                request.rows.map { it.itemType.catalogCreatePermission() }.distinct().forEach { permission ->
                    access.requirePermission(permission, "import this catalog item type")
                }
                val response = connection.importProductRows(access, request)
                if (response.created > 0 || response.skipped > 0) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "catalog_imported",
                        entityType = "product_import",
                        title = "Catalog import",
                        body = "${response.created} created, ${response.skipped} skipped",
                        tone = if (response.errors.isEmpty()) "success" else "warning",
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

    suspend fun importProducts(
        firebaseUser: VerifiedFirebaseUser,
        request: ProductImportRequest,
    ): ProductImportResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                request.rows.map { it.itemType.catalogCreatePermission() }.distinct().forEach { permission ->
                    access.requirePermission(permission, "import this catalog item type")
                }
                val response = connection.importProductRows(access, request)
                if (response.created > 0 || response.skipped > 0) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "catalog_imported",
                        entityType = "product_import",
                        title = "Catalog import",
                        body = "${response.created} created, ${response.skipped} skipped",
                        tone = if (response.errors.isEmpty()) "success" else "warning",
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

    private fun Connection.importProductRows(
        access: DashboardWorkspaceAccess,
        request: ProductImportRequest,
    ): ProductImportResponse {
        val imported = mutableListOf<ProductResponse>()
        val errors = mutableListOf<ProductImportErrorResponse>()
        val seenSkus = mutableSetOf<String>()
        val seenBarcodes = mutableSetOf<String>()
        var skipped = 0
        request.rows.take(500).forEachIndexed { index, row ->
            val rowNumber = index + 2
            val name = row.name.cleanOptional()
            if (name == null) {
                skipped += 1
                errors += ProductImportErrorResponse(row = rowNumber, message = "Product name is required.")
                return@forEachIndexed
            }
            val validationErrors = row.productImportValidationErrors()
            if (validationErrors.isNotEmpty()) {
                skipped += 1
                errors += ProductImportErrorResponse(row = rowNumber, message = validationErrors.joinToString(" "))
                return@forEachIndexed
            }
            val cleanSku = row.sku.cleanOptionalUpper()
            val cleanBarcode = row.barcode.cleanOptional()
            when {
                cleanSku != null && cleanSku in seenSkus -> {
                    skipped += 1
                    errors += ProductImportErrorResponse(row = rowNumber, message = "Duplicate SKU in this CSV.")
                    return@forEachIndexed
                }
                cleanBarcode != null && cleanBarcode in seenBarcodes -> {
                    skipped += 1
                    errors += ProductImportErrorResponse(row = rowNumber, message = "Duplicate barcode in this CSV.")
                    return@forEachIndexed
                }
                productExists(access.workspaceId, cleanSku, cleanBarcode) -> {
                    skipped += 1
                    errors += ProductImportErrorResponse(row = rowNumber, message = "SKU or barcode already exists in this workspace.")
                    return@forEachIndexed
                }
            }
            val savepoint = setSavepoint("product_import_$rowNumber")
            try {
                val supplierId = row.supplierName.cleanOptional()?.let { supplierName ->
                    findOrCreateSupplier(access.workspaceId, supplierName)
                }
                val productRequest = row.toProductRequest(
                    access = access,
                    supplierId = supplierId,
                    productName = name,
                )
                val product = insertProduct(access, productRequest)
                if (productRequest.trackStock && productRequest.stockQuantity.decimalOrZero() != BigDecimal.ZERO) {
                    insertStockMovement(
                        access = access,
                        productId = product.id,
                        movementType = "import",
                        quantityDelta = productRequest.stockQuantity.decimalOrZero(),
                        balanceAfter = productRequest.stockQuantity.decimalOrZero(),
                        note = "Product import",
                    )
                }
                imported += product
                cleanSku?.let(seenSkus::add)
                cleanBarcode?.let(seenBarcodes::add)
                releaseSavepoint(savepoint)
            } catch (error: SQLException) {
                rollback(savepoint)
                skipped += 1
                errors += ProductImportErrorResponse(
                    row = rowNumber,
                    message = error.productImportRowMessage(),
                )
            }
        }
        return ProductImportResponse(
            created = imported.size,
            skipped = skipped,
            errors = errors,
            products = imported,
        )
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
                access.requirePermission(request.itemType.catalogCreatePermission(), "create this catalog item")
                request.validateCatalogItemForSave()
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
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "${product.itemType.cleanActivityType()}_created",
                    entityType = product.itemType.cleanItemType(),
                    entityId = product.id,
                    entityLabel = product.name,
                    title = "${product.itemType.sellableActivityLabel()} created",
                    body = product.catalogActivityBody(),
                    tone = "success",
                )
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
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(request.itemType.catalogCreatePermission(), "update this catalog item")
                request.validateCatalogItemForSave()
                val product = connection.updateProduct(access, productId, request)
                if (product != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "${product.itemType.cleanActivityType()}_updated",
                        entityType = product.itemType.cleanItemType(),
                        entityId = product.id,
                        entityLabel = product.name,
                        title = "${product.itemType.sellableActivityLabel()} updated",
                        body = product.catalogActivityBody(),
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
                access.requirePermission(PermissionManageStock, "update stock and availability")
                if (request.status?.let { !it.isValidCatalogStatusInput() } == true) {
                    throw DashboardOrderValidationException(
                        code = "product_status_invalid",
                        publicMessage = "Choose active, hidden, unavailable, inactive, or out of stock.",
                    )
                }
                val product = connection.adjustProductStock(access, productId, request)
                if (product != null) {
                    val quantityChanged = request.quantityDelta.decimalOrZero() != BigDecimal.ZERO
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = if (quantityChanged) "stock_adjusted" else "product_updated",
                        entityType = "product",
                        entityId = product.id,
                        entityLabel = product.name,
                        title = if (quantityChanged) "Stock adjusted" else "Product details updated",
                        body = if (quantityChanged) {
                            "${product.name} · ${request.quantityDelta} ${product.unit} · balance ${product.stockQuantity}"
                        } else {
                            "${product.name} · ${product.status.cleanCatalogStatus()} · ${product.currency} ${product.sellingPrice}"
                        },
                        tone = if (quantityChanged) "warning" else "info",
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

    suspend fun orders(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<OrderResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listOrders(
                workspaceId = access.workspaceId,
                filters = filters,
                includeItems = true,
                excludeCancelledWhenStatusAll = filters.excludeCancelled,
            )
        }
    }

    suspend fun printers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<PrinterProfileResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listPrinters(access.workspaceId, filters)
        }
    }

    suspend fun createPrinter(
        firebaseUser: VerifiedFirebaseUser,
        request: PrinterProfileRequest,
    ): PrinterProfileResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "create printer profiles")
                val printer = connection.insertPrinter(access.workspaceId, request)
                connection.ensurePrinterDefaults(access.workspaceId)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "printer_created",
                    entityType = "printer",
                    entityId = printer.id,
                    entityLabel = printer.name,
                    title = "Printer created",
                    body = printer.name,
                    tone = "success",
                )
                connection.commit()
                printer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun updatePrinter(
        firebaseUser: VerifiedFirebaseUser,
        printerId: String,
        request: PrinterProfileRequest,
    ): PrinterProfileResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "update printer profiles")
                val printer = connection.updatePrinter(access.workspaceId, printerId, request)
                if (printer != null) {
                    connection.ensurePrinterDefaults(access.workspaceId)
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "printer_updated",
                        entityType = "printer",
                        entityId = printer.id,
                        entityLabel = printer.name,
                        title = "Printer updated",
                        body = printer.name,
                    )
                }
                connection.commit()
                printer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun deletePrinter(
        firebaseUser: VerifiedFirebaseUser,
        printerId: String,
    ): PrinterProfileResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionManageAccount, "delete printer profiles")
                val printer = connection.deletePrinter(access.workspaceId, printerId) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                connection.ensurePrinterDefaults(access.workspaceId)
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "printer_deleted",
                    entityType = "printer",
                    entityId = printer.id,
                    entityLabel = printer.name,
                    title = "Printer removed",
                    body = printer.name,
                    tone = "warning",
                )
                connection.commit()
                printer
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
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
                access.requirePermission(PermissionCreateSale, "create sales or bookings")
                val order = connection.insertOrder(access, request)
                if (request.customerId.cleanUuidOrNull() == null && !request.customerName.cleanOptional().isNullOrBlank()) {
                    order.customerId?.let { customerId ->
                        connection.insertWorkspaceActivity(
                            access = access,
                            activityType = "customer_created",
                            entityType = "customer",
                            entityId = customerId,
                            entityLabel = order.customerName,
                            title = "Customer created",
                            body = order.customerName ?: "Customer profile",
                            tone = "success",
                        )
                    }
                }
                connection.insertWorkspaceActivity(
                    access = access,
                    activityType = "${order.orderType.cleanActivityType()}_created",
                    entityType = "order",
                    entityId = order.id,
                    entityLabel = order.orderNumber,
                    title = order.activityCreatedTitle(),
                    body = order.activitySummaryBody(),
                    tone = order.status.dashboardTone(),
                )
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

    suspend fun updateOrder(
        firebaseUser: VerifiedFirebaseUser,
        orderId: String,
        request: OrderRequest,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionEditSale, "edit sales or bookings")
                val updated = connection.updateOrder(access, orderId, request)
                if (updated != null) {
                    if (request.customerId.cleanUuidOrNull() == null && !request.customerName.cleanOptional().isNullOrBlank()) {
                        updated.customerId?.let { customerId ->
                            connection.insertWorkspaceActivity(
                                access = access,
                                activityType = "customer_created",
                                entityType = "customer",
                                entityId = customerId,
                                entityLabel = updated.customerName,
                                title = "Customer created",
                                body = updated.customerName ?: "Customer profile",
                                tone = "success",
                            )
                        }
                    }
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "${updated.orderType.cleanActivityType()}_updated",
                        entityType = "order",
                        entityId = updated.id,
                        entityLabel = updated.orderNumber,
                        title = updated.activityUpdatedTitle(),
                        body = updated.activitySummaryBody(),
                        tone = updated.status.dashboardTone(),
                    )
                }
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

    suspend fun updateOrderStatus(
        firebaseUser: VerifiedFirebaseUser,
        orderId: String,
        status: String,
        paidTotal: String? = null,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionChangeBookingStatus, "change booking status")
                val updated = connection.updateOrderStatus(access, orderId, status, paidTotal)
                if (updated != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = "${updated.orderType.cleanActivityType()}_status_updated",
                        entityType = "order",
                        entityId = updated.id,
                        entityLabel = updated.orderNumber,
                        title = updated.activityStatusTitle(),
                        body = updated.activityStatusBody(),
                        tone = updated.status.dashboardTone(),
                    )
                }
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

    suspend fun resolveOrderChangeRequest(
        firebaseUser: VerifiedFirebaseUser,
        orderId: String,
        changeRequestId: String,
        request: OrderChangeRequestResolveRequest,
    ): OrderResponse? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolveWorkspaceAccess(firebaseUser) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                access.requirePermission(PermissionEditSale, "edit sales or bookings")
                val cleanOrderId = orderId.cleanUuidOrNull() ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val cleanRequestId = changeRequestId.cleanUuidOrNull() ?: run {
                    connection.rollback()
                    return@withContext null
                }
                val changeRequest = connection.getOrderChangeRequestForUpdate(
                    workspaceId = access.workspaceId,
                    orderId = cleanOrderId,
                    requestId = cleanRequestId,
                ) ?: run {
                    connection.rollback()
                    return@withContext null
                }
                if (changeRequest.status != "pending") {
                    connection.commit()
                    return@withContext connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                }
                if (request.approved) {
                    val current = connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                        ?: run {
                            connection.rollback()
                            return@withContext null
                        }
                    if (!current.status.allowsBusinessOrderChangeApproval()) {
                        throw DashboardOrderValidationException(
                            code = "order_change_closed",
                            publicMessage = "This order is closed. Reopen it before approving customer changes.",
                        )
                    }
                    val requestedQuantityByItemId = changeRequest.items.associate {
                        it.orderItemId to it.requestedQuantity.decimalOrZero()
                    }
                    val nextItems = current.items.mapNotNull { item ->
                        val quantity = requestedQuantityByItemId[item.id] ?: item.quantity.decimalOrZero()
                        if (quantity <= BigDecimal.ZERO) {
                            null
                        } else {
                            OrderItemRequest(
                                productId = item.productId,
                                variantId = item.variantId,
                                description = item.description.ifBlank { item.productName ?: item.variantName ?: "Line item" },
                                quantity = quantity.decimalString(),
                                unitPrice = item.unitPrice,
                                taxRate = item.taxRate,
                            )
                        }
                    }
                    if (nextItems.isEmpty()) {
                        throw DashboardOrderValidationException(
                            code = "order_items_required",
                            publicMessage = "At least one item must remain on the order.",
                        )
                    }
                    connection.updateOrder(
                        access = access,
                        orderId = cleanOrderId,
                        request = current.toOrderRequestForChangeApproval(
                            items = nextItems,
                            paidTotal = changeRequest.requestedPaidTotal ?: current.paidTotal,
                            paymentMode = changeRequest.requestedPaymentMode ?: current.paymentMode,
                            businessNotes = request.notes,
                            changeRequest = changeRequest,
                        ),
                    )
                }
                connection.markOrderChangeRequestResolved(
                    workspaceId = access.workspaceId,
                    orderId = cleanOrderId,
                    requestId = cleanRequestId,
                    approved = request.approved,
                    businessNotes = request.notes.cleanOptional(),
                    userId = access.userId,
                )
                val updated = connection.getOrder(access.workspaceId, cleanOrderId, includeItems = true)
                if (updated != null) {
                    connection.insertWorkspaceActivity(
                        access = access,
                        activityType = if (request.approved) "order_change_approved" else "order_change_rejected",
                        entityType = "order",
                        entityId = updated.id,
                        entityLabel = updated.orderNumber,
                        title = if (request.approved) "Order change approved" else "Order change rejected",
                        body = "${updated.orderNumber} customer change request ${if (request.approved) "approved" else "rejected"}.",
                        tone = if (request.approved) "success" else "warning",
                    )
                }
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

    private fun Connection.findPublicCatalogWorkspace(workspaceId: String): PublicCatalogWorkspaceResponse? {
        val cleanWorkspaceId = workspaceId.cleanUuidOrNull() ?: return null
        val sql = """
            select
                bw.id::text,
                bw.business_name,
                bw.industry,
                bw.city,
                bw.currency,
                mc.whatsapp_display_number,
                bw.logo_file_name,
                bw.cover_file_name
            from business_workspaces bw
            left join meta_connections mc on mc.workspace_id = bw.id
            where bw.id = ?::uuid
              and bw.onboarding_completed_at is not null
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanWorkspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    PublicCatalogWorkspaceResponse(
                        id = result.getString("id"),
                        businessName = result.getString("business_name"),
                        industry = result.getString("industry"),
                        city = result.getString("city"),
                        currency = result.getString("currency") ?: "INR",
                        whatsappDisplayNumber = result.getString("whatsapp_display_number"),
                        logoUrl = result.getString("logo_file_name").toDashboardMediaUrl(),
                        coverUrl = result.getString("cover_file_name").toDashboardMediaUrl(),
                    )
                }
            }
        }
    }

    private fun Connection.resolvePublicWorkspaceAccess(workspaceId: String): DashboardWorkspaceAccess? {
        val cleanWorkspaceId = workspaceId.cleanUuidOrNull() ?: return null
        val sql = """
            select
                id::text as workspace_id,
                owner_user_id::text as user_id,
                currency,
                business_mode
            from business_workspaces
            where id = ?::uuid
              and onboarding_completed_at is not null
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanWorkspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    DashboardWorkspaceAccess(
                        userId = result.getString("user_id"),
                        workspaceId = result.getString("workspace_id"),
                        role = "public_catalog",
                        permissions = listOf("read_only"),
                        currency = result.getString("currency") ?: "INR",
                        businessMode = result.getString("business_mode") ?: "product_selling",
                        displayName = "Public catalog",
                    )
                }
            }
        }
    }

    private fun Connection.listPublicCatalogProducts(workspaceId: String): List<PublicCatalogProductResponse> {
        val sql = """
            select
                products.id::text,
                products.category_id::text,
                pc.name as category_name,
                products.name,
                products.item_type,
                products.description,
                products.unit,
                products.selling_price,
                products.currency,
                products.tax_rate,
                products.prices_include_tax,
                products.track_stock,
                products.stock_quantity,
                products.duration_minutes,
                products.booking_required,
                offer.id::text as offer_id,
                offer.name as offer_name,
                offer.description as offer_description,
                offer.discount_type,
                offer.discount_value,
                offer.discount_cap_amount,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = products.workspace_id
                      and pi.product_id = products.id::text
                      and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path
            from products
            left join product_categories pc on pc.id = products.category_id and pc.status = 'active'
            left join lateral (
                select po.*
                from product_offers po
                where po.workspace_id = products.workspace_id
                  and po.status = 'active'
                  and po.customer_id is null
                  and po.coupon_code is null
                  and (po.starts_at is null or po.starts_at <= now())
                  and (po.ends_at is null or po.ends_at >= current_date)
                  and (
                    po.applies_to = 'all'
                    or (po.applies_to = 'category' and po.category_id = products.category_id)
                    or (po.applies_to = 'product' and po.product_id = products.id)
                  )
                order by
                  case po.applies_to when 'product' then 0 when 'category' then 1 else 2 end,
                  case
                    when po.discount_type = 'fixed' then least(po.discount_value, products.selling_price)
                    else least(
                      products.selling_price,
                      coalesce(po.discount_cap_amount, products.selling_price),
                      products.selling_price * least(po.discount_value, 100) / 100
                    )
                  end desc,
                  po.created_at desc
                limit 1
            ) offer on true
            where products.workspace_id = ?::uuid
              and products.status = 'active'
              and (products.expiry_date is null or products.expiry_date >= current_date)
              and (
                products.track_stock = false
                or products.stock_quantity > 0
                or exists (
                    select 1
                    from product_variants pv
                    where pv.product_id = products.id
                      and pv.status = 'active'
                      and (pv.track_stock = false or pv.stock_quantity > 0)
                )
              )
            order by coalesce(pc.sort_order, 999), coalesce(pc.name, ''), products.name asc
            limit 200
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                val baseItems = buildList {
                    while (result.next()) {
                        val trackStock = result.getBoolean("track_stock")
                        val stockQuantity = result.getBigDecimal("stock_quantity") ?: BigDecimal.ZERO
                        add(
                            result.toPublicCatalogProductResponse(trackStock, stockQuantity),
                        )
                    }
                }
                attachPublicCatalogVariants(baseItems)
            }
        }
    }

    private fun Connection.listPublicCatalogCategories(workspaceId: String): List<PublicCatalogCategoryResponse> {
        val sql = """
            select id::text, name, sort_order
            from product_categories
            where workspace_id = ?::uuid and status = 'active'
            order by sort_order asc, name asc
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            PublicCatalogCategoryResponse(
                                id = result.getString("id"),
                                name = result.getString("name"),
                                sortOrder = result.getInt("sort_order"),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.listPublicCatalogPaymentMethods(workspaceId: String): List<PublicCatalogPaymentMethodResponse> {
        val sql = """
            select id::text, type, label, upi_id, payee_name, is_default
            from workspace_payment_methods
            where workspace_id = ?::uuid and status = 'active'
            order by is_default desc, created_at desc
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toPublicCatalogPaymentMethodResponse())
                }
            }
        }
    }

    private fun Connection.defaultPublicCatalogPaymentMethod(workspaceId: String): PublicCatalogPaymentMethodResponse? {
        val sql = """
            select id::text, type, label, upi_id, payee_name, is_default
            from workspace_payment_methods
            where workspace_id = ?::uuid and status = 'active' and type = 'upi'
            order by is_default desc, created_at desc
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toPublicCatalogPaymentMethodResponse() else null
            }
        }
    }

    private fun Connection.findCustomerByPhone(
        workspaceId: String,
        phoneNumber: String,
    ): CustomerResponse? {
        val phone = phoneNumber.cleanOptional() ?: return null
        val sql = """
            select *
            from customers
            where workspace_id = ?::uuid
              and status = 'active'
              and phone_number = ?
            order by created_at desc
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, phone)
            statement.executeQuery().use { result ->
                if (result.next()) result.toCustomerResponse() else null
            }
        }
    }

    private fun Connection.updatePublicCatalogCustomerContact(
        workspaceId: String,
        customer: CustomerResponse,
        customerName: String,
        phoneNumber: String?,
        email: String?,
    ): CustomerResponse {
        val cleanEmail = email.cleanOptional()?.lowercase()
        val cleanPhone = phoneNumber.cleanOptional()
        if (cleanEmail == null && cleanPhone == null && customerName.cleanOptional() == null) {
            return customer
        }
        val sql = """
            update customers
            set name = coalesce(?, name),
                phone_number = coalesce(phone_number, ?),
                email = coalesce(email, ?),
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setNullableString(1, customerName.cleanOptional())
            statement.setNullableString(2, cleanPhone)
            statement.setNullableString(3, cleanEmail)
            statement.setString(4, customer.id)
            statement.setString(5, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toCustomerResponse() else customer
            }
        }
    }

    private fun Connection.publicOrderItems(
        workspaceId: String,
        request: PublicCatalogOrderRequest,
    ): List<PublicCatalogPreparedItem> =
        request.items
            .take(50)
            .mapNotNull { requestedItem ->
                val productId = requestedItem.productId.cleanUuidOrNull() ?: return@mapNotNull null
                val quantity = requestedItem.quantity.decimalOrZero()
                    .takeIf { it > BigDecimal.ZERO }
                    ?: BigDecimal.ONE
                val product = publicOrderProduct(workspaceId, productId) ?: return@mapNotNull null
                val variantId = requestedItem.variantId.cleanUuidOrNull()
                val variant = variantId?.let { requestedVariantId ->
                    product.variants.firstOrNull { it.id == requestedVariantId && it.status == "active" }
                }
                if (variantId != null && variant == null) {
                    return@mapNotNull null
                }
                val packageComponents = variant?.components.orEmpty()
                if (packageComponents.isNotEmpty()) {
                    if (!packageComponents.packageComponentsAvailable(quantity)) {
                        return@mapNotNull null
                    }
                } else {
                    val productStock = product.stockQuantity.decimalOrZero()
                    val variantStock = variant?.stockQuantity?.decimalOrZero() ?: BigDecimal.ZERO
                    if (variant?.trackStock == true) {
                        if (variantStock <= BigDecimal.ZERO) {
                            return@mapNotNull null
                        }
                        if (quantity > variantStock) {
                            return@mapNotNull null
                        }
                    } else {
                        if (product.trackStock && productStock <= BigDecimal.ZERO) {
                            return@mapNotNull null
                        }
                        if (product.trackStock && quantity > productStock) {
                            return@mapNotNull null
                        }
                    }
                }
                val variantPrice = variant?.sellingPrice?.moneyOrZero()
                val variantDiscount = if (variantPrice != null && product.offer != null) {
                    discountAmount(
                        price = variantPrice,
                        discountType = product.offer.discountType,
                        discountValue = product.offer.discountValue.moneyOrZero(),
                        discountCapAmount = product.offer.discountCapAmount?.moneyOrZero(),
                    )
                } else {
                    BigDecimal.ZERO
                }
                val unitPrice = if (variantPrice != null) {
                    variantPrice.subtract(variantDiscount).coerceAtLeast(BigDecimal.ZERO).moneyString()
                } else {
                    product.offer?.finalPrice ?: product.sellingPrice
                }
                val description = if (variant == null) product.name else "${product.name} - ${variant.name}"
                PublicCatalogPreparedItem(
                    itemType = product.itemType.cleanItemType(),
                    orderItem = OrderItemRequest(
                        productId = product.id,
                        variantId = variant?.id,
                        description = description,
                        quantity = quantity.decimalString(),
                        unitPrice = unitPrice,
                        taxRate = product.taxRate,
                    ),
                    discountTotal = (variantDiscount.takeIf { variant != null } ?: (product.offer?.discountAmount ?: "0").moneyOrZero())
                        .multiply(quantity)
                        .scaled(),
                )
            }

    private fun List<ProductVariantComponentResponse>.packageComponentsAvailable(quantity: BigDecimal): Boolean =
        filter {
            it.status.cleanCatalogStatus() == "active" &&
                it.itemType.cleanItemType() == "product" &&
                it.trackStock
        }.all { component ->
            val required = quantity.multiply(component.quantity.decimalOrZero().takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE)
            component.stockQuantity.decimalOrZero() >= required
        }

    private fun ProductVariantResponse.publicCatalogAvailabilityQuantity(): BigDecimal? {
        if (components.isEmpty()) return null
        val trackedComponents = components.filter {
            it.status.cleanCatalogStatus() == "active" &&
                it.itemType.cleanItemType() == "product" &&
                it.trackStock
        }
        if (trackedComponents.isEmpty()) return BigDecimal("99")
        return trackedComponents.minOf { component ->
            val quantity = component.quantity.decimalOrZero().takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
            component.stockQuantity.decimalOrZero().divide(quantity, 0, RoundingMode.DOWN)
        }.coerceAtLeast(BigDecimal.ZERO)
    }

    private fun Connection.publicOrderProduct(
        workspaceId: String,
        productId: String,
    ): PublicCatalogProductResponse? {
        val sql = """
            select
                products.id::text,
                products.category_id::text,
                pc.name as category_name,
                products.name,
                products.item_type,
                products.description,
                products.unit,
                products.selling_price,
                products.currency,
                products.tax_rate,
                products.prices_include_tax,
                products.track_stock,
                products.stock_quantity,
                products.duration_minutes,
                products.booking_required,
                offer.id::text as offer_id,
                offer.name as offer_name,
                offer.description as offer_description,
                offer.discount_type,
                offer.discount_value,
                offer.discount_cap_amount,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = products.workspace_id
                      and pi.product_id = products.id::text
                      and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path
            from products
            left join product_categories pc on pc.id = products.category_id and pc.status = 'active'
            left join lateral (
                select po.*
                from product_offers po
                where po.workspace_id = products.workspace_id
                  and po.status = 'active'
                  and po.customer_id is null
                  and po.coupon_code is null
                  and (po.starts_at is null or po.starts_at <= now())
                  and (po.ends_at is null or po.ends_at >= current_date)
                  and (
                    po.applies_to = 'all'
                    or (po.applies_to = 'category' and po.category_id = products.category_id)
                    or (po.applies_to = 'product' and po.product_id = products.id)
                  )
                order by
                  case po.applies_to when 'product' then 0 when 'category' then 1 else 2 end,
                  case
                    when po.discount_type = 'fixed' then least(po.discount_value, products.selling_price)
                    else least(
                      products.selling_price,
                      coalesce(po.discount_cap_amount, products.selling_price),
                      products.selling_price * least(po.discount_value, 100) / 100
                    )
                  end desc,
                  po.created_at desc
                limit 1
            ) offer on true
            where products.id = ?::uuid
              and products.workspace_id = ?::uuid
              and products.status = 'active'
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, productId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    val trackStock = result.getBoolean("track_stock")
                    val stockQuantity = result.getBigDecimal("stock_quantity") ?: BigDecimal.ZERO
                    attachPublicCatalogVariants(
                        listOf(result.toPublicCatalogProductResponse(trackStock, stockQuantity)),
                    ).first()
                }
            }
        }
    }

    private fun Connection.resolveWorkspaceAccess(firebaseUser: VerifiedFirebaseUser): DashboardWorkspaceAccess? {
        val sql = """
            select
                au.id::text as user_id,
                bw.id::text as workspace_id,
                wm.role,
                wm.permissions,
                bw.currency,
                bw.business_mode,
                au.display_name,
                au.email,
                au.phone_number
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
                        displayName = result.getString("display_name"),
                        email = result.getString("email"),
                        phoneNumber = result.getString("phone_number"),
                    )
                }
            }
        }
    }

    private fun Connection.scalarInt(sql: String, vararg params: String): Int =
        prepareStatement(sql).use { statement ->
            statement.bindStringParams(params.toList())
            statement.executeQuery().use { result ->
                result.next()
                result.getInt(1)
            }
        }

    private fun ResultSet.getStringArray(column: String): List<String> =
        runCatching {
            val value = getArray(column)?.array ?: return@runCatching emptyList()
            when (value) {
                is Array<*> -> value.mapNotNull { it?.toString() }
                else -> emptyList()
            }
        }.getOrDefault(emptyList())

    private fun Connection.scalarDecimal(sql: String, vararg params: String): BigDecimal =
        prepareStatement(sql).use { statement ->
            statement.bindStringParams(params.toList())
            statement.executeQuery().use { result ->
                result.next()
                result.getBigDecimal(1) ?: BigDecimal.ZERO
            }
        }

    private fun Connection.supplierFinancialAmount(
        workspaceId: String,
        filters: DashboardQueryFilters,
        amountColumn: String,
        supplierAmountColumn: String,
        supplierDateExpression: String,
    ): BigDecimal {
        val params = mutableListOf(workspaceId, workspaceId)
        val sql = buildString {
            append(
                """
                select coalesce(sum(amount), 0)
                from (
                    select coalesce(s.$supplierAmountColumn, 0) as amount,
                           $supplierDateExpression as occurred_at
                    from suppliers s
                    where s.workspace_id = ?::uuid
                      and s.status = 'active'
                      and coalesce(s.$supplierAmountColumn, 0) <> 0
                    union all
                    select coalesce(sm.$amountColumn, 0) as amount,
                           sm.created_at as occurred_at
                    from stock_movements sm
                    where sm.workspace_id = ?::uuid
                      and sm.supplier_id is not null
                      and coalesce(sm.$amountColumn, 0) <> 0
                ) supplier_totals
                where true
                """.trimIndent(),
            )
            appendCreatedDateWhere(filters, params, "occurred_at")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                result.next()
                result.getBigDecimal(1) ?: BigDecimal.ZERO
            }
        }
    }

    private fun Connection.listDashboardRevenueSeries(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardRevenuePointResponse> {
        val dateTo = filters.cleanDateToOrNull()?.let(LocalDate::parse) ?: LocalDate.now()
        val dateFrom = filters.cleanDateFromOrNull()?.let(LocalDate::parse) ?: dateTo.minusDays(6)
        val startDate = if (dateFrom.isAfter(dateTo)) dateTo else dateFrom
        val sql = """
            select
                to_char(days.day, 'YYYY-MM-DD') as date,
                coalesce(sum(${effectivePaidTotalSql("o")}), 0) as amount,
                count(o.id)::int as orders_count
            from generate_series(
                ?::date,
                ?::date,
                interval '1 day'
            ) as days(day)
            left join orders o
              on o.workspace_id = ?::uuid
             and o.status <> 'cancelled'
             and coalesce(o.scheduled_at, o.created_at) >= days.day
             and coalesce(o.scheduled_at, o.created_at) < days.day + interval '1 day'
            group by days.day
            order by days.day asc
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, startDate.toString())
            statement.setString(2, dateTo.toString())
            statement.setString(3, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            DashboardRevenuePointResponse(
                                date = result.getString("date"),
                                amount = result.getBigDecimal("amount").moneyString(),
                                ordersCount = result.getInt("orders_count"),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.listOrderStatusBreakdown(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardBreakdownResponse> {
        val params = filters.withOrderDateParams(workspaceId)
        val sql = """
                select
                    o.status as key,
                    count(*)::int as item_count,
                    coalesce(sum(o.total), 0) as amount
                from orders o
                where o.workspace_id = ?::uuid
                  and o.status <> 'cancelled'
                  ${filters.orderDateWhereSql("o")}
                group by o.status
                order by item_count desc, o.status asc
            """.trimIndent()
        return listDashboardBreakdown(params, sql)
    }

    private fun Connection.listOrderTypeBreakdown(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardBreakdownResponse> {
        val params = filters.withOrderDateParams(workspaceId)
        val sql = """
                select
                    o.order_type as key,
                    count(*)::int as item_count,
                    coalesce(sum(o.total), 0) as amount
                from orders o
                where o.workspace_id = ?::uuid
                  and o.status <> 'cancelled'
                  ${filters.orderDateWhereSql("o")}
                group by o.order_type
                order by item_count desc, o.order_type asc
            """.trimIndent()
        return listDashboardBreakdown(params, sql)
    }

    private fun Connection.listDashboardBreakdown(
        params: List<String>,
        sql: String,
    ): List<DashboardBreakdownResponse> =
        prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val key = result.getString("key") ?: "unknown"
                        add(
                            DashboardBreakdownResponse(
                                key = key,
                                label = key.dashboardLabel(),
                                count = result.getInt("item_count"),
                                amount = result.getBigDecimal("amount").moneyString(),
                            ),
                        )
                    }
                }
            }
        }

    private fun Connection.listDashboardTopItems(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardTopItemResponse> {
        val params = filters.withOrderDateParams(workspaceId)
        val sql = """
            select
                oi.product_id::text,
                coalesce(p.name, oi.description) as name,
                coalesce(p.item_type, 'product') as item_type,
                coalesce(sum(oi.quantity), 0) as quantity,
                coalesce(sum(oi.line_total), 0) as amount,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = o.workspace_id
                      and pi.product_id = oi.product_id::text
                      and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path
            from order_items oi
            join orders o on o.id = oi.order_id
            left join products p on p.id = oi.product_id
            where o.workspace_id = ?::uuid
              and o.status <> 'cancelled'
              ${filters.orderDateWhereSql("o")}
            group by oi.product_id, coalesce(p.name, oi.description), coalesce(p.item_type, 'product'), image_storage_path
            order by coalesce(sum(oi.line_total), 0) desc, coalesce(sum(oi.quantity), 0) desc
            limit 5
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            DashboardTopItemResponse(
                                productId = result.getString("product_id"),
                                name = result.getString("name") ?: "Item",
                                itemType = result.getString("item_type") ?: "product",
                                quantity = result.getBigDecimal("quantity").decimalString(),
                                amount = result.getBigDecimal("amount").moneyString(),
                                imageUrl = result.getString("image_storage_path").toDashboardMediaUrl(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.listDashboardActivity(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardActivityResponse> {
        val recorded = listRecordedDashboardActivity(workspaceId, filters)
        if (recorded.isNotEmpty()) return recorded
        return listLegacyDashboardActivity(workspaceId, filters)
    }

    private fun Connection.listRecordedDashboardActivity(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardActivityResponse> {
        val params = filters.withCreatedDateParams(workspaceId)
        val sql = """
            select
                id::text,
                activity_type,
                title,
                body,
                created_at::text as occurred_at,
                tone,
                actor_user_id::text,
                actor_display_name,
                actor_email,
                actor_phone_number,
                actor_role
            from workspace_activity
            where workspace_id = ?::uuid
              ${filters.createdDateWhereSql("created_at")}
            order by created_at desc
            limit 12
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            DashboardActivityResponse(
                                id = result.getString("id"),
                                type = result.getString("activity_type") ?: "activity",
                                title = result.getString("title"),
                                body = result.getString("body"),
                                occurredAt = result.getString("occurred_at"),
                                tone = result.getString("tone") ?: "info",
                                performedByUserId = result.getString("actor_user_id"),
                                performedByDisplayName = result.getString("actor_display_name"),
                                performedByEmail = result.getString("actor_email"),
                                performedByPhoneNumber = result.getString("actor_phone_number"),
                                performedByRole = result.getString("actor_role"),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.listLegacyDashboardActivity(
        workspaceId: String,
        filters: DashboardQueryFilters,
    ): List<DashboardActivityResponse> {
        val params = filters.withOrderDateParams(workspaceId)
        val sql = """
            select
                o.id::text,
                o.order_type,
                o.order_number,
                o.status,
                o.total,
                o.currency,
                coalesce(c.name, 'Walk-in customer') as customer_name,
                o.created_at::text as occurred_at
            from orders o
            left join customers c on c.id = o.customer_id
            where o.workspace_id = ?::uuid
              ${filters.orderDateWhereSql("o")}
            order by o.created_at desc
            limit 8
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val orderType = result.getString("order_type") ?: "sale"
                        val status = result.getString("status") ?: "draft"
                        add(
                            DashboardActivityResponse(
                                id = result.getString("id"),
                                type = orderType,
                                title = "${orderType.dashboardLabel()} ${result.getString("order_number")}",
                                body = "${result.getString("customer_name")} · ${status.dashboardLabel()} · ${result.getString("currency")} ${result.getBigDecimal("total").moneyString()}",
                                occurredAt = result.getString("occurred_at"),
                                tone = status.dashboardTone(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.insertWorkspaceActivity(
        access: DashboardWorkspaceAccess,
        activityType: String,
        entityType: String,
        entityId: String? = null,
        entityLabel: String? = null,
        title: String,
        body: String,
        tone: String = "info",
        actorUserId: String? = access.userId,
        actorDisplayName: String? = access.displayName,
        actorEmail: String? = access.email,
        actorPhoneNumber: String? = access.phoneNumber,
        actorRole: String? = access.role,
    ) {
        val sql = """
            insert into workspace_activity (
                workspace_id, actor_user_id, actor_display_name, actor_email, actor_phone_number,
                actor_role, activity_type, entity_type, entity_id, entity_label, title, body, tone
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?,
                ?, ?, ?, ?::uuid, ?, ?, ?, ?
            )
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setNullableUuid(2, actorUserId)
            statement.setNullableString(3, actorDisplayName.cleanOptional())
            statement.setNullableString(4, actorEmail.cleanOptional()?.lowercase())
            statement.setNullableString(5, actorPhoneNumber.cleanOptional())
            statement.setNullableString(6, actorRole.cleanOptional())
            statement.setString(7, activityType.cleanActivityType())
            statement.setString(8, entityType.cleanActivityType())
            statement.setNullableUuid(9, entityId)
            statement.setNullableString(10, entityLabel.cleanOptional())
            statement.setString(11, title.cleanName())
            statement.setString(12, body.cleanOptional() ?: "Workspace action completed.")
            statement.setString(13, tone.cleanActivityTone())
            statement.executeUpdate()
        }
    }

    private fun Connection.listDashboardTasks(workspaceId: String): List<DashboardTaskResponse> = buildList {
        val lowStockCount = scalarInt(
            """
            select count(*)
            from products
            where workspace_id = ?::uuid
              and status = 'active'
              and item_type = 'product'
              and track_stock = true
              and stock_quantity <= reorder_level
            """.trimIndent(),
            workspaceId,
        )
        if (lowStockCount > 0) {
            add(
                DashboardTaskResponse(
                    id = "low_stock",
                    title = "Review low stock",
                    body = "$lowStockCount stocked items are at or below reorder level.",
                    action = "products.low_stock",
                    priority = "high",
                    tone = "warning",
                    count = lowStockCount,
                ),
            )
        }

        val expiredProductCount = scalarInt(
            """
            select count(*)
            from products
            where workspace_id = ?::uuid
              and status = 'active'
              and item_type = 'product'
              and expiry_date is not null
              and expiry_date < current_date
            """.trimIndent(),
            workspaceId,
        )
        if (expiredProductCount > 0) {
            add(
                DashboardTaskResponse(
                    id = "expired_products",
                    title = "Remove expired products",
                    body = "$expiredProductCount products are past their expiry date.",
                    action = "products.expiry",
                    priority = "high",
                    tone = "danger",
                    count = expiredProductCount,
                ),
            )
        }

        val expiringProductCount = scalarInt(
            """
            select count(*)
            from products
            where workspace_id = ?::uuid
              and status = 'active'
              and item_type = 'product'
              and expiry_date is not null
              and expiry_date >= current_date
              and expiry_date <= current_date + interval '7 days'
            """.trimIndent(),
            workspaceId,
        )
        if (expiringProductCount > 0) {
            add(
                DashboardTaskResponse(
                    id = "expiring_products",
                    title = "Check product expiry",
                    body = "$expiringProductCount products expire within 7 days.",
                    action = "products.expiry",
                    priority = "normal",
                    tone = "warning",
                    count = expiringProductCount,
                ),
            )
        }

        val draftOrders = scalarInt(
            "select count(*) from orders where workspace_id = ?::uuid and status = 'draft'",
            workspaceId,
        )
        if (draftOrders > 0) {
            add(
                DashboardTaskResponse(
                    id = "draft_orders",
                    title = "Confirm draft orders",
                    body = "$draftOrders orders are waiting for review.",
                    action = "orders.draft",
                    priority = "normal",
                    tone = "info",
                    count = draftOrders,
                ),
            )
        }

        val failedNotifications = scalarInt(
            "select count(*) from notification_events where workspace_id = ?::uuid and failure_count > 0",
            workspaceId,
        )
        if (failedNotifications > 0) {
            add(
                DashboardTaskResponse(
                    id = "notification_failures",
                    title = "Check notifications",
                    body = "$failedNotifications notifications reported delivery failures.",
                    action = "notifications.review",
                    priority = "normal",
                    tone = "warning",
                    count = failedNotifications,
                ),
            )
        }
    }

    private fun Connection.listDashboardNotificationPreview(
        workspaceId: String,
        limit: Int = 5,
    ): List<DashboardNotificationPreviewResponse> {
        val sql = """
            select
                id::text,
                workspace_id::text,
                order_id::text,
                event_type,
                coalesce(nullif(payload ->> 'channel', ''), 'catalog_orders') as channel,
                title,
                body,
                status,
                created_at::text
            from notification_events
            where workspace_id = ?::uuid
            order by created_at desc
            limit ?
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setInt(2, limit)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val status = result.getString("status") ?: "queued"
                        add(
                            DashboardNotificationPreviewResponse(
                                id = result.getString("id"),
                                eventType = result.getString("event_type") ?: "notification",
                                channel = result.getString("channel") ?: "catalog_orders",
                                orderId = result.getString("order_id"),
                                workspaceId = result.getString("workspace_id"),
                                title = result.getString("title"),
                                body = result.getString("body"),
                                createdAt = result.getString("created_at"),
                                tone = status.dashboardTone(),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun Connection.listCustomers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<CustomerResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select *, count(*) over()::int as total_count
                from customers
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(
                    """
                     and (
                        name ilike ?
                        or coalesce(phone_number, '') ilike ?
                        or coalesce(email, '') ilike ?
                        or coalesce(tax_number, '') ilike ?
                        or coalesce(city, '') ilike ?
                        or coalesce(notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(6) { params.add(search.ilikePattern()) }
            }
            appendCreatedDateWhere(filters, params, "created_at")
            append(" order by created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toCustomerResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertCustomer(workspaceId: String, request: CustomerRequest): CustomerResponse {
        val sql = """
            insert into customers (
                workspace_id, name, phone_number, email, tax_number, address_line, city, region,
                country, postal_code, notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
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
            set name = ?, phone_number = ?, email = ?, tax_number = ?, address_line = ?, city = ?,
                region = ?, country = ?, postal_code = ?, notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindCustomerRequest(request, startIndex = 1)
            statement.setString(11, customerId)
            statement.setString(12, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toCustomerResponse() else null
            }
        }
    }

    private fun Connection.updateCustomerBillingDetails(
        workspaceId: String,
        customerId: String,
        request: OrderRequest,
    ) {
        if (!request.hasCustomerBillingDetails()) return
        val sql = """
            update customers
            set name = coalesce(?, name),
                phone_number = coalesce(?, phone_number),
                email = coalesce(?, email),
                tax_number = coalesce(?, tax_number),
                address_line = coalesce(?, address_line),
                city = coalesce(?, city),
                region = coalesce(?, region),
                country = coalesce(?, country),
                postal_code = coalesce(?, postal_code),
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setNullableString(1, request.customerName?.cleanOptional())
            statement.setNullableString(2, request.customerPhoneNumber?.cleanOptional())
            statement.setNullableString(3, request.customerEmail?.cleanOptional()?.lowercase())
            statement.setNullableString(4, request.customerTaxNumber?.cleanOptionalUpper())
            statement.setNullableString(5, request.customerAddressLine?.cleanOptional())
            statement.setNullableString(6, request.customerCity?.cleanOptional())
            statement.setNullableString(7, request.customerRegion?.cleanOptional())
            statement.setNullableString(8, request.customerCountry?.cleanOptional())
            statement.setNullableString(9, request.customerPostalCode?.cleanOptional())
            statement.setString(10, customerId)
            statement.setString(11, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.listSuppliers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<SupplierResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select
                    s.id,
                    s.workspace_id,
                    s.name,
                    s.phone_number,
                    s.email,
                    s.tax_number,
                    s.address_line,
                    s.payment_terms,
                    s.payment_mode,
                    s.payment_reference,
                    (coalesce(s.payable_total, 0) + coalesce(batch_totals.payable_total, 0)) as payable_total,
                    (coalesce(s.paid_total, 0) + coalesce(batch_totals.paid_total, 0)) as paid_total,
                    s.currency,
                    s.last_payment_at,
                    s.notes,
                    s.status,
                    s.created_at,
                    s.updated_at,
                    count(*) over()::int as total_count
                from suppliers s
                left join (
                    select
                        supplier_id,
                        sum(coalesce(payable_amount, 0)) as payable_total,
                        sum(coalesce(paid_amount, 0)) as paid_total
                    from stock_movements
                    where workspace_id = ?::uuid
                      and supplier_id is not null
                      and (coalesce(payable_amount, 0) <> 0 or coalesce(paid_amount, 0) <> 0)
                    group by supplier_id
                ) batch_totals on batch_totals.supplier_id = s.id
                where s.workspace_id = ?::uuid and s.status = 'active'
                """.trimIndent(),
            )
            params.add(workspaceId)
            if (search != null) {
                append(
                    """
                     and (
                        s.name ilike ?
                        or coalesce(s.phone_number, '') ilike ?
                        or coalesce(s.email, '') ilike ?
                        or coalesce(s.tax_number, '') ilike ?
                        or coalesce(s.payment_terms, '') ilike ?
                        or coalesce(s.payment_mode, '') ilike ?
                        or coalesce(s.payment_reference, '') ilike ?
                        or coalesce(s.notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(8) { params.add(search.ilikePattern()) }
            }
            appendCreatedDateWhere(filters, params, "s.created_at")
            append(" order by s.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toSupplierResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertSupplier(workspaceId: String, request: SupplierRequest): SupplierResponse {
        val sql = """
            insert into suppliers (
                workspace_id, name, phone_number, email, tax_number, address_line,
                payment_terms, payment_mode, payment_reference, payable_total, paid_total,
                currency, last_payment_at, notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::timestamptz, ?, now())
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
                address_line = ?, payment_terms = ?, payment_mode = ?, payment_reference = ?,
                payable_total = ?, paid_total = ?, currency = ?, last_payment_at = ?::timestamptz,
                notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindSupplierRequest(request, startIndex = 1)
            statement.setString(14, supplierId)
            statement.setString(15, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toSupplierResponse() else null
            }
        }
    }

    private fun Connection.listProductCategories(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductCategoryResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val itemType = filters.itemType.cleanItemTypeFilter()
        val sql = buildString {
            append(
                """
                select id::text, name, item_type, sort_order, status, created_at::text, updated_at::text,
                    count(*) over()::int as total_count
                from product_categories
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and name ilike ?")
                params.add(search.ilikePattern())
            }
            if (itemType != null) {
                append(" and (item_type = ? or item_type = 'all')")
                params.add(itemType)
            }
            appendCreatedDateWhere(filters, params, "created_at")
            append(" order by case when item_type = 'all' then 0 else 1 end, sort_order asc, name asc")
            append(" limit ${filters.limit.sanitizedLimit()} offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toProductCategoryResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertProductCategory(
        workspaceId: String,
        request: ProductCategoryRequest,
    ): ProductCategoryResponse {
        val name = request.name.cleanName()
        val itemType = request.itemType.cleanCategoryItemType()
        prepareStatement(
            """
            select id::text, name, item_type, sort_order, status, created_at::text, updated_at::text
            from product_categories
            where workspace_id = ?::uuid
              and item_type = ?
              and lower(name) = lower(?)
            order by case when status = 'active' then 0 else 1 end, updated_at desc
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, itemType)
            statement.setString(3, name)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    return updateProductCategorySort(
                        workspaceId = workspaceId,
                        categoryId = result.getString("id"),
                        sortOrder = request.sortOrder,
                    )
                }
            }
        }
        val sql = """
            insert into product_categories (workspace_id, name, item_type, sort_order, updated_at)
            values (?::uuid, ?, ?, ?, now())
            returning id::text, name, item_type, sort_order, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, name)
            statement.setString(3, itemType)
            statement.setInt(4, request.sortOrder.coerceIn(0, 999))
            statement.executeQuery().use { result ->
                result.next()
                result.toProductCategoryResponse()
            }
        }
    }

    private fun Connection.updateProductCategorySort(
        workspaceId: String,
        categoryId: String,
        sortOrder: Int,
    ): ProductCategoryResponse {
        val sql = """
            update product_categories
            set sort_order = ?, status = 'active', updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning id::text, name, item_type, sort_order, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setInt(1, sortOrder.coerceIn(0, 999))
            statement.setString(2, categoryId)
            statement.setString(3, workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                result.toProductCategoryResponse()
            }
        }
    }

    private fun Connection.listProductOffers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductOfferResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val categoryId = filters.categoryId.cleanUuidOrNull()
        val sql = buildString {
            append(
                """
                select
                    po.id::text,
                    po.applies_to,
                    po.product_id::text,
                    p.name as product_name,
                    po.category_id::text,
                    pc.name as category_name,
                    po.customer_id::text,
                    c.name as customer_name,
                    po.name,
                    po.description,
                    po.discount_type,
                    po.discount_value,
                    po.discount_cap_amount,
                    po.coupon_code,
                    po.starts_at::text,
                    po.ends_at::text,
                    po.status,
                    po.created_at::text,
                    po.updated_at::text,
                    count(*) over()::int as total_count
                from product_offers po
                left join products p on p.id = po.product_id
                left join product_categories pc on pc.id = po.category_id
                left join customers c on c.id = po.customer_id
                where po.workspace_id = ?::uuid and po.status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and (po.name ilike ? or coalesce(p.name, '') ilike ? or coalesce(pc.name, '') ilike ? or coalesce(c.name, '') ilike ? or coalesce(po.coupon_code, '') ilike ?)")
                repeat(5) { params.add(search.ilikePattern()) }
            }
            if (categoryId != null) {
                append(" and (po.category_id = ?::uuid or p.category_id = ?::uuid)")
                params.add(categoryId)
                params.add(categoryId)
            }
            appendCreatedDateWhere(filters, params, "po.created_at")
            append(" order by po.created_at desc limit ${filters.limit.sanitizedLimit()} offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toProductOfferResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertProductOffer(
        workspaceId: String,
        request: ProductOfferRequest,
    ): ProductOfferResponse {
        val appliesTo = request.appliesTo.cleanOfferScope()
        val discountType = request.discountType.cleanDiscountType()
        val capAmount = request.discountCapAmount.cleanOfferCapAmount(discountType)
        val couponCode = request.couponCode.cleanCouponCode()
        val sql = """
            insert into product_offers (
                workspace_id, product_id, category_id, customer_id, applies_to, name, description,
                discount_type, discount_value, discount_cap_amount, coupon_code, starts_at, ends_at, updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::timestamptz, ?::timestamptz, now())
            returning
                id::text,
                applies_to,
                product_id::text,
                (select name from products where id = product_offers.product_id) as product_name,
                category_id::text,
                (select name from product_categories where id = product_offers.category_id) as category_name,
                customer_id::text,
                (select name from customers where id = product_offers.customer_id) as customer_name,
                name,
                description,
                discount_type,
                discount_value,
                discount_cap_amount,
                coupon_code,
                starts_at::text,
                ends_at::text,
                status,
                created_at::text,
                updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setNullableUuid(2, if (appliesTo == "product") request.productId else null)
            statement.setNullableUuid(3, if (appliesTo == "category") request.categoryId else null)
            statement.setNullableUuid(4, request.customerId)
            statement.setString(5, appliesTo)
            statement.setString(6, request.name.cleanName())
            statement.setNullableString(7, request.description?.cleanOptional())
            statement.setString(8, discountType)
            statement.setBigDecimal(9, request.discountValue.decimalOrZero().coerceAtLeast(BigDecimal.ZERO))
            if (capAmount == null) statement.setNull(10, java.sql.Types.NUMERIC) else statement.setBigDecimal(10, capAmount)
            statement.setNullableString(11, couponCode)
            statement.setNullableString(12, request.startsAt?.cleanOptional())
            statement.setNullableString(13, request.endsAt?.cleanOptional())
            statement.executeQuery().use { result ->
                result.next()
                result.toProductOfferResponse()
            }
        }
    }

    private fun Connection.updateProductOffer(
        workspaceId: String,
        offerId: String,
        request: ProductOfferRequest,
    ): ProductOfferResponse? {
        val appliesTo = request.appliesTo.cleanOfferScope()
        val discountType = request.discountType.cleanDiscountType()
        val capAmount = request.discountCapAmount.cleanOfferCapAmount(discountType)
        val couponCode = request.couponCode.cleanCouponCode()
        val sql = """
            update product_offers
            set product_id = ?::uuid, category_id = ?::uuid, customer_id = ?::uuid, applies_to = ?, name = ?, description = ?,
                discount_type = ?, discount_value = ?, discount_cap_amount = ?, coupon_code = ?,
                starts_at = ?::timestamptz, ends_at = ?::timestamptz,
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning
                id::text,
                applies_to,
                product_id::text,
                (select name from products where id = product_offers.product_id) as product_name,
                category_id::text,
                (select name from product_categories where id = product_offers.category_id) as category_name,
                customer_id::text,
                (select name from customers where id = product_offers.customer_id) as customer_name,
                name,
                description,
                discount_type,
                discount_value,
                discount_cap_amount,
                coupon_code,
                starts_at::text,
                ends_at::text,
                status,
                created_at::text,
                updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setNullableUuid(1, if (appliesTo == "product") request.productId else null)
            statement.setNullableUuid(2, if (appliesTo == "category") request.categoryId else null)
            statement.setNullableUuid(3, request.customerId)
            statement.setString(4, appliesTo)
            statement.setString(5, request.name.cleanName())
            statement.setNullableString(6, request.description?.cleanOptional())
            statement.setString(7, discountType)
            statement.setBigDecimal(8, request.discountValue.decimalOrZero().coerceAtLeast(BigDecimal.ZERO))
            if (capAmount == null) statement.setNull(9, java.sql.Types.NUMERIC) else statement.setBigDecimal(9, capAmount)
            statement.setNullableString(10, couponCode)
            statement.setNullableString(11, request.startsAt?.cleanOptional())
            statement.setNullableString(12, request.endsAt?.cleanOptional())
            statement.setString(13, offerId)
            statement.setString(14, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toProductOfferResponse() else null
            }
        }
    }

    private fun Connection.listPaymentMethods(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<WorkspacePaymentMethodResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text,
                    count(*) over()::int as total_count
                from workspace_payment_methods
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and (label ilike ? or coalesce(upi_id, '') ilike ? or coalesce(payee_name, '') ilike ?)")
                repeat(3) { params.add(search.ilikePattern()) }
            }
            appendCreatedDateWhere(filters, params, "created_at")
            append(" order by is_default desc, created_at desc limit ${filters.limit.sanitizedLimit()} offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toWorkspacePaymentMethodResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertPaymentMethod(
        workspaceId: String,
        request: WorkspacePaymentMethodRequest,
    ): WorkspacePaymentMethodResponse {
        if (request.isDefault) {
            prepareStatement(
                "update workspace_payment_methods set is_default = false, updated_at = now() where workspace_id = ?::uuid and status = 'active'",
            ).use { statement ->
                statement.setString(1, workspaceId)
                statement.executeUpdate()
            }
        }
        val sql = """
            insert into workspace_payment_methods (workspace_id, type, label, upi_id, payee_name, is_default, updated_at)
            values (?::uuid, ?, ?, ?, ?, ?, now())
            returning id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, request.type.cleanPaymentMethodType())
            statement.setString(3, request.label.cleanName())
            statement.setNullableString(4, request.upiId.cleanUpiId())
            statement.setNullableString(5, request.payeeName?.cleanOptional())
            statement.setBoolean(6, request.isDefault)
            statement.executeQuery().use { result ->
                result.next()
                result.toWorkspacePaymentMethodResponse()
            }
        }
    }

    private fun Connection.updatePaymentMethod(
        workspaceId: String,
        paymentMethodId: String,
        request: WorkspacePaymentMethodRequest,
    ): WorkspacePaymentMethodResponse? {
        val cleanPaymentMethodId = paymentMethodId.cleanUuidOrNull() ?: return null
        if (request.isDefault) {
            clearPaymentMethodDefaults(workspaceId, exceptPaymentMethodId = cleanPaymentMethodId)
        }
        val sql = """
            update workspace_payment_methods
            set type = ?, label = ?, upi_id = ?, payee_name = ?, is_default = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, request.type.cleanPaymentMethodType())
            statement.setString(2, request.label.cleanName())
            statement.setNullableString(3, request.upiId.cleanUpiId())
            statement.setNullableString(4, request.payeeName?.cleanOptional())
            statement.setBoolean(5, request.isDefault)
            statement.setString(6, cleanPaymentMethodId)
            statement.setString(7, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toWorkspacePaymentMethodResponse() else null
            }
        }
    }

    private fun Connection.setDefaultPaymentMethod(
        workspaceId: String,
        paymentMethodId: String,
    ): WorkspacePaymentMethodResponse? {
        val cleanPaymentMethodId = paymentMethodId.cleanUuidOrNull() ?: return null
        clearPaymentMethodDefaults(workspaceId, exceptPaymentMethodId = cleanPaymentMethodId)
        val sql = """
            update workspace_payment_methods
            set is_default = true, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active' and type = 'upi'
            returning id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanPaymentMethodId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toWorkspacePaymentMethodResponse() else null
            }
        }
    }

    private fun Connection.deletePaymentMethod(
        workspaceId: String,
        paymentMethodId: String,
    ): WorkspacePaymentMethodResponse? {
        val cleanPaymentMethodId = paymentMethodId.cleanUuidOrNull() ?: return null
        val sql = """
            update workspace_payment_methods
            set status = 'deleted', is_default = false, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanPaymentMethodId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toWorkspacePaymentMethodResponse() else null
            }
        }
    }

    private fun Connection.clearPaymentMethodDefaults(
        workspaceId: String,
        exceptPaymentMethodId: String? = null,
    ) {
        val sql = buildString {
            append("update workspace_payment_methods set is_default = false, updated_at = now() where workspace_id = ?::uuid and status = 'active'")
            if (exceptPaymentMethodId != null) append(" and id <> ?::uuid")
        }
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            if (exceptPaymentMethodId != null) statement.setString(2, exceptPaymentMethodId)
            statement.executeUpdate()
        }
    }

    private fun Connection.ensureOneDefaultPaymentMethod(workspaceId: String) {
        val hasDefault = prepareStatement(
            "select 1 from workspace_payment_methods where workspace_id = ?::uuid and status = 'active' and type = 'upi' and is_default = true limit 1",
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { it.next() }
        }
        if (hasDefault) return
        prepareStatement(
            """
            update workspace_payment_methods
            set is_default = true, updated_at = now()
            where id = (
                select id
                from workspace_payment_methods
                where workspace_id = ?::uuid and status = 'active' and type = 'upi'
                order by created_at desc
                limit 1
            )
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.listProducts(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<ProductResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val supplierId = filters.supplierId.cleanUuidOrNull()
        val categoryId = filters.categoryId.cleanUuidOrNull()
        val barcode = filters.barcode.cleanSearchTerm()
        val itemType = filters.itemType.cleanItemTypeFilter()
        val sql = buildString {
            append(
                """
                select p.*, s.name as supplier_name, pc.name as category_name,
                    count(*) over()::int as total_count,
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
                left join suppliers s on s.id = p.supplier_id
                left join product_categories pc on pc.id = p.category_id
                where p.workspace_id = ?::uuid and p.status <> 'archived'
                """.trimIndent(),
            )
            if (search != null) {
                append(
                    """
                     and (
                        p.name ilike ?
                        or coalesce(p.sku, '') ilike ?
                        or coalesce(p.barcode, '') ilike ?
                        or coalesce(p.description, '') ilike ?
                        or coalesce(s.name, '') ilike ?
                        or coalesce(pc.name, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(6) { params.add(search.ilikePattern()) }
            }
            if (supplierId != null) {
                append(" and p.supplier_id = ?::uuid")
                params.add(supplierId)
            }
            if (categoryId != null) {
                append(" and p.category_id = ?::uuid")
                params.add(categoryId)
            }
            if (itemType != null) {
                append(" and p.item_type = ?")
                params.add(itemType)
            }
            if (barcode != null) {
                append(" and coalesce(p.barcode, '') ilike ?")
                params.add(barcode.ilikePattern())
            }
            if (filters.lowStockOnly) {
                append(" and p.track_stock = true and p.stock_quantity <= p.reorder_level")
            }
            appendCreatedDateWhere(filters, params, "p.created_at")
            append(" order by p.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val baseItems = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toProductResponse())
                    }
                }
                val items = attachProductVariants(baseItems)
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertProduct(
        access: DashboardWorkspaceAccess,
        request: ProductRequest,
    ): ProductResponse {
        val clientRequestId = request.clientRequestId.cleanClientRequestId()
        if (clientRequestId != null) {
            lockProductClientRequest(access.workspaceId, clientRequestId)
            getProductByClientRequestId(access.workspaceId, clientRequestId)?.let { existingProduct ->
                return existingProduct
            }
        }
        val categoryId = resolveProductCategoryId(access, request)
        val supplierId = resolveSupplierId(access, request.supplierId)
        val sql = """
            insert into products (
                workspace_id, supplier_id, category_id, name, item_type, sku, barcode, description, unit,
                selling_price, cost_price, currency, tax_rate, prices_include_tax,
                stock_quantity, reorder_level, track_stock, duration_minutes, booking_required, expiry_date, status,
                client_request_id, updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::date, ?, ?, now())
            returning *, null::text as supplier_name, null::text as category_name, null::text as image_storage_path
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.bindProductRequest(access, request, supplierId, categoryId, startIndex = 2)
            statement.setNullableString(22, clientRequestId)
            statement.executeQuery().use { result ->
                result.next()
                val product = result.toProductResponse()
                replaceProductVariants(access, product.id, request)
                attachProductVariants(listOf(product)).first()
            }
        }
    }

    private fun Connection.lockProductClientRequest(
        workspaceId: String,
        clientRequestId: String,
    ) {
        prepareStatement("select pg_advisory_xact_lock(hashtext(?), hashtext(?))").use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, "product:$clientRequestId")
            statement.execute()
        }
    }

    private fun Connection.getProductByClientRequestId(
        workspaceId: String,
        clientRequestId: String,
    ): ProductResponse? {
        val sql = """
            select p.*, s.name as supplier_name, pc.name as category_name,
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
            left join suppliers s on s.id = p.supplier_id
            left join product_categories pc on pc.id = p.category_id
            where p.workspace_id = ?::uuid
              and p.client_request_id = ?
              and p.status <> 'archived'
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, clientRequestId)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    attachProductVariants(listOf(result.toProductResponse())).firstOrNull()
                } else {
                    null
                }
            }
        }
    }

    private fun Connection.productExists(
        workspaceId: String,
        sku: String?,
        barcode: String?,
    ): Boolean {
        val cleanSku = sku.cleanOptionalUpper()
        val cleanBarcode = barcode.cleanOptional()
        if (cleanSku == null && cleanBarcode == null) return false
        val clauses = buildList {
            if (cleanSku != null) add("upper(sku) = ?")
            if (cleanBarcode != null) add("barcode = ?")
        }
        val sql = """
            select 1
            from products
            where workspace_id = ?::uuid
              and status = 'active'
              and (${clauses.joinToString(" or ")})
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            var index = 1
            statement.setString(index++, workspaceId)
            if (cleanSku != null) statement.setString(index++, cleanSku)
            if (cleanBarcode != null) statement.setString(index, cleanBarcode)
            statement.executeQuery().use { result -> result.next() }
        }
    }

    private fun Connection.findOrCreateSupplier(
        workspaceId: String,
        supplierName: String,
    ): String {
        prepareStatement(
            """
            select id::text
            from suppliers
            where workspace_id = ?::uuid
              and status = 'active'
              and lower(name) = lower(?)
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, supplierName.cleanName())
            statement.executeQuery().use { result ->
                if (result.next()) return result.getString("id")
            }
        }
        return insertSupplier(
            workspaceId = workspaceId,
            request = SupplierRequest(name = supplierName),
        ).id
    }

    private fun Connection.resolveSupplierId(
        access: DashboardWorkspaceAccess,
        supplierId: String?,
    ): String? {
        val cleanSupplierId = supplierId.cleanUuidOrNull() ?: return null
        val sql = """
            select id::text
            from suppliers
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            limit 1
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanSupplierId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("id") else null
            }
        }
    }

    private fun Connection.resolveProductCategoryId(
        access: DashboardWorkspaceAccess,
        request: ProductRequest,
    ): String? {
        val itemType = request.itemType.cleanItemType()
        request.categoryId.cleanUuidOrNull()?.let { categoryId ->
            if (productCategoryMatches(access.workspaceId, categoryId, itemType)) return categoryId
            throw DashboardOrderValidationException(
                code = "product_category_invalid",
                publicMessage = "Choose a category for this item type.",
            )
        }
        val categoryName = request.categoryName?.cleanOptional() ?: return null
        return findOrCreateProductCategory(
            workspaceId = access.workspaceId,
            categoryName = categoryName,
            itemType = itemType,
        )
    }

    private fun Connection.productCategoryMatches(
        workspaceId: String,
        categoryId: String,
        itemType: String,
    ): Boolean =
        prepareStatement(
            """
            select 1
            from product_categories
            where id = ?::uuid
              and workspace_id = ?::uuid
              and status = 'active'
              and (item_type = ? or item_type = 'all')
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, categoryId)
            statement.setString(2, workspaceId)
            statement.setString(3, itemType.cleanItemType())
            statement.executeQuery().use { result -> result.next() }
        }

    private fun Connection.findOrCreateProductCategory(
        workspaceId: String,
        categoryName: String,
        itemType: String,
    ): String {
        val cleanName = categoryName.cleanName()
        val cleanType = itemType.cleanCategoryItemType()
        prepareStatement(
            """
            select id::text
            from product_categories
            where workspace_id = ?::uuid
              and status = 'active'
              and lower(name) = lower(?)
              and (item_type = ? or item_type = 'all')
            order by case when item_type = ? then 0 else 1 end, sort_order asc, name asc
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, cleanName)
            statement.setString(3, cleanType)
            statement.setString(4, cleanType)
            statement.executeQuery().use { result ->
                if (result.next()) return result.getString("id")
            }
        }
        return insertProductCategory(
            workspaceId = workspaceId,
            request = ProductCategoryRequest(
                name = cleanName,
                itemType = cleanType,
            ),
        ).id
    }

    private fun Connection.updateProduct(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: ProductRequest,
    ): ProductResponse? {
        val categoryId = resolveProductCategoryId(access, request)
        val supplierId = resolveSupplierId(access, request.supplierId)
        val sql = """
            update products
            set supplier_id = ?::uuid, category_id = ?::uuid, name = ?, item_type = ?, sku = ?, barcode = ?, description = ?,
                unit = ?, selling_price = ?, cost_price = ?, currency = ?, tax_rate = ?,
                prices_include_tax = ?, stock_quantity = ?, reorder_level = ?, track_stock = ?,
                duration_minutes = ?, booking_required = ?, expiry_date = ?::date, status = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *, null::text as supplier_name, null::text as category_name, null::text as image_storage_path
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindProductRequest(access, request, supplierId, categoryId, startIndex = 1)
            statement.setString(21, productId)
            statement.setString(22, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    val product = result.toProductResponse()
                    replaceProductVariants(access, product.id, request)
                    attachProductVariants(listOf(product)).first()
                } else {
                    null
                }
            }
        }
    }

    private fun Connection.replaceProductVariants(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: ProductRequest,
    ) {
        val itemType = request.itemType.cleanItemType()
        val retainedVariantIds = request.variants
            .filter { it.name.cleanOptional() != null }
            .distinctBy { it.name.cleanName().lowercase() }
            .take(40)
            .mapIndexed { index, variant ->
                val values = variant.toProductVariantValues(request, itemType, index)
                val existingVariantId = findExistingProductVariantId(
                    access = access,
                    productId = productId,
                    requestedVariantId = variant.id,
                    cleanName = values.name,
                )
                val variantId = if (existingVariantId == null) {
                    insertProductVariant(access, productId, values)
                } else {
                    updateProductVariant(access, productId, existingVariantId, values)
                }
                deleteProductVariantComponents(access, variantId)
                insertProductVariantComponents(
                    access = access,
                    packageVariantId = variantId,
                    parentItemType = itemType,
                    components = variant.components,
                )
                variantId
            }
        archiveRemovedProductVariants(access, productId, retainedVariantIds)
    }

    private fun ProductVariantRequest.toProductVariantValues(
        request: ProductRequest,
        itemType: String,
        sortOrder: Int,
    ): ProductVariantValues {
        val sellingPrice = sellingPrice?.cleanOptional()?.moneyOrZero() ?: request.sellingPrice.moneyOrZero()
        val costPrice = costPrice?.cleanOptional()?.moneyOrZero() ?: request.costPrice.moneyOrZero()
        val stockQuantity = if (itemType == "product") {
            stockQuantity?.cleanOptional()?.decimalOrZero() ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }
        val variantTracksStock = itemType == "product" && trackStock
        return ProductVariantValues(
            name = name.cleanName(),
            sku = sku?.cleanOptionalUpper(),
            barcode = barcode?.cleanOptional(),
            sellingPrice = sellingPrice.scaled(),
            costPrice = costPrice.scaled(),
            stockQuantity = stockQuantity.scaled(),
            trackStock = variantTracksStock,
            durationMinutes = durationMinutes?.takeIf { it > 0 }?.coerceAtMost(1440),
            includedQuantity = includedQuantity.coerceAtLeast(1).coerceAtMost(999),
            addonsJson = addons.cleanVariantAddons().let(DashboardRepositoryJson::encodeToString),
            sortOrder = sortOrder,
            status = status.cleanCatalogStatus(),
        )
    }

    private fun Connection.findExistingProductVariantId(
        access: DashboardWorkspaceAccess,
        productId: String,
        requestedVariantId: String?,
        cleanName: String,
    ): String? {
        requestedVariantId.cleanUuidOrNull()?.let { variantId ->
            prepareStatement(
                """
                select id::text
                from product_variants
                where workspace_id = ?::uuid and product_id = ?::uuid and id = ?::uuid
                limit 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, access.workspaceId)
                statement.setString(2, productId)
                statement.setString(3, variantId)
                statement.executeQuery().use { result ->
                    if (result.next()) return result.getString("id")
                }
            }
        }
        return prepareStatement(
            """
            select id::text
            from product_variants
            where workspace_id = ?::uuid and product_id = ?::uuid and name = ?
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setString(2, productId)
            statement.setString(3, cleanName)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("id") else null
            }
        }
    }

    private fun Connection.insertProductVariant(
        access: DashboardWorkspaceAccess,
        productId: String,
        values: ProductVariantValues,
    ): String =
        prepareStatement(
            """
            insert into product_variants (
                workspace_id, product_id, name, sku, barcode, selling_price, cost_price,
                stock_quantity, track_stock, duration_minutes, included_quantity, addons_json, sort_order, status, updated_at
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, now())
            returning id::text
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setString(2, productId)
            statement.bindProductVariantValues(startIndex = 3, values = values)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }

    private fun Connection.updateProductVariant(
        access: DashboardWorkspaceAccess,
        productId: String,
        variantId: String,
        values: ProductVariantValues,
    ): String =
        prepareStatement(
            """
            update product_variants
            set name = ?,
                sku = ?,
                barcode = ?,
                selling_price = ?,
                cost_price = ?,
                stock_quantity = ?,
                track_stock = ?,
                duration_minutes = ?,
                included_quantity = ?,
                addons_json = ?::jsonb,
                sort_order = ?,
                status = ?,
                updated_at = now()
            where workspace_id = ?::uuid and product_id = ?::uuid and id = ?::uuid
            returning id::text
            """.trimIndent(),
        ).use { statement ->
            statement.bindProductVariantValues(startIndex = 1, values = values)
            statement.setString(13, access.workspaceId)
            statement.setString(14, productId)
            statement.setString(15, variantId)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }

    private fun PreparedStatement.bindProductVariantValues(
        startIndex: Int,
        values: ProductVariantValues,
    ) {
        setString(startIndex, values.name)
        setNullableString(startIndex + 1, values.sku)
        setNullableString(startIndex + 2, values.barcode)
        setBigDecimal(startIndex + 3, values.sellingPrice)
        setBigDecimal(startIndex + 4, values.costPrice)
        setBigDecimal(startIndex + 5, values.stockQuantity)
        setBoolean(startIndex + 6, values.trackStock)
        if (values.durationMinutes == null) {
            setNull(startIndex + 7, Types.INTEGER)
        } else {
            setInt(startIndex + 7, values.durationMinutes)
        }
        setInt(startIndex + 8, values.includedQuantity)
        setString(startIndex + 9, values.addonsJson)
        setInt(startIndex + 10, values.sortOrder)
        setString(startIndex + 11, values.status)
    }

    private fun Connection.deleteProductVariantComponents(
        access: DashboardWorkspaceAccess,
        packageVariantId: String,
    ) {
        prepareStatement(
            """
            delete from product_variant_components
            where workspace_id = ?::uuid and package_variant_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setString(2, packageVariantId)
            statement.executeUpdate()
        }
    }

    private fun Connection.archiveRemovedProductVariants(
        access: DashboardWorkspaceAccess,
        productId: String,
        retainedVariantIds: List<String>,
    ) {
        if (retainedVariantIds.isEmpty()) {
            prepareStatement(
                """
                update product_variants
                set status = 'archived', updated_at = now()
                where workspace_id = ?::uuid and product_id = ?::uuid
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, access.workspaceId)
                statement.setString(2, productId)
                statement.executeUpdate()
            }
            return
        }
        val placeholders = retainedVariantIds.joinToString(", ") { "?::uuid" }
        prepareStatement(
            """
            update product_variants
            set status = 'archived', updated_at = now()
            where workspace_id = ?::uuid
                and product_id = ?::uuid
                and id not in ($placeholders)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setString(2, productId)
            retainedVariantIds.forEachIndexed { index, variantId ->
                statement.setString(index + 3, variantId)
            }
            statement.executeUpdate()
        }
    }

    private fun Connection.insertProductVariantComponents(
        access: DashboardWorkspaceAccess,
        packageVariantId: String,
        parentItemType: String,
        components: List<ProductVariantComponentRequest>,
    ) {
        components
            .take(40)
            .mapNotNull { component ->
                val productId = component.productId.cleanUuidOrNull() ?: return@mapNotNull null
                val variantId = component.variantId.cleanUuidOrNull()
                val quantity = component.quantity.decimalOrZero()
                    .takeIf { it > BigDecimal.ZERO }
                    ?.coerceAtMost(BigDecimal("999"))
                    ?: BigDecimal.ONE
                val resolved = resolvePackageComponent(
                    workspaceId = access.workspaceId,
                    productId = productId,
                    variantId = variantId,
                    expectedItemType = parentItemType,
                ) ?: return@mapNotNull null
                resolved.copy(quantity = quantity, status = component.status.cleanCatalogStatus())
            }
            .forEachIndexed { index, component ->
                prepareStatement(
                    """
                    insert into product_variant_components (
                        workspace_id, package_variant_id, component_product_id, component_variant_id,
                        quantity, sort_order, status, updated_at
                    )
                    values (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?, ?, now())
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, access.workspaceId)
                    statement.setString(2, packageVariantId)
                    statement.setString(3, component.productId)
                    statement.setNullableUuid(4, component.variantId)
                    statement.setBigDecimal(5, component.quantity.scaled())
                    statement.setInt(6, index)
                    statement.setString(7, component.status)
                    statement.executeUpdate()
                }
            }
    }

    private fun Connection.resolvePackageComponent(
        workspaceId: String,
        productId: String,
        variantId: String?,
        expectedItemType: String,
    ): ResolvedPackageComponent? {
        val sql = if (variantId == null) {
            """
            select p.id::text as product_id, null::text as variant_id
            from products p
            where p.workspace_id = ?::uuid
                and p.id = ?::uuid
                and p.item_type = ?
                and p.status = 'active'
            limit 1
            """.trimIndent()
        } else {
            """
            select p.id::text as product_id, pv.id::text as variant_id
            from products p
            join product_variants pv on pv.product_id = p.id and pv.workspace_id = p.workspace_id
            where p.workspace_id = ?::uuid
                and p.id = ?::uuid
                and p.item_type = ?
                and p.status = 'active'
                and pv.id = ?::uuid
                and pv.status = 'active'
            limit 1
            """.trimIndent()
        }
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, productId)
            statement.setString(3, expectedItemType)
            if (variantId != null) statement.setString(4, variantId)
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    null
                } else {
                    ResolvedPackageComponent(
                        productId = result.getString("product_id"),
                        variantId = result.getString("variant_id"),
                        quantity = BigDecimal.ONE,
                        status = "active",
                    )
                }
            }
        }
    }

    private fun Connection.attachProductVariants(products: List<ProductResponse>): List<ProductResponse> {
        if (products.isEmpty()) return products
        val productIds = products.map { it.id }.distinct()
        val placeholders = productIds.joinToString(", ") { "?::uuid" }
        val variantsByProduct = mutableMapOf<String, MutableList<ProductVariantResponse>>()
        prepareStatement(
            """
            select
                id::text,
                product_id::text,
                name,
                sku,
                barcode,
                selling_price,
                cost_price,
                stock_quantity,
                track_stock,
                duration_minutes,
                included_quantity,
                addons_json::text as addons_json,
                status
            from product_variants
            where product_id in ($placeholders) and status <> 'archived'
            order by product_id, sort_order asc, name asc
            """.trimIndent(),
        ).use { statement ->
            productIds.forEachIndexed { index, productId ->
                statement.setString(index + 1, productId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val variant = result.toProductVariantResponse()
                    variantsByProduct.getOrPut(variant.productId) { mutableListOf() }.add(variant)
                }
            }
        }
        val componentsByVariant = loadProductVariantComponents(
            variantIds = variantsByProduct.values.flatten().map { it.id },
            publicOnly = false,
        )
        val variantsWithComponentsByProduct = variantsByProduct.mapValues { (_, variants) ->
            variants.map { variant -> variant.copy(components = componentsByVariant[variant.id].orEmpty()) }
        }
        return products.map { product -> product.copy(variants = variantsWithComponentsByProduct[product.id].orEmpty()) }
    }

    private fun Connection.attachPublicCatalogVariants(
        products: List<PublicCatalogProductResponse>,
    ): List<PublicCatalogProductResponse> {
        if (products.isEmpty()) return products
        val productIds = products.map { it.id }.distinct()
        val placeholders = productIds.joinToString(", ") { "?::uuid" }
        val variantsByProduct = mutableMapOf<String, MutableList<ProductVariantResponse>>()
        prepareStatement(
            """
            select
                id::text,
                product_id::text,
                name,
                sku,
                barcode,
                selling_price,
                cost_price,
                stock_quantity,
                track_stock,
                duration_minutes,
                included_quantity,
                addons_json::text as addons_json,
                status
            from product_variants
            where product_id in ($placeholders) and status = 'active'
            order by product_id, sort_order asc, name asc
            """.trimIndent(),
        ).use { statement ->
            productIds.forEachIndexed { index, productId ->
                statement.setString(index + 1, productId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val variant = result.toProductVariantResponse()
                    variantsByProduct.getOrPut(variant.productId) { mutableListOf() }.add(variant)
                }
            }
        }
        val componentsByVariant = loadProductVariantComponents(
            variantIds = variantsByProduct.values.flatten().map { it.id },
            publicOnly = true,
        )
        val variantsWithComponentsByProduct = variantsByProduct.mapValues { (_, variants) ->
            variants.map { variant ->
                val withComponents = variant.copy(components = componentsByVariant[variant.id].orEmpty())
                withComponents.publicCatalogAvailabilityQuantity()?.let { availableQuantity ->
                    withComponents.copy(stockQuantity = availableQuantity.decimalString())
                } ?: withComponents
            }
        }
        return products.map { product ->
            val variants = variantsWithComponentsByProduct[product.id].orEmpty()
            product.copy(
                inStock = product.inStock || variants.any { variant ->
                    variant.status == "active" && when {
                        variant.components.isNotEmpty() -> variant.components.packageComponentsAvailable(BigDecimal.ONE)
                        product.itemType.cleanItemType() == "product" && variant.trackStock -> variant.stockQuantity.decimalOrZero() > BigDecimal.ZERO
                        product.itemType.cleanItemType() == "product" && product.trackStock -> product.stockQuantity.decimalOrZero() > BigDecimal.ZERO
                        else -> true
                    }
                },
                variants = variants,
            )
        }
    }

    private fun Connection.loadProductVariantComponents(
        variantIds: List<String>,
        publicOnly: Boolean,
    ): Map<String, List<ProductVariantComponentResponse>> {
        if (variantIds.isEmpty()) return emptyMap()
        val cleanVariantIds = variantIds.distinct()
        val placeholders = cleanVariantIds.joinToString(", ") { "?::uuid" }
        val statusClause = if (publicOnly) {
            "pvc.status = 'active' and p.status = 'active' and (pv.id is null or pv.status = 'active')"
        } else {
            "pvc.status <> 'archived' and p.status <> 'archived' and (pv.id is null or pv.status <> 'archived')"
        }
        val componentsByVariant = mutableMapOf<String, MutableList<ProductVariantComponentResponse>>()
        prepareStatement(
            """
            select
                pvc.package_variant_id::text,
                pvc.component_product_id::text,
                p.name as product_name,
                p.item_type,
                p.unit,
                p.selling_price as product_selling_price,
                p.stock_quantity as product_stock_quantity,
                p.track_stock,
                p.duration_minutes as product_duration_minutes,
                pvc.component_variant_id::text,
                pv.name as variant_name,
                pv.selling_price as variant_selling_price,
                pv.stock_quantity as variant_stock_quantity,
                pv.track_stock as variant_track_stock,
                pv.duration_minutes as variant_duration_minutes,
                pvc.quantity,
                pvc.status
            from product_variant_components pvc
            join products p on p.id = pvc.component_product_id and p.workspace_id = pvc.workspace_id
            left join product_variants pv on pv.id = pvc.component_variant_id and pv.workspace_id = pvc.workspace_id
            where pvc.package_variant_id in ($placeholders)
                and $statusClause
            order by pvc.package_variant_id, pvc.sort_order asc, p.name asc
            """.trimIndent(),
        ).use { statement ->
            cleanVariantIds.forEachIndexed { index, variantId ->
                statement.setString(index + 1, variantId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val packageVariantId = result.getString("package_variant_id")
                    val variantPrice = result.getBigDecimal("variant_selling_price")
                    val sellingPrice = if (result.wasNull()) {
                        result.getBigDecimal("product_selling_price")
                    } else {
                        variantPrice
                    }
                    val variantStockQuantity = result.getBigDecimal("variant_stock_quantity")
                    val componentVariantId = result.getString("component_variant_id")
                    val variantTrackStock = componentVariantId != null && result.getBoolean("variant_track_stock")
                    val productTrackStock = result.getBoolean("track_stock")
                    val productStockQuantity = result.getBigDecimal("product_stock_quantity") ?: BigDecimal.ZERO
                    val stockQuantity = when {
                        componentVariantId != null && variantTrackStock -> variantStockQuantity ?: BigDecimal.ZERO
                        productTrackStock -> productStockQuantity
                        componentVariantId != null -> variantStockQuantity ?: BigDecimal.ZERO
                        else -> productStockQuantity
                    }
                    componentsByVariant.getOrPut(packageVariantId) { mutableListOf() }.add(
                        ProductVariantComponentResponse(
                            productId = result.getString("component_product_id"),
                            productName = result.getString("product_name"),
                            itemType = result.getString("item_type").cleanItemType(),
                            variantId = componentVariantId,
                            variantName = result.getString("variant_name"),
                            quantity = result.getBigDecimal("quantity").decimalString(),
                            unit = result.getString("unit") ?: "pcs",
                            sellingPrice = sellingPrice.moneyString(),
                            stockQuantity = stockQuantity.decimalString(),
                            trackStock = productTrackStock || variantTrackStock,
                            durationMinutes = result.getNullableInt("variant_duration_minutes")
                                ?: result.getNullableInt("product_duration_minutes"),
                            status = result.getString("status").cleanCatalogStatus(),
                        ),
                    )
                }
            }
        }
        return componentsByVariant
    }

    private fun Connection.adjustProductStock(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: StockAdjustmentRequest,
    ): ProductResponse? {
        val quantityDelta = request.quantityDelta.decimalOrZero()
        var currentSupplierId: String? = null
        var currentSellingPrice = BigDecimal.ZERO
        var currentCostPrice = BigDecimal.ZERO
        var currentStatus = "active"
        var currentExpiryDate: String? = null
        val current = prepareStatement(
            """
            select stock_quantity, supplier_id::text, selling_price, cost_price, status, expiry_date::text
            from products
            where id = ?::uuid and workspace_id = ?::uuid and status <> 'archived' and item_type = 'product'
            for update
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, productId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) return null
                currentSupplierId = result.getString("supplier_id")
                currentSellingPrice = result.getBigDecimal("selling_price")
                currentCostPrice = result.getBigDecimal("cost_price")
                currentStatus = result.getString("status") ?: "active"
                currentExpiryDate = result.getString("expiry_date")
                result.getBigDecimal("stock_quantity")
            }
        }
        val next = current.add(quantityDelta).scaled()
        val nextSupplierId = resolveSupplierId(access, request.supplierId) ?: currentSupplierId
        val nextSellingPrice = request.sellingPrice?.cleanOptional()?.moneyOrZero() ?: currentSellingPrice
        val nextCostPrice = request.costPrice?.cleanOptional()?.moneyOrZero() ?: currentCostPrice
        val supplierPaidTotal = request.supplierPaidTotal?.cleanOptional()?.moneyOrZero() ?: BigDecimal.ZERO
        val payableAmount = if (quantityDelta > BigDecimal.ZERO && nextSupplierId != null) {
            nextCostPrice.multiply(quantityDelta).scaled()
        } else {
            BigDecimal.ZERO
        }
        val batchPaidAmount = supplierPaidTotal.coerceAtMost(payableAmount).coerceAtLeast(BigDecimal.ZERO)
        val nextStatus = request.status?.cleanCatalogStatus() ?: currentStatus.cleanCatalogStatus()
        val nextExpiryDate = request.expiryDate?.cleanIsoDateOrNull() ?: currentExpiryDate
        val updated = prepareStatement(
            """
            update products
            set stock_quantity = ?, track_stock = true, supplier_id = ?::uuid,
                selling_price = ?, cost_price = ?, status = ?, expiry_date = ?::date, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *, null::text as supplier_name, null::text as category_name,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = products.workspace_id
                      and pi.product_id = products.id::text
                      and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path
            """.trimIndent(),
        ).use { statement ->
            statement.setBigDecimal(1, next)
            statement.setNullableUuid(2, nextSupplierId)
            statement.setBigDecimal(3, nextSellingPrice)
            statement.setBigDecimal(4, nextCostPrice)
            statement.setString(5, nextStatus)
            statement.setNullableString(6, nextExpiryDate)
            statement.setString(7, productId)
            statement.setString(8, access.workspaceId)
            statement.executeQuery().use { result ->
                result.next()
                attachProductVariants(listOf(result.toProductResponse())).first()
            }
        }
        if (quantityDelta != BigDecimal.ZERO) {
            insertStockMovement(
                access = access,
                productId = productId,
                movementType = "adjustment",
                quantityDelta = quantityDelta,
                balanceAfter = next,
                note = request.note?.cleanOptional(),
                supplierId = nextSupplierId,
                unitCost = nextCostPrice,
                payableAmount = payableAmount,
                paidAmount = batchPaidAmount,
                expiryDate = nextExpiryDate,
            )
        }
        return updated
    }

    private fun Connection.listOrders(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
        includeItems: Boolean,
        excludeCancelledWhenStatusAll: Boolean = false,
    ): PagedResult<OrderResponse> {
        val params = mutableListOf(workspaceId)
        val searchTokens = filters.query.cleanSearchTokens()
        val status = filters.status.cleanOrderStatusFilter()
        val orderType = filters.orderType.cleanOrderTypeFilter()
        val sql = buildString {
            append(orderSelectSql())
            append(" where o.workspace_id = ?::uuid")
            if (status != null) {
                append(" and o.status = ?")
                params.add(status)
            } else if (excludeCancelledWhenStatusAll) {
                append(" and o.status <> 'cancelled'")
            }
            if (filters.scheduledOnly) {
                append(" and o.scheduled_at is not null")
            }
            if (orderType != null) {
                append(" and o.order_type = ?")
                params.add(orderType)
            }
            appendOrderDateWhere(filters, params, alias = "o")
            appendOrderSearchWhere(searchTokens, params)
            append(" group by o.id, c.name")
            append(" order by o.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        val order = result.toOrderResponse(emptyList(), emptyList())
                        add(if (includeItems) {
                            order.copy(
                                items = listOrderItems(order.id),
                                sessions = listOrderSessions(workspaceId, order.id),
                                changeRequests = listOrderChangeRequests(workspaceId, order.id),
                            )
                        } else {
                            order
                        })
                    }
                }
                val resolvedTotalItems = if (items.isEmpty() && filters.offset() > 0) {
                    countOrders(
                        workspaceId = workspaceId,
                        filters = filters,
                        excludeCancelledWhenStatusAll = excludeCancelledWhenStatusAll,
                    )
                } else {
                    totalItems
                }
                items.toPagedResult(filters, resolvedTotalItems)
            }
        }
    }

    private fun Connection.countOrders(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
        excludeCancelledWhenStatusAll: Boolean = false,
    ): Int {
        val params = mutableListOf(workspaceId)
        val searchTokens = filters.query.cleanSearchTokens()
        val status = filters.status.cleanOrderStatusFilter()
        val orderType = filters.orderType.cleanOrderTypeFilter()
        val sql = buildString {
            append(
                """
                select count(distinct o.id)::int as total_count
                from orders o
                left join customers c on c.id = o.customer_id
                where o.workspace_id = ?::uuid
                """.trimIndent(),
            )
            if (status != null) {
                append(" and o.status = ?")
                params.add(status)
            } else if (excludeCancelledWhenStatusAll) {
                append(" and o.status <> 'cancelled'")
            }
            if (filters.scheduledOnly) {
                append(" and o.scheduled_at is not null")
            }
            if (orderType != null) {
                append(" and o.order_type = ?")
                params.add(orderType)
            }
            appendOrderDateWhere(filters, params, alias = "o")
            appendOrderSearchWhere(searchTokens, params)
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                if (result.next()) result.getInt("total_count") else 0
            }
        }
    }

    private fun Connection.listCustomerOrders(
        workspaceId: String,
        customerId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
        includeItems: Boolean,
    ): PagedResult<OrderResponse> {
        val params = mutableListOf(workspaceId, customerId)
        val searchTokens = filters.query.cleanSearchTokens()
        val status = filters.status.cleanOrderStatusFilter()
        val orderType = filters.orderType.cleanOrderTypeFilter()
        val sql = buildString {
            append(orderSelectSql())
            append(" where o.workspace_id = ?::uuid and o.customer_id = ?::uuid")
            if (status != null) {
                append(" and o.status = ?")
                params.add(status)
            }
            if (filters.scheduledOnly) {
                append(" and o.scheduled_at is not null")
            }
            if (orderType != null) {
                append(" and o.order_type = ?")
                params.add(orderType)
            }
            appendOrderDateWhere(filters, params, alias = "o")
            appendOrderSearchWhere(searchTokens, params)
            append(" group by o.id, c.name")
            append(" order by o.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        val order = result.toOrderResponse(emptyList(), emptyList())
                        add(if (includeItems) {
                            order.copy(
                                items = listOrderItems(order.id),
                                sessions = listOrderSessions(workspaceId, order.id),
                                changeRequests = listOrderChangeRequests(workspaceId, order.id),
                            )
                        } else {
                            order
                        })
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.listPublicCatalogCustomerOrders(
        workspaceId: String,
        identifiers: PublicCatalogCustomerIdentifiers,
        filters: DashboardQueryFilters = DashboardQueryFilters(limit = 20),
        includeItems: Boolean,
    ): PagedResult<OrderResponse> {
        val params = mutableListOf(workspaceId)
        val searchTokens = filters.query.cleanSearchTokens()
        val sql = buildString {
            append(orderSelectSql())
            append(" where o.workspace_id = ?::uuid")
            append(" and o.source = 'public_catalog'")
            append(" and o.customer_id is not null")
            append(" and (")
            val clauses = buildList {
                identifiers.email?.let { email ->
                    add("lower(coalesce(c.email, '')) = ?")
                    params.add(email)
                }
                identifiers.phoneNumber?.let { phoneNumber ->
                    add("coalesce(c.phone_number, '') = ?")
                    params.add(phoneNumber)
                }
            }
            append(clauses.joinToString(" or "))
            append(")")
            appendOrderDateWhere(filters, params, alias = "o")
            appendOrderSearchWhere(searchTokens, params)
            append(" group by o.id, c.name")
            append(" order by o.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit().coerceAtMost(20)}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        val order = result.toOrderResponse(emptyList(), emptyList())
                        add(if (includeItems) {
                            order.copy(
                                items = listOrderItems(order.id),
                                sessions = listOrderSessions(workspaceId, order.id),
                                changeRequests = listOrderChangeRequests(workspaceId, order.id),
                            )
                        } else {
                            order
                        })
                    }
                }
                items.toPagedResult(filters.copy(limit = filters.limit.coerceAtMost(20)), totalItems)
            }
        }
    }

    private fun Connection.upsertPublicCatalogNotificationDeviceToken(
        workspaceId: String,
        firebaseUser: VerifiedFirebaseUser,
        identifiers: PublicCatalogCustomerIdentifiers,
        request: NotificationPreferenceRequest,
    ) {
        val cleanToken = request.deviceToken?.trim()?.takeIf { it.isNotBlank() } ?: return
        val cleanPlatform = request.platform?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "unknown"
        val cleanDeviceName = request.deviceName?.trim()?.takeIf { it.isNotBlank() }?.take(120)
        prepareStatement(
            """
            insert into public_catalog_notification_device_tokens (
                workspace_id, firebase_uid, email, phone_number, token, platform, device_name,
                enabled, last_seen_at, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, true, now(), now())
            on conflict (token) do update set
                workspace_id = excluded.workspace_id,
                firebase_uid = excluded.firebase_uid,
                email = excluded.email,
                phone_number = excluded.phone_number,
                platform = excluded.platform,
                device_name = excluded.device_name,
                enabled = true,
                last_seen_at = now(),
                updated_at = now()
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, firebaseUser.uid)
            statement.setNullableString(3, identifiers.email)
            statement.setNullableString(4, identifiers.phoneNumber)
            statement.setString(5, cleanToken)
            statement.setString(6, cleanPlatform)
            statement.setNullableString(7, cleanDeviceName)
            statement.executeUpdate()
        }
    }

    private fun Connection.disablePublicCatalogNotificationDeviceToken(
        workspaceId: String,
        firebaseUser: VerifiedFirebaseUser,
        identifiers: PublicCatalogCustomerIdentifiers,
        token: String?,
    ) {
        val cleanToken = token?.trim()?.takeIf { it.isNotBlank() }
        val email = identifiers.email.orEmpty()
        val phoneNumber = identifiers.phoneNumber.orEmpty()
        val sql = buildString {
            append(
                """
                update public_catalog_notification_device_tokens
                set enabled = false, updated_at = now()
                where workspace_id = ?::uuid
                  and (
                    firebase_uid = ?
                    or (? <> '' and lower(coalesce(email, '')) = ?)
                    or (? <> '' and coalesce(phone_number, '') = ?)
                  )
                """.trimIndent(),
            )
            if (cleanToken != null) {
                append(" and token = ?")
            }
        }
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, firebaseUser.uid)
            statement.setString(3, email)
            statement.setString(4, email)
            statement.setString(5, phoneNumber)
            statement.setString(6, phoneNumber)
            cleanToken?.let { statement.setString(7, it) }
            statement.executeUpdate()
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
                    val order = result.toOrderResponse(emptyList(), emptyList())
                    if (includeItems) {
                        order.copy(
                            items = listOrderItems(order.id),
                            sessions = listOrderSessions(workspaceId, order.id),
                            changeRequests = listOrderChangeRequests(workspaceId, order.id),
                        )
                    } else {
                        order
                    }
                }
            }
        }
    }

    private fun Connection.listPrinters(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): PagedResult<PrinterProfileResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select *, count(*) over()::int as total_count
                from printer_profiles
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(
                    """
                     and (
                        name ilike ?
                        or connection_type ilike ?
                        or coalesce(address, '') ilike ?
                        or coalesce(notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(4) { params.add(search.ilikePattern()) }
            }
            appendCreatedDateWhere(filters, params, "created_at")
            append(" order by is_default_receipt desc, is_default_barcode desc, created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
            append(" offset ${filters.offset()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                var totalItems = 0
                val items = buildList {
                    while (result.next()) {
                        if (totalItems == 0) totalItems = result.getInt("total_count")
                        add(result.toPrinterProfileResponse())
                    }
                }
                items.toPagedResult(filters, totalItems)
            }
        }
    }

    private fun Connection.insertPrinter(
        workspaceId: String,
        request: PrinterProfileRequest,
    ): PrinterProfileResponse {
        clearPrinterDefaultsIfNeeded(workspaceId, request)
        val sql = """
            insert into printer_profiles (
                workspace_id, name, connection_type, address, paper_width_mm, dpi,
                supports_receipts, supports_barcodes, is_default_receipt, is_default_barcode,
                print_logo, header_alignment, show_business_address, show_catalog_qr,
                show_timed_greeting, notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.bindPrinterRequest(request, startIndex = 2)
            statement.executeQuery().use { result ->
                result.next()
                result.toPrinterProfileResponse()
            }
        }
    }

    private fun Connection.updatePrinter(
        workspaceId: String,
        printerId: String,
        request: PrinterProfileRequest,
    ): PrinterProfileResponse? {
        val cleanPrinterId = printerId.cleanUuidOrNull() ?: return null
        clearPrinterDefaultsIfNeeded(workspaceId, request, exceptPrinterId = cleanPrinterId)
        val sql = """
            update printer_profiles
            set name = ?, connection_type = ?, address = ?, paper_width_mm = ?, dpi = ?,
                supports_receipts = ?, supports_barcodes = ?, is_default_receipt = ?,
                is_default_barcode = ?, print_logo = ?, header_alignment = ?,
                show_business_address = ?, show_catalog_qr = ?, show_timed_greeting = ?,
                notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindPrinterRequest(request, startIndex = 1)
            statement.setString(16, cleanPrinterId)
            statement.setString(17, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toPrinterProfileResponse() else null
            }
        }
    }

    private fun Connection.deletePrinter(
        workspaceId: String,
        printerId: String,
    ): PrinterProfileResponse? {
        val cleanPrinterId = printerId.cleanUuidOrNull() ?: return null
        val sql = """
            update printer_profiles
            set status = 'deleted',
                is_default_receipt = false,
                is_default_barcode = false,
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, cleanPrinterId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toPrinterProfileResponse() else null
            }
        }
    }

    private fun Connection.clearPrinterDefaultsIfNeeded(
        workspaceId: String,
        request: PrinterProfileRequest,
        exceptPrinterId: String? = null,
    ) {
        if (!request.isDefaultReceipt && !request.isDefaultBarcode) return
        val clauses = buildList {
            if (request.isDefaultReceipt) add("is_default_receipt = false")
            if (request.isDefaultBarcode) add("is_default_barcode = false")
        }
        val sql = buildString {
            append("update printer_profiles set ")
            append(clauses.joinToString(", "))
            append(", updated_at = now() where workspace_id = ?::uuid and status = 'active'")
            if (exceptPrinterId != null) append(" and id <> ?::uuid")
        }
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            if (exceptPrinterId != null) statement.setString(2, exceptPrinterId)
            statement.executeUpdate()
        }
    }

    private fun Connection.ensurePrinterDefaults(workspaceId: String) {
        ensurePrinterDefault(
            workspaceId = workspaceId,
            defaultColumn = "is_default_receipt",
            supportColumn = "supports_receipts",
        )
        ensurePrinterDefault(
            workspaceId = workspaceId,
            defaultColumn = "is_default_barcode",
            supportColumn = "supports_barcodes",
        )
    }

    private fun Connection.ensurePrinterDefault(
        workspaceId: String,
        defaultColumn: String,
        supportColumn: String,
    ) {
        val sql = """
            update printer_profiles
            set $defaultColumn = true, updated_at = now()
            where id = (
                select id
                from printer_profiles
                where workspace_id = ?::uuid
                    and status = 'active'
                    and $supportColumn = true
                    and not exists (
                        select 1
                        from printer_profiles existing
                        where existing.workspace_id = printer_profiles.workspace_id
                            and existing.status = 'active'
                            and existing.$defaultColumn = true
                    )
                order by created_at desc
                limit 1
            )
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeUpdate()
        }
    }

    private fun Connection.insertOrder(
        access: DashboardWorkspaceAccess,
        request: OrderRequest,
        discountTotalOverride: BigDecimal = BigDecimal.ZERO,
    ): OrderResponse {
        val status = request.status.normalizedOrderStatus()
        val orderType = request.orderType.cleanOrderType()
        if (orderType == "appointment" && request.scheduledAt.cleanOptional() == null) {
            throw DashboardOrderValidationException(
                code = "appointment_time_required",
                publicMessage = "Choose a preferred date and time for this appointment.",
            )
        }
        val currency = request.currency?.cleanOptionalUpper() ?: access.currency
        val source = request.source.cleanOrderSource()
        val clientRequestId = request.clientRequestId.cleanClientRequestId()
        if (clientRequestId != null) {
            lockOrderClientRequest(access.workspaceId, source, clientRequestId)
            getOrderByClientRequestId(access.workspaceId, source, clientRequestId)?.let { existingOrder ->
                return existingOrder
            }
        }
        val customerId = request.customerId.cleanUuidOrNull()
        if (customerId != null) {
            updateCustomerBillingDetails(
                workspaceId = access.workspaceId,
                customerId = customerId,
                request = request,
            )
        }
        val effectiveCustomerId = customerId
            ?: request.customerName?.cleanOptional()?.let { name ->
                insertCustomer(
                    workspaceId = access.workspaceId,
                    request = request.toCustomerRequest(name),
                ).id
            }
        val preparedItems = request.items.map { it.toPreparedOrderItem() }
        validateOrderCatalogItemTypes(access.workspaceId, orderType, preparedItems)
        if (source == "public_catalog" && status == "new" && !publicCatalogInventoryHasCapacity(access.workspaceId, preparedItems)) {
            throw DashboardOrderValidationException(
                code = "public_catalog_stock_unavailable",
                publicMessage = "Selected items are no longer available in the requested quantity.",
            )
        }
        val subtotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineSubtotal) }.scaled()
        val taxTotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineTax) }.scaled()
        val total = preparedItems.fold(BigDecimal.ZERO) { sum, item -> sum.add(item.lineTotal) }.scaled()
        val discountTotal = (if (discountTotalOverride > BigDecimal.ZERO) {
            discountTotalOverride
        } else {
            request.discountTotal.moneyOrZero()
        })
            .coerceAtLeast(BigDecimal.ZERO)
            .scaled()
        val paymentMode = request.paymentMode.cleanPaymentMode()
        validateCreditPaymentStatus(paymentMode, status)
        val requestedPaidTotal = request.paidTotal.moneyOrZero().coerceAtMost(total)
        val paidTotal = if (status.impliesFullPayment() && requestedPaidTotal <= BigDecimal.ZERO) {
            total
        } else {
            requestedPaidTotal
        }
        val inventoryApplied = status.appliesInventory()
        val orderId: String
        val orderNumber = if (source.isBillingDocumentOrderSource()) {
            nextBillingDocumentOrderNumber(access.workspaceId, source)
        } else {
            "ORD-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        }

        val insertOrderSql = """
            insert into orders (
                workspace_id, customer_id, order_number, order_type, status, scheduled_at,
                subtotal, tax_total, discount_total, paid_total, total, currency,
                notes, inventory_applied, created_by_user_id, source, fulfillment_type, payment_mode,
                client_request_id, updated_at
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?, ?::timestamptz,
                ?, ?, ?, ?, ?, ?, ?, ?, ?::uuid, ?, ?, ?, ?, now()
            )
            returning id::text
        """.trimIndent()
        orderId = prepareStatement(insertOrderSql).use { statement ->
            statement.setString(1, access.workspaceId)
            statement.setNullableUuid(2, effectiveCustomerId)
            statement.setString(3, orderNumber)
            statement.setString(4, orderType)
            statement.setString(5, status)
            statement.setNullableString(6, request.scheduledAt?.cleanOptional())
            statement.setBigDecimal(7, subtotal)
            statement.setBigDecimal(8, taxTotal)
            statement.setBigDecimal(9, discountTotal)
            statement.setBigDecimal(10, paidTotal)
            statement.setBigDecimal(11, total)
            statement.setString(12, currency)
            statement.setNullableString(13, request.notes?.cleanOptional())
            statement.setBoolean(14, inventoryApplied)
            statement.setString(15, access.userId)
            statement.setString(16, source)
            statement.setString(17, request.fulfillmentType.cleanFulfillmentType())
            statement.setString(18, paymentMode)
            statement.setNullableString(19, clientRequestId)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }

        val insertedItems = preparedItems.map { item ->
            val orderItemId = insertOrderItem(orderId, item)
            if (inventoryApplied && item.productId != null) {
                applyOrderInventoryMovement(access, item, item.quantity.negate(), "Order $orderNumber")
            }
            InsertedOrderItem(item = item, orderItemId = orderItemId)
        }
        replaceOrderSessions(
            workspaceId = access.workspaceId,
            orderId = orderId,
            orderType = orderType,
            requestedSessions = request.sessions,
            insertedItems = insertedItems,
            fallbackScheduledAt = request.scheduledAt,
        )

        return getOrder(access.workspaceId, orderId, includeItems = true) ?: error("Created order was not found.")
    }

    private fun Connection.nextBillingDocumentOrderNumber(
        workspaceId: String,
        source: String,
    ): String {
        val current = prepareStatement(
            """
            select invoice_prefix, next_invoice_number
            from business_workspaces
            where id = ?::uuid
            for update
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    result.getString("invoice_prefix") to result.getString("next_invoice_number")
                } else {
                    "ORMA" to "0001"
                }
            }
        }
        val prefix = current.first
            ?.trim()
            ?.uppercase()
            ?.filter { it.isLetterOrDigit() }
            ?.ifBlank { null }
            ?: "ORMA"
        val rawNext = current.second.orEmpty().filter(Char::isDigit).ifBlank { "0001" }
        val width = rawNext.length.coerceAtLeast(4)
        var nextNumber = rawNext.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
        val quotation = source == "quotation"
        var orderNumber: String
        do {
            val padded = nextNumber.toString().padStart(width, '0')
            orderNumber = if (quotation) "$prefix-QTN-$padded" else "$prefix-$padded"
            nextNumber += 1L
        } while (orderNumberExists(workspaceId, orderNumber))

        val nextStoredNumber = nextNumber.toString().padStart(width, '0')
        prepareStatement(
            """
            update business_workspaces
            set next_invoice_number = ?, updated_at = now()
            where id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, nextStoredNumber)
            statement.setString(2, workspaceId)
            statement.executeUpdate()
        }
        return orderNumber
    }

    private fun Connection.orderNumberExists(
        workspaceId: String,
        orderNumber: String,
    ): Boolean =
        prepareStatement(
            """
            select 1
            from orders
            where workspace_id = ?::uuid and lower(order_number) = lower(?)
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderNumber)
            statement.executeQuery().use { result -> result.next() }
        }

    private fun Connection.lockOrderClientRequest(
        workspaceId: String,
        source: String,
        clientRequestId: String,
    ) {
        prepareStatement("select pg_advisory_xact_lock(hashtext(?), hashtext(?))").use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, "$source:$clientRequestId")
            statement.execute()
        }
    }

    private fun Connection.getOrderByClientRequestId(
        workspaceId: String,
        source: String,
        clientRequestId: String,
    ): OrderResponse? {
        val orderId = prepareStatement(
            """
            select id::text
            from orders
            where workspace_id = ?::uuid and source = ? and client_request_id = ?
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, source)
            statement.setString(3, clientRequestId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getString("id") else null
            }
        }
        return orderId?.let { getOrder(workspaceId, it, includeItems = true) }
    }

    private fun Connection.updateOrder(
        access: DashboardWorkspaceAccess,
        orderId: String,
        request: OrderRequest,
    ): OrderResponse? {
        val cleanOrderId = orderId.cleanUuidOrNull() ?: return null
        val status = request.status.normalizedOrderStatus()
        val orderType = request.orderType.cleanOrderType()
        if (orderType == "appointment" && request.scheduledAt.cleanOptional() == null) {
            throw DashboardOrderValidationException(
                code = "appointment_time_required",
                publicMessage = "Choose a preferred date and time for this appointment.",
            )
        }
        val currentSql = """
            select order_number, inventory_applied
            from orders
            where id = ?::uuid and workspace_id = ?::uuid
            for update
        """.trimIndent()
        val current = prepareStatement(currentSql).use { statement ->
            statement.setString(1, cleanOrderId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) return null
                result.getString("order_number") to result.getBoolean("inventory_applied")
            }
        }
        val orderNumber = current.first
        val inventoryWasApplied = current.second
        val oldItems = listOrderItems(cleanOrderId)
        val currency = request.currency?.cleanOptionalUpper() ?: access.currency
        val customerId = request.customerId.cleanUuidOrNull()
        if (customerId != null) {
            updateCustomerBillingDetails(
                workspaceId = access.workspaceId,
                customerId = customerId,
                request = request,
            )
        }
        val effectiveCustomerId = customerId
            ?: request.customerName?.cleanOptional()?.let { name ->
                insertCustomer(
                    workspaceId = access.workspaceId,
                    request = request.toCustomerRequest(name),
                ).id
            }
        val preparedItems = request.items.map { it.toPreparedOrderItem() }
        validateOrderCatalogItemTypes(access.workspaceId, orderType, preparedItems)
        val subtotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineSubtotal) }.scaled()
        val taxTotal = preparedItems.fold(BigDecimal.ZERO) { total, item -> total.add(item.lineTax) }.scaled()
        val total = preparedItems.fold(BigDecimal.ZERO) { sum, item -> sum.add(item.lineTotal) }.scaled()
        val discountTotal = request.discountTotal.moneyOrZero().coerceAtLeast(BigDecimal.ZERO).scaled()
        val paymentMode = request.paymentMode.cleanPaymentMode()
        validateCreditPaymentStatus(paymentMode, status)
        val requestedPaidTotal = request.paidTotal.moneyOrZero().coerceAtMost(total)
        val paidTotal = if (status.impliesFullPayment() && requestedPaidTotal <= BigDecimal.ZERO) {
            total
        } else {
            requestedPaidTotal
        }
        val inventoryShouldBeApplied = status.appliesInventory()

        if (inventoryWasApplied) {
            oldItems.forEach { item ->
                applyOrderInventoryMovement(
                    access = access,
                    item = item,
                    quantityDelta = item.quantity.decimalOrZero(),
                    note = "Order $orderNumber edited",
                )
            }
        }

        prepareStatement("delete from order_items where order_id = ?::uuid").use { statement ->
            statement.setString(1, cleanOrderId)
            statement.executeUpdate()
        }

        prepareStatement(
            """
            update orders
            set
                customer_id = ?::uuid,
                order_type = ?,
                status = ?,
                scheduled_at = ?::timestamptz,
                subtotal = ?,
                tax_total = ?,
                discount_total = ?,
                paid_total = ?,
                total = ?,
                currency = ?,
                notes = ?,
                inventory_applied = ?,
                source = ?,
                fulfillment_type = ?,
                payment_mode = ?,
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setNullableUuid(1, effectiveCustomerId)
            statement.setString(2, orderType)
            statement.setString(3, status)
            statement.setNullableString(4, request.scheduledAt?.cleanOptional())
            statement.setBigDecimal(5, subtotal)
            statement.setBigDecimal(6, taxTotal)
            statement.setBigDecimal(7, discountTotal)
            statement.setBigDecimal(8, paidTotal)
            statement.setBigDecimal(9, total)
            statement.setString(10, currency)
            statement.setNullableString(11, request.notes?.cleanOptional())
            statement.setBoolean(12, inventoryShouldBeApplied)
            statement.setString(13, request.source.cleanOrderSource())
            statement.setString(14, request.fulfillmentType.cleanFulfillmentType())
            statement.setString(15, paymentMode)
            statement.setString(16, cleanOrderId)
            statement.setString(17, access.workspaceId)
            statement.executeUpdate()
        }

        val insertedItems = preparedItems.map { item ->
            val orderItemId = insertOrderItem(cleanOrderId, item)
            if (inventoryShouldBeApplied && item.productId != null) {
                applyOrderInventoryMovement(access, item, item.quantity.negate(), "Order $orderNumber edited")
            }
            InsertedOrderItem(item = item, orderItemId = orderItemId)
        }
        replaceOrderSessions(
            workspaceId = access.workspaceId,
            orderId = cleanOrderId,
            orderType = orderType,
            requestedSessions = request.sessions,
            insertedItems = insertedItems,
            fallbackScheduledAt = request.scheduledAt,
        )

        return getOrder(access.workspaceId, cleanOrderId, includeItems = true)
    }

    private fun Connection.updateOrderStatus(
        access: DashboardWorkspaceAccess,
        orderId: String,
        requestedStatus: String,
        requestedPaidTotal: String? = null,
    ): OrderResponse? {
        val status = requestedStatus.normalizedOrderStatus()
        val currentSql = """
            select status, inventory_applied, total, payment_mode
            from orders
            where id = ?::uuid and workspace_id = ?::uuid
            for update
        """.trimIndent()
        val current = prepareStatement(currentSql).use { statement ->
            statement.setString(1, orderId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (!result.next()) return null
                CurrentOrderStatus(
                    status = result.getString("status"),
                    inventoryApplied = result.getBoolean("inventory_applied"),
                    total = result.getBigDecimal("total") ?: BigDecimal.ZERO,
                    paymentMode = result.getString("payment_mode") ?: "pay_on_spot",
                )
            }
        }
        val cleanRequestedPaidTotal = requestedPaidTotal
            ?.takeIf { it.isNotBlank() }
            ?.moneyOrZero()
            ?.coerceAtMost(current.total)
        val shouldApplyInventory = status.appliesInventory() && !current.inventoryApplied
        prepareStatement(
            """
            update orders
            set
                status = ?,
                inventory_applied = inventory_applied or ?,
                paid_total = case
                    when ? then ?
                    when ? and coalesce(paid_total, 0) <= 0 then total
                    else paid_total
                end,
                payment_mode = case
                    when ? then 'pay_on_spot'
                    else payment_mode
                end,
                updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, status)
            statement.setBoolean(2, shouldApplyInventory)
            statement.setBoolean(3, cleanRequestedPaidTotal != null)
            statement.setBigDecimal(4, cleanRequestedPaidTotal ?: BigDecimal.ZERO)
            statement.setBoolean(5, status.impliesFullPayment())
            statement.setBoolean(6, current.paymentMode == "credit" && status.impliesFullPayment())
            statement.setString(7, orderId)
            statement.setString(8, access.workspaceId)
            statement.executeUpdate()
        }
        if (shouldApplyInventory) {
            listOrderItems(orderId).forEach { item ->
                applyOrderInventoryMovement(
                    access = access,
                    item = item,
                    quantityDelta = item.quantity.decimalOrZero().negate(),
                    note = "Order status: $status",
                )
            }
        }
        return getOrder(access.workspaceId, orderId, includeItems = true)
    }

    private fun Connection.insertOrderItem(orderId: String, item: PreparedOrderItem): String {
        val sql = """
            insert into order_items (
                order_id, product_id, variant_id, variant_name, description, quantity, unit_price, tax_rate,
                line_subtotal, line_tax, line_total
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?)
            returning id::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, orderId)
            statement.setNullableUuid(2, item.productId)
            statement.setNullableUuid(3, item.variantId)
            statement.setNullableString(4, item.variantName)
            statement.setString(5, item.description)
            statement.setBigDecimal(6, item.quantity)
            statement.setBigDecimal(7, item.unitPrice)
            statement.setBigDecimal(8, item.taxRate)
            statement.setBigDecimal(9, item.lineSubtotal)
            statement.setBigDecimal(10, item.lineTax)
            statement.setBigDecimal(11, item.lineTotal)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
    }

    private fun Connection.listOrderItems(orderId: String): List<OrderItemResponse> {
        val sql = """
            select
                oi.id::text,
                oi.product_id::text,
                p.name as product_name,
                (
                    select pi.storage_path
                    from product_images pi
                    where pi.workspace_id = p.workspace_id
                        and pi.product_id = oi.product_id::text
                        and pi.status = 'active'
                    order by pi.sort_order asc, pi.created_at desc
                    limit 1
                ) as image_storage_path,
                oi.variant_id::text,
                coalesce(oi.variant_name, pv.name) as variant_name,
                oi.description,
                oi.quantity,
                oi.unit_price,
                oi.tax_rate,
                oi.line_subtotal,
                oi.line_tax,
                oi.line_total
            from order_items oi
            left join products p on p.id = oi.product_id
            left join product_variants pv on pv.id = oi.variant_id
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

    private fun Connection.listOrderChangeRequests(
        workspaceId: String,
        orderId: String,
    ): List<OrderChangeRequestResponse> {
        val sql = """
            select
                id::text,
                status,
                requested_items::text,
                requested_payment_mode,
                requested_paid_total,
                payment_reference,
                customer_notes,
                business_notes,
                resolved_at::text,
                created_at::text,
                updated_at::text
            from order_change_requests
            where workspace_id = ?::uuid and order_id = ?::uuid
            order by
                case when status = 'pending' then 0 else 1 end,
                created_at desc
            limit 12
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(result.toOrderChangeRequestResponse())
                    }
                }
            }
        }
    }

    private fun Connection.getOrderChangeRequest(
        workspaceId: String,
        requestId: String,
    ): OrderChangeRequestResponse? {
        val sql = """
            select
                id::text,
                status,
                requested_items::text,
                requested_payment_mode,
                requested_paid_total,
                payment_reference,
                customer_notes,
                business_notes,
                resolved_at::text,
                created_at::text,
                updated_at::text
            from order_change_requests
            where workspace_id = ?::uuid and id = ?::uuid
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, requestId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toOrderChangeRequestResponse() else null
            }
        }
    }

    private fun Connection.getOrderChangeRequestForUpdate(
        workspaceId: String,
        orderId: String,
        requestId: String,
    ): OrderChangeRequestResponse? {
        val sql = """
            select
                id::text,
                status,
                requested_items::text,
                requested_payment_mode,
                requested_paid_total,
                payment_reference,
                customer_notes,
                business_notes,
                resolved_at::text,
                created_at::text,
                updated_at::text
            from order_change_requests
            where workspace_id = ?::uuid and order_id = ?::uuid and id = ?::uuid
            for update
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.setString(3, requestId)
            statement.executeQuery().use { result ->
                if (result.next()) result.toOrderChangeRequestResponse() else null
            }
        }
    }

    private fun Connection.insertOrderChangeRequest(
        workspaceId: String,
        order: OrderResponse,
        firebaseUid: String?,
        items: List<OrderChangeRequestItemResponse>,
        requestedPaymentMode: String?,
        requestedPaidTotal: BigDecimal?,
        paymentReference: String?,
        customerNotes: String?,
    ): String {
        val sql = """
            insert into order_change_requests (
                workspace_id,
                order_id,
                customer_id,
                firebase_uid,
                requested_items,
                requested_payment_mode,
                requested_paid_total,
                payment_reference,
                customer_notes,
                updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?::jsonb, ?, ?, ?, ?, now())
            returning id::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, order.id)
            statement.setNullableUuid(3, order.customerId)
            statement.setNullableString(4, firebaseUid?.cleanOptional())
            statement.setString(5, DashboardRepositoryJson.encodeToString(items))
            statement.setNullableString(6, requestedPaymentMode)
            statement.setNullableBigDecimal(7, requestedPaidTotal)
            statement.setNullableString(8, paymentReference)
            statement.setNullableString(9, customerNotes)
            statement.executeQuery().use { result ->
                result.next()
                result.getString("id")
            }
        }
    }

    private fun Connection.markOrderChangeRequestResolved(
        workspaceId: String,
        orderId: String,
        requestId: String,
        approved: Boolean,
        businessNotes: String?,
        userId: String,
    ) {
        prepareStatement(
            """
            update order_change_requests
            set status = ?,
                business_notes = ?,
                resolved_by_user_id = ?::uuid,
                resolved_at = now(),
                updated_at = now()
            where workspace_id = ?::uuid and order_id = ?::uuid and id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, if (approved) "approved" else "rejected")
            statement.setNullableString(2, businessNotes)
            statement.setString(3, userId)
            statement.setString(4, workspaceId)
            statement.setString(5, orderId)
            statement.setString(6, requestId)
            statement.executeUpdate()
        }
    }

    private fun Connection.listOrderSessions(workspaceId: String, orderId: String): List<OrderSessionResponse> {
        val sql = """
            select
                id::text,
                order_id::text,
                order_item_id::text,
                sequence_number,
                title,
                scheduled_at::text,
                status,
                addon_total,
                paid_total,
                notes,
                created_at::text,
                updated_at::text
            from order_sessions
            where workspace_id = ?::uuid and order_id = ?::uuid
            order by sequence_number, created_at
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toOrderSessionResponse())
                }
            }
        }
    }

    private fun Connection.replaceOrderSessions(
        workspaceId: String,
        orderId: String,
        orderType: String,
        requestedSessions: List<OrderSessionRequest>,
        insertedItems: List<InsertedOrderItem>,
        fallbackScheduledAt: String?,
    ) {
        prepareStatement("delete from order_sessions where workspace_id = ?::uuid and order_id = ?::uuid").use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.executeUpdate()
        }
        if (orderType !in setOf("appointment", "service")) return
        val sessions = requestedSessions
            .take(200)
            .mapIndexedNotNull { index, session ->
                session.toPreparedOrderSession(
                    index = index,
                    fallbackScheduledAt = fallbackScheduledAt,
                    defaultTitle = "${orderType.dashboardLabel()} session ${index + 1}",
                )
            }
            .ifEmpty {
                insertedItems.generatedOrderSessions(
                    orderType = orderType,
                    fallbackScheduledAt = fallbackScheduledAt,
                )
            }
        sessions.forEach { session ->
            insertOrderSession(
                workspaceId = workspaceId,
                orderId = orderId,
                session = session,
            )
        }
    }

    private fun Connection.insertOrderSession(
        workspaceId: String,
        orderId: String,
        session: PreparedOrderSession,
    ) {
        val sql = """
            insert into order_sessions (
                workspace_id, order_id, order_item_id, sequence_number, title, scheduled_at,
                status, addon_total, paid_total, notes, updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?::timestamptz, ?, ?, ?, ?, now())
        """.trimIndent()
        prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, orderId)
            statement.setNullableUuid(3, session.orderItemId)
            statement.setInt(4, session.sequenceNumber)
            statement.setString(5, session.title)
            statement.setNullableString(6, session.scheduledAt?.cleanOptional())
            statement.setString(7, session.status)
            statement.setBigDecimal(8, session.addonTotal)
            statement.setBigDecimal(9, session.paidTotal)
            statement.setNullableString(10, session.notes?.cleanOptional())
            statement.executeUpdate()
        }
    }

    private fun Connection.applyOrderInventoryMovement(
        access: DashboardWorkspaceAccess,
        item: PreparedOrderItem,
        quantityDelta: BigDecimal,
        note: String,
    ) {
        if (item.components.isNotEmpty()) {
            applyPackageComponentInventoryMovement(access, item.components, quantityDelta, note)
            return
        }
        val productId = item.productId ?: return
        applyOrderInventoryMovement(
            access = access,
            productId = productId,
            variantId = item.variantId,
            variantTrackStock = item.variantTrackStock,
            quantityDelta = quantityDelta,
            note = note,
        )
    }

    private fun Connection.applyOrderInventoryMovement(
        access: DashboardWorkspaceAccess,
        item: OrderItemResponse,
        quantityDelta: BigDecimal,
        note: String,
    ) {
        val components = item.variantId
            ?.let { loadCatalogPackageComponents(access.workspaceId, listOf(it))[it] }
            .orEmpty()
        if (components.isNotEmpty()) {
            applyPackageComponentInventoryMovement(access, components, quantityDelta, note)
            return
        }
        val productId = item.productId ?: return
        val variantTrackStock = item.variantId
            ?.let { isProductVariantStockTracked(access.workspaceId, it) }
            ?: false
        applyOrderInventoryMovement(
            access = access,
            productId = productId,
            variantId = item.variantId,
            variantTrackStock = variantTrackStock,
            quantityDelta = quantityDelta,
            note = note,
        )
    }

    private fun Connection.applyPackageComponentInventoryMovement(
        access: DashboardWorkspaceAccess,
        components: List<PreparedPackageComponent>,
        quantityDelta: BigDecimal,
        note: String,
    ) {
        components
            .filter { it.itemType == "product" && (it.trackStock || it.variantTrackStock) }
            .forEach { component ->
                val componentQuantityDelta = quantityDelta.multiply(component.quantity).scaled()
                val componentVariantId = component.variantId?.takeIf { component.variantTrackStock }
                if (componentVariantId != null) {
                    applyVariantStockMovement(access, componentVariantId, componentQuantityDelta)
                } else if (component.trackStock) {
                    applyOrderStockMovement(access, component.productId, componentQuantityDelta, note)
                }
            }
    }

    private fun Connection.applyOrderInventoryMovement(
        access: DashboardWorkspaceAccess,
        productId: String,
        variantId: String?,
        variantTrackStock: Boolean,
        quantityDelta: BigDecimal,
        note: String,
    ) {
        if (variantId != null && variantTrackStock) {
            applyVariantStockMovement(access, variantId, quantityDelta)
        } else {
            applyOrderStockMovement(access, productId, quantityDelta, note)
        }
    }

    private fun Connection.isProductVariantStockTracked(
        workspaceId: String,
        variantId: String,
    ): Boolean =
        prepareStatement(
            """
            select track_stock
            from product_variants
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            limit 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, variantId)
            statement.setString(2, workspaceId)
            statement.executeQuery().use { result ->
                result.next() && result.getBoolean("track_stock")
            }
        }

    private fun Connection.applyVariantStockMovement(
        access: DashboardWorkspaceAccess,
        variantId: String,
        quantityDelta: BigDecimal,
    ) {
        val current = prepareStatement(
            """
            select stock_quantity
            from product_variants
            where id = ?::uuid and workspace_id = ?::uuid and track_stock = true and status = 'active'
            for update
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, variantId)
            statement.setString(2, access.workspaceId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getBigDecimal("stock_quantity") else return
            }
        }
        val next = current.add(quantityDelta).scaled()
        if (next < BigDecimal.ZERO) {
            throw DashboardOrderValidationException(
                code = "variant_stock_insufficient",
                publicMessage = "Not enough variant stock is available for this order.",
            )
        }
        prepareStatement(
            """
            update product_variants
            set stock_quantity = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            """.trimIndent(),
        ).use { statement ->
            statement.setBigDecimal(1, next)
            statement.setString(2, variantId)
            statement.setString(3, access.workspaceId)
            statement.executeUpdate()
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
            where id = ?::uuid and workspace_id = ?::uuid and item_type = 'product' and track_stock = true and status = 'active'
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
        if (next < BigDecimal.ZERO) {
            throw DashboardOrderValidationException(
                code = "product_stock_insufficient",
                publicMessage = "Not enough product stock is available for this order.",
            )
        }
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
        insertStockMovement(
            access = access,
            productId = productId,
            movementType = "order",
            quantityDelta = quantityDelta,
            balanceAfter = next,
            note = note,
        )
    }

    private fun Connection.insertStockMovement(
        access: DashboardWorkspaceAccess,
        productId: String,
        movementType: String,
        quantityDelta: BigDecimal,
        balanceAfter: BigDecimal,
        note: String?,
        supplierId: String? = null,
        unitCost: BigDecimal = BigDecimal.ZERO,
        payableAmount: BigDecimal = BigDecimal.ZERO,
        paidAmount: BigDecimal = BigDecimal.ZERO,
        expiryDate: String? = null,
    ): StockMovementResponse {
        val sql = """
            insert into stock_movements (
                workspace_id, product_id, movement_type, quantity_delta,
                balance_after, note, created_by_user_id, supplier_id,
                unit_cost, payable_amount, paid_amount, expiry_date
            )
            values (?::uuid, ?::uuid, ?, ?, ?, ?, ?::uuid, ?::uuid, ?, ?, ?, ?::date)
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
            statement.setNullableUuid(8, supplierId)
            statement.setBigDecimal(9, unitCost.scaled())
            statement.setBigDecimal(10, payableAmount.scaled())
            statement.setBigDecimal(11, paidAmount.scaled())
            statement.setNullableString(12, expiryDate)
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
            max(c.name) as customer_name,
            max(c.phone_number) as customer_phone_number,
            max(c.email) as customer_email,
            max(c.tax_number) as customer_tax_number,
            max(c.address_line) as customer_address_line,
            max(c.city) as customer_city,
            max(c.region) as customer_region,
            max(c.country) as customer_country,
            max(c.postal_code) as customer_postal_code,
            o.order_type,
            o.status,
            o.scheduled_at::text,
            o.subtotal,
            o.tax_total,
            o.discount_total,
            o.paid_total,
            o.total,
            o.currency,
            o.notes,
            o.fulfillment_type,
            o.payment_mode,
            o.source,
            o.created_at::text,
            o.updated_at::text,
            count(oi.id)::int as item_count,
            count(*) over()::int as total_count
        from orders o
        left join customers c on c.id = o.customer_id
        left join order_items oi on oi.order_id = o.id
        """.trimIndent()

    private fun PreparedStatement.bindCustomerRequest(request: CustomerRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setNullableString(startIndex + 1, request.phoneNumber?.cleanOptional())
        setNullableString(startIndex + 2, request.email?.cleanOptional()?.lowercase())
        setNullableString(startIndex + 3, request.taxNumber?.cleanOptionalUpper())
        setNullableString(startIndex + 4, request.addressLine?.cleanOptional())
        setNullableString(startIndex + 5, request.city?.cleanOptional())
        setNullableString(startIndex + 6, request.region?.cleanOptional())
        setNullableString(startIndex + 7, request.country?.cleanOptional())
        setNullableString(startIndex + 8, request.postalCode?.cleanOptional())
        setNullableString(startIndex + 9, request.notes?.cleanOptional())
    }

    private fun PreparedStatement.bindSupplierRequest(request: SupplierRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setNullableString(startIndex + 1, request.phoneNumber?.cleanOptional())
        setNullableString(startIndex + 2, request.email?.cleanOptional()?.lowercase())
        setNullableString(startIndex + 3, request.taxNumber?.cleanOptionalUpper())
        setNullableString(startIndex + 4, request.addressLine?.cleanOptional())
        setNullableString(startIndex + 5, request.paymentTerms?.cleanOptional())
        setNullableString(startIndex + 6, request.paymentMode?.cleanOptional())
        setNullableString(startIndex + 7, request.paymentReference?.cleanOptional())
        setBigDecimal(startIndex + 8, request.payableTotal.orEmpty().moneyOrZero())
        setBigDecimal(startIndex + 9, request.paidTotal.orEmpty().moneyOrZero())
        setString(startIndex + 10, request.currency?.cleanOptionalUpper() ?: "INR")
        setNullableString(startIndex + 11, request.lastPaymentAt?.cleanOptional())
        setNullableString(startIndex + 12, request.notes?.cleanOptional())
    }

    private fun PreparedStatement.bindProductRequest(
        access: DashboardWorkspaceAccess,
        request: ProductRequest,
        supplierId: String?,
        categoryId: String?,
        startIndex: Int,
    ) {
        val itemType = request.itemType.cleanItemType()
        val trackStock = itemType == "product" && request.trackStock
        val stockQuantity = if (trackStock) request.stockQuantity.decimalOrZero() else BigDecimal.ZERO
        val reorderLevel = if (trackStock) request.reorderLevel.decimalOrZero() else BigDecimal.ZERO
        val duration = request.durationMinutes?.takeIf { it > 0 }?.coerceAtMost(1440)
        setNullableUuid(startIndex, supplierId)
        setNullableUuid(startIndex + 1, categoryId)
        setString(startIndex + 2, request.name.cleanName())
        setString(startIndex + 3, itemType)
        setNullableString(startIndex + 4, request.sku?.cleanOptionalUpper())
        setNullableString(startIndex + 5, request.barcode?.cleanOptional())
        setNullableString(startIndex + 6, request.description?.cleanOptional())
        setString(startIndex + 7, request.unit.cleanOptional() ?: if (itemType == "product") "pcs" else "service")
        setBigDecimal(startIndex + 8, request.sellingPrice.moneyOrZero())
        setBigDecimal(startIndex + 9, request.costPrice.moneyOrZero())
        setString(startIndex + 10, request.currency?.cleanOptionalUpper() ?: access.currency)
        setBigDecimal(startIndex + 11, request.taxRate.decimalOrZero())
        setBoolean(startIndex + 12, request.pricesIncludeTax)
        setBigDecimal(startIndex + 13, stockQuantity)
        setBigDecimal(startIndex + 14, reorderLevel)
        setBoolean(startIndex + 15, trackStock)
        if (duration == null) {
            setNull(startIndex + 16, Types.INTEGER)
        } else {
            setInt(startIndex + 16, duration)
        }
        setBoolean(startIndex + 17, itemType == "appointment" || request.bookingRequired)
        setNullableString(startIndex + 18, if (itemType == "product") request.expiryDate.cleanIsoDateOrNull() else null)
        setString(startIndex + 19, request.status.cleanCatalogStatus())
    }

    private fun PreparedStatement.bindPrinterRequest(request: PrinterProfileRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setString(startIndex + 1, request.connectionType.cleanPrinterConnectionType())
        setNullableString(startIndex + 2, request.address?.cleanOptional())
        setInt(startIndex + 3, request.paperWidthMm.coerceIn(40, 120))
        setInt(startIndex + 4, request.dpi.coerceIn(120, 600))
        setBoolean(startIndex + 5, request.supportsReceipts)
        setBoolean(startIndex + 6, request.supportsBarcodes)
        setBoolean(startIndex + 7, request.isDefaultReceipt && request.supportsReceipts)
        setBoolean(startIndex + 8, request.isDefaultBarcode && request.supportsBarcodes)
        setBoolean(startIndex + 9, request.printLogo)
        setString(startIndex + 10, request.headerAlignment.cleanPrinterHeaderAlignment())
        setBoolean(startIndex + 11, request.showBusinessAddress)
        setBoolean(startIndex + 12, request.showCatalogQr)
        setBoolean(startIndex + 13, request.showTimedGreeting)
        setNullableString(startIndex + 14, request.notes?.cleanOptional())
    }

    private fun ResultSet.toProductCategoryResponse(): ProductCategoryResponse =
        ProductCategoryResponse(
            id = getString("id"),
            name = getString("name"),
            itemType = getString("item_type") ?: "all",
            sortOrder = getInt("sort_order"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toProductOfferResponse(): ProductOfferResponse =
        ProductOfferResponse(
            id = getString("id"),
            appliesTo = getString("applies_to"),
            productId = getString("product_id"),
            productName = getString("product_name"),
            categoryId = getString("category_id"),
            categoryName = getString("category_name"),
            customerId = getString("customer_id"),
            customerName = getString("customer_name"),
            name = getString("name"),
            description = getString("description"),
            discountType = getString("discount_type"),
            discountValue = getBigDecimal("discount_value").decimalString(),
            discountCapAmount = getBigDecimal("discount_cap_amount")?.decimalString(),
            couponCode = getString("coupon_code"),
            startsAt = getString("starts_at"),
            endsAt = getString("ends_at"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toWorkspacePaymentMethodResponse(): WorkspacePaymentMethodResponse =
        WorkspacePaymentMethodResponse(
            id = getString("id"),
            type = getString("type"),
            label = getString("label"),
            upiId = getString("upi_id"),
            payeeName = getString("payee_name"),
            isDefault = getBoolean("is_default"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toPublicCatalogPaymentMethodResponse(): PublicCatalogPaymentMethodResponse =
        PublicCatalogPaymentMethodResponse(
            id = getString("id"),
            type = getString("type"),
            label = getString("label"),
            upiId = getString("upi_id"),
            payeeName = getString("payee_name"),
            isDefault = getBoolean("is_default"),
        )

    private fun ResultSet.toPublicCatalogProductResponse(
        trackStock: Boolean,
        stockQuantity: BigDecimal,
    ): PublicCatalogProductResponse {
        val price = getBigDecimal("selling_price") ?: BigDecimal.ZERO
        val discountType = getString("discount_type")
        val discountValue = getBigDecimal("discount_value") ?: BigDecimal.ZERO
        val discountCapAmount = getBigDecimal("discount_cap_amount")
        val discount = if (getString("offer_id") == null) {
            BigDecimal.ZERO
        } else {
            discountAmount(price, discountType, discountValue, discountCapAmount)
        }
        val finalPrice = price.subtract(discount).coerceAtLeast(BigDecimal.ZERO).scaled()
        return PublicCatalogProductResponse(
            id = getString("id"),
            categoryId = getString("category_id"),
            categoryName = getString("category_name"),
            name = getString("name"),
            itemType = getString("item_type") ?: "product",
            description = getString("description"),
            unit = getString("unit"),
            sellingPrice = price.moneyString(),
            currency = getString("currency") ?: "INR",
            taxRate = getBigDecimal("tax_rate").decimalString(),
            pricesIncludeTax = getBoolean("prices_include_tax"),
            trackStock = trackStock,
            stockQuantity = stockQuantity.decimalString(),
            inStock = !trackStock || stockQuantity > BigDecimal.ZERO,
            durationMinutes = getNullableInt("duration_minutes"),
            bookingRequired = getBoolean("booking_required"),
            imageUrl = getString("image_storage_path").toDashboardMediaUrl(),
            offer = getString("offer_id")?.let {
                PublicCatalogOfferResponse(
                    id = it,
                    name = getString("offer_name"),
                    description = getString("offer_description"),
                    discountType = discountType ?: "percentage",
                    discountValue = discountValue.decimalString(),
                    discountCapAmount = discountCapAmount?.moneyString(),
                    discountAmount = discount.moneyString(),
                    finalPrice = finalPrice.moneyString(),
                )
            },
        )
    }

    private fun ResultSet.toCustomerResponse(): CustomerResponse =
        CustomerResponse(
            id = getString("id"),
            name = getString("name"),
            phoneNumber = getString("phone_number"),
            email = getString("email"),
            taxNumber = getString("tax_number"),
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

    private fun ResultSet.toSupplierResponse(): SupplierResponse {
        val payableTotal = getBigDecimal("payable_total") ?: BigDecimal.ZERO
        val paidTotal = getBigDecimal("paid_total") ?: BigDecimal.ZERO
        val balanceDue = (payableTotal - paidTotal).coerceAtLeast(BigDecimal.ZERO)
        return SupplierResponse(
            id = getString("id"),
            name = getString("name"),
            phoneNumber = getString("phone_number"),
            email = getString("email"),
            taxNumber = getString("tax_number"),
            addressLine = getString("address_line"),
            paymentTerms = getString("payment_terms"),
            paymentMode = getString("payment_mode"),
            paymentReference = getString("payment_reference"),
            payableTotal = payableTotal.moneyString(),
            paidTotal = paidTotal.moneyString(),
            balanceDue = balanceDue.moneyString(),
            currency = getString("currency") ?: "INR",
            lastPaymentAt = getString("last_payment_at"),
            notes = getString("notes"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )
    }

    private fun ResultSet.toProductResponse(): ProductResponse {
        val stockQuantity = getBigDecimal("stock_quantity") ?: BigDecimal.ZERO
        val reorderLevel = getBigDecimal("reorder_level") ?: BigDecimal.ZERO
        val trackStock = getBoolean("track_stock")
        return ProductResponse(
            id = getString("id"),
            categoryId = getString("category_id"),
            categoryName = getString("category_name"),
            supplierId = getString("supplier_id"),
            supplierName = getString("supplier_name"),
            name = getString("name"),
            itemType = getString("item_type") ?: "product",
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
            durationMinutes = getNullableInt("duration_minutes"),
            bookingRequired = getBoolean("booking_required"),
            expiryDate = getString("expiry_date"),
            lowStock = trackStock && stockQuantity <= reorderLevel,
            status = getString("status"),
            imageUrl = getString("image_storage_path").toDashboardMediaUrl(),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )
    }

    private fun ResultSet.toProductVariantResponse(): ProductVariantResponse =
        ProductVariantResponse(
            id = getString("id"),
            productId = getString("product_id"),
            name = getString("name"),
            sku = getString("sku"),
            barcode = getString("barcode"),
            sellingPrice = getBigDecimal("selling_price").moneyString(),
            costPrice = getBigDecimal("cost_price").moneyString(),
            stockQuantity = getBigDecimal("stock_quantity").decimalString(),
            trackStock = getBoolean("track_stock"),
            durationMinutes = getNullableInt("duration_minutes"),
            includedQuantity = (getNullableInt("included_quantity") ?: 1).coerceAtLeast(1),
            addons = getString("addons_json").parseVariantAddons(),
            status = getString("status"),
        )

    private fun List<ProductVariantAddonRequest>.cleanVariantAddons(): List<ProductVariantAddonResponse> =
        take(20).mapNotNull { addon ->
            val name = addon.name.cleanOptional()?.take(80) ?: return@mapNotNull null
            ProductVariantAddonResponse(
                name = name,
                sellingPrice = addon.sellingPrice?.cleanOptional()?.moneyOrZero()?.moneyString() ?: "0.00",
                costPrice = addon.costPrice?.cleanOptional()?.moneyOrZero()?.moneyString() ?: "0.00",
                durationMinutes = addon.durationMinutes?.takeIf { it > 0 }?.coerceAtMost(1440),
                status = addon.status.cleanCatalogStatus(),
            )
        }

    private fun String?.parseVariantAddons(): List<ProductVariantAddonResponse> =
        if (isNullOrBlank()) {
            emptyList()
        } else {
            runCatching {
                DashboardRepositoryJson.decodeFromString<List<ProductVariantAddonResponse>>(this)
                    .filter { it.name.isNotBlank() && it.status != "archived" }
            }.getOrDefault(emptyList())
        }

    private fun ResultSet.toStockMovementResponse(): StockMovementResponse =
        StockMovementResponse(
            id = getString("id"),
            productId = getString("product_id"),
            movementType = getString("movement_type"),
            quantityDelta = getBigDecimal("quantity_delta").decimalString(),
            balanceAfter = getBigDecimal("balance_after").decimalString(),
            supplierId = getString("supplier_id"),
            unitCost = getBigDecimal("unit_cost").moneyString(),
            payableAmount = getBigDecimal("payable_amount").moneyString(),
            paidAmount = getBigDecimal("paid_amount").moneyString(),
            expiryDate = getString("expiry_date"),
            note = getString("note"),
            createdAt = getString("created_at"),
        )

    private fun ResultSet.toPrinterProfileResponse(): PrinterProfileResponse =
        PrinterProfileResponse(
            id = getString("id"),
            name = getString("name"),
            connectionType = getString("connection_type"),
            address = getString("address"),
            paperWidthMm = getInt("paper_width_mm"),
            dpi = getInt("dpi"),
            supportsReceipts = getBoolean("supports_receipts"),
            supportsBarcodes = getBoolean("supports_barcodes"),
            isDefaultReceipt = getBoolean("is_default_receipt"),
            isDefaultBarcode = getBoolean("is_default_barcode"),
            printLogo = getBoolean("print_logo"),
            headerAlignment = getString("header_alignment") ?: "center",
            showBusinessAddress = getBoolean("show_business_address"),
            showCatalogQr = getBoolean("show_catalog_qr"),
            showTimedGreeting = getBoolean("show_timed_greeting"),
            notes = getString("notes"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.toOrderResponse(
        items: List<OrderItemResponse>,
        sessions: List<OrderSessionResponse>,
        changeRequests: List<OrderChangeRequestResponse> = emptyList(),
    ): OrderResponse {
        val status = getString("status")
        val total = getBigDecimal("total")
        val paidTotal = effectivePaidTotal(
            status = status,
            paidTotal = getBigDecimal("paid_total"),
            total = total,
        )
        return OrderResponse(
            id = getString("id"),
            orderNumber = getString("order_number"),
            customerId = getString("customer_id"),
            customerName = getString("customer_name"),
            customerPhoneNumber = getString("customer_phone_number"),
            customerEmail = getString("customer_email"),
            customerTaxNumber = getString("customer_tax_number"),
            customerAddressLine = getString("customer_address_line"),
            customerCity = getString("customer_city"),
            customerRegion = getString("customer_region"),
            customerCountry = getString("customer_country"),
            customerPostalCode = getString("customer_postal_code"),
            orderType = getString("order_type") ?: "sale",
            status = status,
            scheduledAt = getString("scheduled_at"),
            subtotal = getBigDecimal("subtotal").moneyString(),
            taxTotal = getBigDecimal("tax_total").moneyString(),
            discountTotal = getBigDecimal("discount_total").moneyString(),
            paidTotal = paidTotal.moneyString(),
            total = total.moneyString(),
            currency = getString("currency"),
            notes = getString("notes"),
            fulfillmentType = getString("fulfillment_type") ?: "standard",
            paymentMode = getString("payment_mode") ?: "pay_on_spot",
            source = getString("source") ?: "dashboard",
            itemCount = getInt("item_count"),
            items = items,
            sessions = sessions,
            changeRequests = changeRequests,
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )
    }

    private fun ResultSet.toOrderChangeRequestResponse(): OrderChangeRequestResponse {
        val paidTotal = getBigDecimal("requested_paid_total")
        val requestedPaidTotal = if (wasNull()) null else paidTotal.moneyString()
        return OrderChangeRequestResponse(
            id = getString("id"),
            status = getString("status") ?: "pending",
            items = getString("requested_items")
                ?.takeIf { it.isNotBlank() }
                ?.let { json ->
                    runCatching {
                        DashboardRepositoryJson.decodeFromString<List<OrderChangeRequestItemResponse>>(json)
                    }.getOrDefault(emptyList())
                }
                .orEmpty(),
            requestedPaymentMode = getString("requested_payment_mode"),
            requestedPaidTotal = requestedPaidTotal,
            paymentReference = getString("payment_reference"),
            customerNotes = getString("customer_notes"),
            businessNotes = getString("business_notes"),
            resolvedAt = getString("resolved_at"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )
    }

    private fun ResultSet.toOrderItemResponse(): OrderItemResponse =
        OrderItemResponse(
            id = getString("id"),
            productId = getString("product_id"),
            productName = getString("product_name"),
            imageUrl = getString("image_storage_path").toDashboardMediaUrl(),
            variantId = getString("variant_id"),
            variantName = getString("variant_name"),
            description = getString("description"),
            quantity = getBigDecimal("quantity").decimalString(),
            unitPrice = getBigDecimal("unit_price").moneyString(),
            taxRate = getBigDecimal("tax_rate").decimalString(),
            lineSubtotal = getBigDecimal("line_subtotal").moneyString(),
            lineTax = getBigDecimal("line_tax").moneyString(),
            lineTotal = getBigDecimal("line_total").moneyString(),
        )

    private fun Connection.prepareOrderChangeRequestItems(
        order: OrderResponse,
        request: OrderChangeRequestRequest,
    ): List<OrderChangeRequestItemResponse> {
        val orderItemsById = order.items.associateBy { it.id }
        return request.items
            .mapNotNull { item ->
                val cleanItemId = item.orderItemId.cleanUuidOrNull() ?: return@mapNotNull null
                val existing = orderItemsById[cleanItemId] ?: return@mapNotNull null
                val requestedQuantity = item.quantity.decimalOrZero().coerceAtLeast(BigDecimal.ZERO).scaled()
                val previousQuantity = existing.quantity.decimalOrZero().scaled()
                if (requestedQuantity == previousQuantity) return@mapNotNull null
                val lineTotal = requestedQuantity
                    .multiply(existing.unitPrice.moneyOrZero())
                    .scaled()
                OrderChangeRequestItemResponse(
                    orderItemId = existing.id,
                    productId = existing.productId,
                    variantId = existing.variantId,
                    name = existing.productName
                        ?.takeIf { it.isNotBlank() }
                        ?: existing.variantName?.takeIf { it.isNotBlank() }
                        ?: existing.description.ifBlank { "Line item" },
                    previousQuantity = previousQuantity.decimalString(),
                    requestedQuantity = requestedQuantity.decimalString(),
                    unitPrice = existing.unitPrice,
                    lineTotal = lineTotal.moneyString(),
                )
            }
    }

    private fun OrderResponse.toOrderRequestForChangeApproval(
        items: List<OrderItemRequest>,
        paidTotal: String,
        paymentMode: String,
        businessNotes: String?,
        changeRequest: OrderChangeRequestResponse,
    ): OrderRequest =
        OrderRequest(
            customerId = customerId,
            customerName = customerName,
            customerPhoneNumber = customerPhoneNumber,
            customerEmail = customerEmail,
            customerTaxNumber = customerTaxNumber,
            customerAddressLine = customerAddressLine,
            customerCity = customerCity,
            customerRegion = customerRegion,
            customerCountry = customerCountry,
            customerPostalCode = customerPostalCode,
            orderType = orderType,
            status = status,
            scheduledAt = scheduledAt,
            paidTotal = paidTotal,
            discountTotal = discountTotal,
            currency = currency,
            notes = appendOrderChangeApprovalNotes(
                existing = notes,
                changeRequest = changeRequest,
                businessNotes = businessNotes,
            ),
            fulfillmentType = fulfillmentType,
            paymentMode = paymentMode,
            source = source,
            items = items,
            sessions = sessions.map { session ->
                OrderSessionRequest(
                    id = session.id,
                    orderItemId = null,
                    sequenceNumber = session.sequenceNumber,
                    title = session.title,
                    scheduledAt = session.scheduledAt,
                    status = session.status,
                    addonTotal = session.addonTotal,
                    paidTotal = session.paidTotal,
                    notes = session.notes,
                )
            },
        )

    private fun ResultSet.toOrderSessionResponse(): OrderSessionResponse =
        OrderSessionResponse(
            id = getString("id"),
            orderId = getString("order_id"),
            orderItemId = getString("order_item_id"),
            sequenceNumber = getInt("sequence_number"),
            title = getString("title"),
            scheduledAt = getString("scheduled_at"),
            status = getString("status"),
            addonTotal = getBigDecimal("addon_total").moneyString(),
            paidTotal = getBigDecimal("paid_total").moneyString(),
            notes = getString("notes"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
        )

    private fun ResultSet.getNullableInt(column: String): Int? {
        val value = getInt(column)
        return if (wasNull()) null else value
    }

    private fun OrderItemRequest.toPreparedOrderItem(): PreparedOrderItem {
        val quantity = quantity.decimalOrZero().takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
        val unitPrice = unitPrice.moneyOrZero()
        val taxRate = taxRate.decimalOrZero()
        val lineSubtotal = quantity.multiply(unitPrice).scaled()
        val lineTax = lineSubtotal.multiply(taxRate).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP).scaled()
        return PreparedOrderItem(
            productId = productId.cleanUuidOrNull(),
            variantId = variantId.cleanUuidOrNull(),
            variantName = null,
            description = description.cleanName(),
            quantity = quantity,
            unitPrice = unitPrice,
            taxRate = taxRate,
            lineSubtotal = lineSubtotal,
            lineTax = lineTax,
            lineTotal = lineSubtotal.add(lineTax).scaled(),
        )
    }

    private fun Connection.validateOrderCatalogItemTypes(
        workspaceId: String,
        orderType: String,
        items: List<PreparedOrderItem>,
    ) {
        val productIds = items.mapNotNull { it.productId }.distinct()
        if (productIds.isEmpty()) return

        val expectedItemType = orderType.expectedCatalogItemType()
        val placeholders = productIds.joinToString(", ") { "?::uuid" }
        val products = mutableMapOf<String, Pair<String, String>>()
        prepareStatement(
            """
            select id::text, item_type, name
            from products
            where workspace_id = ?::uuid and status = 'active' and id in ($placeholders)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            productIds.forEachIndexed { index, productId ->
                statement.setString(index + 2, productId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    products[result.getString("id")] = result.getString("item_type") to result.getString("name")
                }
            }
        }

        if (productIds.any { it !in products }) {
            throw DashboardOrderValidationException(
                code = "order_item_catalog_missing",
                publicMessage = "One or more selected catalog items are no longer available.",
            )
        }
        val variantIds = items.mapNotNull { it.variantId }.distinct()
        if (variantIds.isNotEmpty()) {
            val variantPlaceholders = variantIds.joinToString(", ") { "?::uuid" }
            val variants = mutableMapOf<String, CatalogVariant>()
            val componentsByVariant = loadCatalogPackageComponents(workspaceId, variantIds)
            prepareStatement(
                """
                select id::text, product_id::text, name, track_stock, included_quantity
                from product_variants
                where workspace_id = ?::uuid and status = 'active' and id in ($variantPlaceholders)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, workspaceId)
                variantIds.forEachIndexed { index, variantId ->
                    statement.setString(index + 2, variantId)
                }
                statement.executeQuery().use { result ->
                    while (result.next()) {
                        variants[result.getString("id")] = CatalogVariant(
                            productId = result.getString("product_id"),
                            name = result.getString("name"),
                            trackStock = result.getBoolean("track_stock"),
                            includedQuantity = result.getInt("included_quantity").coerceAtLeast(1),
                            components = componentsByVariant[result.getString("id")].orEmpty(),
                        )
                    }
                }
            }
            items.forEach { item ->
                val variantId = item.variantId ?: return@forEach
                val variant = variants[variantId]
                if (variant == null || variant.productId != item.productId) {
                    throw DashboardOrderValidationException(
                        code = "order_item_variant_missing",
                        publicMessage = "One or more selected item options are no longer available.",
                    )
                }
                item.variantName = variant.name
                item.variantTrackStock = variant.trackStock
                item.includedQuantity = variant.includedQuantity
                item.components = variant.components
            }
        }
        val wrongType = products.values.firstOrNull { (itemType, _) -> itemType != expectedItemType } ?: return
        throw DashboardOrderValidationException(
            code = "order_item_type_mismatch",
            publicMessage = orderType.orderCatalogMismatchMessage(wrongType.second),
        )
    }

    private fun Connection.publicCatalogInventoryHasCapacity(
        workspaceId: String,
        items: List<PreparedOrderItem>,
    ): Boolean {
        val requirements = items.publicCatalogInventoryRequirements()
        if (requirements.isEmpty()) return true

        lockPublicCatalogInventoryRows(
            workspaceId = workspaceId,
            productIds = requirements.map { it.productId }.distinct(),
            variantIds = requirements.mapNotNull { it.variantId }.distinct(),
        )
        val productStock = loadProductStockStates(workspaceId, requirements.map { it.productId }.distinct())
        val variantStock = loadVariantStockStates(workspaceId, requirements.mapNotNull { it.variantId }.distinct())

        return requirements.all { requirement ->
            val product = productStock[requirement.productId] ?: return@all false
            val availableStock = if (requirement.variantScoped) {
                val variant = requirement.variantId?.let { variantStock[it] } ?: return@all false
                if (!variant.trackStock) return@all true
                variant.stockQuantity
            } else {
                if (!product.trackStock) return@all true
                product.stockQuantity
            }
            val reserved = publicCatalogReservedQuantity(
                workspaceId = workspaceId,
                productId = requirement.productId,
                variantId = requirement.variantId,
                variantScoped = requirement.variantScoped,
            )
            requirement.quantity <= availableStock.subtract(reserved)
        }
    }

    private fun List<PreparedOrderItem>.publicCatalogInventoryRequirements(): List<InventoryRequirement> {
        val requirements = linkedMapOf<InventoryRequirementKey, BigDecimal>()
        fun addRequirement(productId: String?, variantId: String?, variantTrackStock: Boolean, quantity: BigDecimal) {
            val cleanProductId = productId ?: return
            if (quantity <= BigDecimal.ZERO) return
            if (variantId != null && variantTrackStock) {
                val variantKey = InventoryRequirementKey(cleanProductId, variantId, variantScoped = true)
                requirements[variantKey] = requirements[variantKey]?.add(quantity) ?: quantity
            } else {
                val productKey = InventoryRequirementKey(cleanProductId, null, variantScoped = false)
                requirements[productKey] = requirements[productKey]?.add(quantity) ?: quantity
            }
        }
        forEach { item ->
            if (item.components.isNotEmpty()) {
                item.components
                    .filter { it.itemType == "product" && (it.trackStock || it.variantTrackStock) }
                    .forEach { component ->
                        addRequirement(
                            productId = component.productId,
                            variantId = component.variantId,
                            variantTrackStock = component.variantTrackStock,
                            quantity = item.quantity.multiply(component.quantity).scaled(),
                        )
                    }
            } else {
                addRequirement(
                    productId = item.productId,
                    variantId = item.variantId,
                    variantTrackStock = item.variantTrackStock,
                    quantity = item.quantity.scaled(),
                )
            }
        }
        return requirements.map { (key, quantity) ->
            InventoryRequirement(
                productId = key.productId,
                variantId = key.variantId,
                variantScoped = key.variantScoped,
                quantity = quantity.scaled(),
            )
        }
    }

    private fun Connection.lockPublicCatalogInventoryRows(
        workspaceId: String,
        productIds: List<String>,
        variantIds: List<String>,
    ) {
        productIds.sorted().forEach { productId ->
            prepareStatement(
                """
                select id
                from products
                where workspace_id = ?::uuid and id = ?::uuid
                for update
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, workspaceId)
                statement.setString(2, productId)
                statement.executeQuery().use { result -> result.next() }
            }
        }
        variantIds.sorted().forEach { variantId ->
            prepareStatement(
                """
                select id
                from product_variants
                where workspace_id = ?::uuid and id = ?::uuid
                for update
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, workspaceId)
                statement.setString(2, variantId)
                statement.executeQuery().use { result -> result.next() }
            }
        }
    }

    private fun Connection.loadProductStockStates(
        workspaceId: String,
        productIds: List<String>,
    ): Map<String, ProductStockState> {
        if (productIds.isEmpty()) return emptyMap()
        val placeholders = productIds.distinct().joinToString(", ") { "?::uuid" }
        val states = mutableMapOf<String, ProductStockState>()
        prepareStatement(
            """
            select id::text, track_stock, stock_quantity
            from products
            where workspace_id = ?::uuid and id in ($placeholders) and status = 'active'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            productIds.distinct().forEachIndexed { index, productId ->
                statement.setString(index + 2, productId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    states[result.getString("id")] = ProductStockState(
                        trackStock = result.getBoolean("track_stock"),
                        stockQuantity = result.getBigDecimal("stock_quantity") ?: BigDecimal.ZERO,
                    )
                }
            }
        }
        return states
    }

    private fun Connection.loadVariantStockStates(
        workspaceId: String,
        variantIds: List<String>,
    ): Map<String, VariantStockState> {
        if (variantIds.isEmpty()) return emptyMap()
        val placeholders = variantIds.distinct().joinToString(", ") { "?::uuid" }
        val states = mutableMapOf<String, VariantStockState>()
        prepareStatement(
            """
            select id::text, track_stock, stock_quantity
            from product_variants
            where workspace_id = ?::uuid and id in ($placeholders) and status = 'active'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            variantIds.distinct().forEachIndexed { index, variantId ->
                statement.setString(index + 2, variantId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    states[result.getString("id")] = VariantStockState(
                        trackStock = result.getBoolean("track_stock"),
                        stockQuantity = result.getBigDecimal("stock_quantity") ?: BigDecimal.ZERO,
                    )
                }
            }
        }
        return states
    }

    private fun Connection.publicCatalogReservedQuantity(
        workspaceId: String,
        productId: String,
        variantId: String?,
        variantScoped: Boolean,
    ): BigDecimal {
        val directVariantClause = if (variantScoped) {
            "and oi.variant_id is not distinct from ?::uuid"
        } else {
            ""
        }
        val componentVariantClause = if (variantScoped) {
            "and pvc.component_variant_id is not distinct from ?::uuid"
        } else {
            ""
        }
        val sql = """
            select coalesce(sum(reserved_quantity), 0) as reserved_quantity
            from (
                select oi.quantity as reserved_quantity
                from order_items oi
                join orders o on o.id = oi.order_id
                where o.workspace_id = ?::uuid
                  and o.source = 'public_catalog'
                  and o.status = 'new'
                  and o.inventory_applied = false
                  and oi.product_id = ?::uuid
                  $directVariantClause
                union all
                select oi.quantity * pvc.quantity as reserved_quantity
                from order_items oi
                join orders o on o.id = oi.order_id
                join product_variant_components pvc
                  on pvc.workspace_id = o.workspace_id
                 and pvc.package_variant_id = oi.variant_id
                 and pvc.status = 'active'
                where o.workspace_id = ?::uuid
                  and o.source = 'public_catalog'
                  and o.status = 'new'
                  and o.inventory_applied = false
                  and pvc.component_product_id = ?::uuid
                  $componentVariantClause
            ) reservations
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            var index = 1
            statement.setString(index++, workspaceId)
            statement.setString(index++, productId)
            if (variantScoped) statement.setNullableUuid(index++, variantId)
            statement.setString(index++, workspaceId)
            statement.setString(index++, productId)
            if (variantScoped) statement.setNullableUuid(index, variantId)
            statement.executeQuery().use { result ->
                if (result.next()) result.getBigDecimal("reserved_quantity") ?: BigDecimal.ZERO else BigDecimal.ZERO
            }
        }
    }

    private fun Connection.loadCatalogPackageComponents(
        workspaceId: String,
        variantIds: List<String>,
    ): Map<String, List<PreparedPackageComponent>> {
        if (variantIds.isEmpty()) return emptyMap()
        val cleanVariantIds = variantIds.distinct()
        val placeholders = cleanVariantIds.joinToString(", ") { "?::uuid" }
        val componentsByVariant = mutableMapOf<String, MutableList<PreparedPackageComponent>>()
        prepareStatement(
            """
            select
                pvc.package_variant_id::text,
                pvc.component_product_id::text,
                p.name as product_name,
                p.item_type,
                p.unit,
                p.track_stock,
                p.stock_quantity as product_stock_quantity,
                pvc.component_variant_id::text,
                pv.name as variant_name,
                pv.stock_quantity as variant_stock_quantity,
                pv.track_stock as variant_track_stock,
                pvc.quantity
            from product_variant_components pvc
            join products p on p.id = pvc.component_product_id and p.workspace_id = pvc.workspace_id
            left join product_variants pv on pv.id = pvc.component_variant_id and pv.workspace_id = pvc.workspace_id
            where pvc.workspace_id = ?::uuid
                and pvc.package_variant_id in ($placeholders)
                and pvc.status = 'active'
                and p.status = 'active'
                and (pv.id is null or pv.status = 'active')
            order by pvc.package_variant_id, pvc.sort_order asc, p.name asc
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, workspaceId)
            cleanVariantIds.forEachIndexed { index, variantId ->
                statement.setString(index + 2, variantId)
            }
            statement.executeQuery().use { result ->
                while (result.next()) {
                    val packageVariantId = result.getString("package_variant_id")
                    val variantStockQuantity = result.getBigDecimal("variant_stock_quantity")
                    val componentVariantId = result.getString("component_variant_id")
                    val variantTrackStock = componentVariantId != null && result.getBoolean("variant_track_stock")
                    val productTrackStock = result.getBoolean("track_stock")
                    val productStockQuantity = result.getBigDecimal("product_stock_quantity") ?: BigDecimal.ZERO
                    val stockQuantity = when {
                        componentVariantId != null && variantTrackStock -> variantStockQuantity ?: BigDecimal.ZERO
                        productTrackStock -> productStockQuantity
                        componentVariantId != null -> variantStockQuantity ?: BigDecimal.ZERO
                        else -> productStockQuantity
                    }
                    componentsByVariant.getOrPut(packageVariantId) { mutableListOf() }.add(
                        PreparedPackageComponent(
                            productId = result.getString("component_product_id"),
                            productName = result.getString("product_name"),
                            itemType = result.getString("item_type").cleanItemType(),
                            unit = result.getString("unit") ?: "pcs",
                            trackStock = productTrackStock,
                            variantTrackStock = variantTrackStock,
                            stockQuantity = stockQuantity,
                            variantId = componentVariantId,
                            variantName = result.getString("variant_name"),
                            quantity = result.getBigDecimal("quantity")
                                .takeIf { it > BigDecimal.ZERO }
                                ?: BigDecimal.ONE,
                        ),
                    )
                }
            }
        }
        return componentsByVariant
    }

    private fun OrderSessionRequest.toPreparedOrderSession(
        index: Int,
        fallbackScheduledAt: String?,
        defaultTitle: String,
    ): PreparedOrderSession? {
        val sequence = sequenceNumber.takeIf { it > 0 } ?: (index + 1)
        val title = title.cleanOptional()
            ?: defaultTitle.take(120)
        return PreparedOrderSession(
            orderItemId = orderItemId.cleanUuidOrNull(),
            sequenceNumber = sequence,
            title = title,
            scheduledAt = scheduledAt?.cleanOptional() ?: fallbackScheduledAt?.cleanOptional()?.takeIf { index == 0 },
            status = status.cleanOrderSessionStatus(),
            addonTotal = addonTotal.moneyOrZero(),
            paidTotal = paidTotal.moneyOrZero(),
            notes = notes?.cleanOptional(),
        )
    }

    private fun List<InsertedOrderItem>.generatedOrderSessions(
        orderType: String,
        fallbackScheduledAt: String?,
    ): List<PreparedOrderSession> {
        if (isEmpty()) return emptyList()
        var sequence = 1
        return buildList {
            this@generatedOrderSessions.forEach { inserted ->
                val componentSessions = inserted.item.components
                    .filter { it.itemType in setOf("appointment", "service") }
                if (componentSessions.isNotEmpty()) {
                    componentSessions.forEach { component ->
                        val sessionCount = component.packageComponentSessionCount(inserted.item.quantity).coerceAtLeast(1)
                        val lineTitle = buildList {
                            add(component.productName)
                            component.variantName?.cleanOptional()?.let(::add)
                        }.joinToString(" - ").ifBlank {
                            inserted.item.variantName?.cleanOptional()
                                ?: inserted.item.description.cleanOptional()
                                ?: orderType.dashboardLabel()
                        }
                        repeat(sessionCount) { index ->
                            val scheduledAt = fallbackScheduledAt?.cleanOptional()?.takeIf { sequence == 1 }
                            val title = if (sessionCount == 1) {
                                lineTitle
                            } else {
                                "$lineTitle session ${index + 1} of $sessionCount"
                            }.take(120)
                            add(
                                PreparedOrderSession(
                                    orderItemId = inserted.orderItemId,
                                    sequenceNumber = sequence,
                                    title = title,
                                    scheduledAt = scheduledAt,
                                    status = if (scheduledAt == null) "pending_schedule" else "scheduled",
                                    addonTotal = BigDecimal.ZERO,
                                    paidTotal = BigDecimal.ZERO,
                                    notes = "Included in ${inserted.item.variantName ?: inserted.item.description}",
                                ),
                            )
                            sequence += 1
                        }
                    }
                    return@forEach
                }
                val sessionCount = inserted.item.packageSessionCount().coerceAtLeast(1)
                val lineTitle = inserted.item.variantName?.cleanOptional()
                    ?: inserted.item.description.cleanOptional()
                    ?: orderType.dashboardLabel()
                repeat(sessionCount) { index ->
                    val scheduledAt = fallbackScheduledAt?.cleanOptional()?.takeIf { sequence == 1 }
                    val title = if (sessionCount == 1) {
                        lineTitle
                    } else {
                        "$lineTitle session ${index + 1} of $sessionCount"
                    }.take(120)
                    add(
                        PreparedOrderSession(
                            orderItemId = inserted.orderItemId,
                            sequenceNumber = sequence,
                            title = title,
                            scheduledAt = scheduledAt,
                            status = if (scheduledAt == null) "pending_schedule" else "scheduled",
                            addonTotal = BigDecimal.ZERO,
                            paidTotal = BigDecimal.ZERO,
                            notes = if (inserted.item.includedQuantity > 1) {
                                "${inserted.item.includedQuantity} included sessions"
                            } else {
                                null
                            },
                        ),
                    )
                    sequence += 1
                }
            }
        }
    }

    private fun PreparedPackageComponent.packageComponentSessionCount(lineQuantity: BigDecimal): Int =
        quantity.multiply(lineQuantity)
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
            .coerceAtLeast(1)

    private fun PreparedOrderItem.packageSessionCount(): Int {
        val lineQuantity = runCatching {
            quantity.setScale(0, RoundingMode.HALF_UP).toInt()
        }.getOrDefault(1).coerceAtLeast(1)
        return includedQuantity.coerceAtLeast(1) * lineQuantity
    }

    private fun String.expectedCatalogItemType(): String =
        when (cleanOrderType()) {
            "service" -> "service"
            "appointment" -> "appointment"
            else -> "product"
        }

    private fun String.orderCatalogMismatchMessage(itemName: String): String =
        when (cleanOrderType()) {
            "service" -> "Service requests can only use service catalog items. Remove $itemName or change the order type."
            "appointment" -> "Appointment bookings can only use appointment catalog items. Remove $itemName or change the order type."
            else -> "Sales can only use product catalog items. Remove $itemName or change the order type."
        }

    private fun OrderRequest.toCustomerRequest(name: String): CustomerRequest =
        CustomerRequest(
            name = name,
            phoneNumber = customerPhoneNumber,
            email = customerEmail,
            taxNumber = customerTaxNumber,
            addressLine = customerAddressLine,
            city = customerCity,
            region = customerRegion,
            country = customerCountry,
            postalCode = customerPostalCode,
            notes = "Created from invoice/order.",
        )

    private fun OrderRequest.hasCustomerBillingDetails(): Boolean =
        listOf(
            customerName,
            customerPhoneNumber,
            customerEmail,
            customerTaxNumber,
            customerAddressLine,
            customerCity,
            customerRegion,
            customerCountry,
            customerPostalCode,
        ).any { it.cleanOptional() != null }

    private fun PreparedStatement.setNullableString(index: Int, value: String?) {
        if (value.isNullOrBlank()) {
            setNull(index, Types.VARCHAR)
        } else {
            setString(index, value)
        }
    }

    private fun PreparedStatement.setNullableBigDecimal(index: Int, value: BigDecimal?) {
        if (value == null) {
            setNull(index, Types.NUMERIC)
        } else {
            setBigDecimal(index, value)
        }
    }

    private fun PreparedStatement.setNullableUuid(index: Int, value: String?) {
        if (value.cleanUuidOrNull() == null) {
            setNull(index, Types.OTHER)
        } else {
            setString(index, value)
        }
    }

    private fun PreparedStatement.bindStringParams(params: List<String>) {
        params.forEachIndexed { index, value ->
            setString(index + 1, value)
        }
    }

    private fun String?.cleanUuidOrNull(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
            runCatching { UUID.fromString(value).toString() }.getOrNull()
        }

    private data class DashboardResolvedDateRange(
        val from: String? = null,
        val to: String? = null,
    )

    private fun DashboardQueryFilters.cleanDateFromOrNull(): String? =
        resolvedDateRange().from

    private fun DashboardQueryFilters.cleanDateToOrNull(): String? =
        resolvedDateRange().to

    private fun DashboardQueryFilters.resolvedDateRange(): DashboardResolvedDateRange {
        val today = LocalDate.now()
        val explicitFrom = dateFrom.cleanIsoDateOrNull()
        val explicitTo = dateTo.cleanIsoDateOrNull()
        return when (datePreset.cleanDatePreset()) {
            "today" -> DashboardResolvedDateRange(
                explicitFrom ?: today.toString(),
                explicitTo ?: explicitFrom ?: today.toString(),
            )
            "yesterday" -> {
                val yesterday = today.minusDays(1)
                DashboardResolvedDateRange(
                    explicitFrom ?: yesterday.toString(),
                    explicitTo ?: explicitFrom ?: yesterday.toString(),
                )
            }
            "week" -> DashboardResolvedDateRange(
                from = explicitFrom ?: today.minusDays((today.dayOfWeek.value - 1).toLong()).toString(),
                to = explicitTo ?: today.toString(),
            )
            "month" -> DashboardResolvedDateRange(
                from = explicitFrom ?: today.withDayOfMonth(1).toString(),
                to = explicitTo ?: today.toString(),
            )
            "upcoming" -> DashboardResolvedDateRange(
                from = explicitFrom ?: today.toString(),
                to = explicitTo,
            )
            "all" -> DashboardResolvedDateRange()
            else -> DashboardResolvedDateRange(
                from = explicitFrom,
                to = explicitTo,
            )
        }
    }

    private fun DashboardQueryFilters.withOrderDateParams(workspaceId: String): List<String> =
        buildList {
            add(workspaceId)
            cleanDateFromOrNull()?.let { add(it) }
            cleanDateToOrNull()?.let { add(it) }
        }

    private fun DashboardQueryFilters.withCreatedDateParams(workspaceId: String): List<String> =
        buildList {
            add(workspaceId)
            cleanDateFromOrNull()?.let { add(it) }
            cleanDateToOrNull()?.let { add(it) }
        }

    private fun DashboardQueryFilters.orderDateWhereSql(alias: String = "o"): String =
        buildString {
            cleanDateFromOrNull()?.let {
                append(" and coalesce($alias.scheduled_at, $alias.created_at) >= ?::date")
            }
            cleanDateToOrNull()?.let {
                append(" and coalesce($alias.scheduled_at, $alias.created_at) < (?::date + interval '1 day')")
            }
        }

    private fun DashboardQueryFilters.createdDateWhereSql(expression: String): String =
        buildString {
            cleanDateFromOrNull()?.let {
                append(" and $expression >= ?::date")
            }
            cleanDateToOrNull()?.let {
                append(" and $expression < (?::date + interval '1 day')")
            }
        }

    private fun StringBuilder.appendOrderDateWhere(
        filters: DashboardQueryFilters,
        params: MutableList<String>,
        alias: String = "o",
    ) {
        filters.cleanDateFromOrNull()?.let {
            append(" and coalesce($alias.scheduled_at, $alias.created_at) >= ?::date")
            params.add(it)
        }
        filters.cleanDateToOrNull()?.let {
            append(" and coalesce($alias.scheduled_at, $alias.created_at) < (?::date + interval '1 day')")
            params.add(it)
        }
    }

    private fun StringBuilder.appendOrderSearchWhere(
        searchTokens: List<String>,
        params: MutableList<String>,
    ) {
        searchTokens.forEach { token ->
            val searchSql = """
                and (
                    o.id::text ilike ?
                    or o.order_number ilike ?
                    or coalesce(c.name, '') ilike ?
                    or coalesce(c.phone_number, '') ilike ?
                    or coalesce(c.email, '') ilike ?
                    or coalesce(c.tax_number, '') ilike ?
                    or coalesce(c.address_line, '') ilike ?
                    or coalesce(c.city, '') ilike ?
                    or coalesce(c.region, '') ilike ?
                    or coalesce(c.country, '') ilike ?
                    or coalesce(c.postal_code, '') ilike ?
                    or o.order_type ilike ?
                    or replace(o.order_type, '_', ' ') ilike ?
                    or o.status ilike ?
                    or replace(o.status, '_', ' ') ilike ?
                    or case when o.status = 'draft' then 'captured' else replace(o.status, '_', ' ') end ilike ?
                    or o.fulfillment_type ilike ?
                    or replace(o.fulfillment_type, '_', ' ') ilike ?
                    or o.payment_mode ilike ?
                    or replace(o.payment_mode, '_', ' ') ilike ?
                    or coalesce(o.source, '') ilike ?
                    or replace(coalesce(o.source, ''), '_', ' ') ilike ?
                    or coalesce(o.notes, '') ilike ?
                    or o.currency ilike ?
                    or o.subtotal::text ilike ?
                    or o.tax_total::text ilike ?
                    or o.discount_total::text ilike ?
                    or o.paid_total::text ilike ?
                    or o.total::text ilike ?
                    or greatest(o.total - o.paid_total, 0)::text ilike ?
                    or (o.currency || ' ' || o.total::text) ilike ?
                    or (o.currency || ' ' || o.paid_total::text) ilike ?
                    or (o.currency || ' ' || greatest(o.total - o.paid_total, 0)::text) ilike ?
                    or case when o.total > o.paid_total then 'due' else 'paid' end ilike ?
                    or coalesce(o.scheduled_at::text, '') ilike ?
                    or coalesce(o.created_at::text, '') ilike ?
                    or (
                        (select count(*) from order_items oi_count where oi_count.order_id = o.id)::text || ' items'
                    ) ilike ?
                    or exists (
                        select 1
                        from order_items oi_search
                        left join products p_search on p_search.id = oi_search.product_id
                        where oi_search.order_id = o.id
                          and (
                            coalesce(oi_search.description, '') ilike ?
                            or coalesce(p_search.name, '') ilike ?
                            or oi_search.quantity::text ilike ?
                            or oi_search.unit_price::text ilike ?
                            or oi_search.line_total::text ilike ?
                          )
                    )
                )
            """.trimIndent()
            append(" ")
            append(searchSql)
            repeat(searchSql.count { it == '?' }) { params.add(token.ilikePattern()) }
        }
    }

    private fun StringBuilder.appendCreatedDateWhere(
        filters: DashboardQueryFilters,
        params: MutableList<String>,
        expression: String,
    ) {
        filters.cleanDateFromOrNull()?.let {
            append(" and $expression >= ?::date")
            params.add(it)
        }
        filters.cleanDateToOrNull()?.let {
            append(" and $expression < (?::date + interval '1 day')")
            params.add(it)
        }
    }

    private fun String.cleanName(): String =
        trim().take(180).ifBlank { "Untitled" }

    private fun VerifiedFirebaseUser.publicCatalogCustomerIdentifiers(): PublicCatalogCustomerIdentifiers =
        PublicCatalogCustomerIdentifiers(
            email = email.cleanOptional()?.lowercase(),
            phoneNumber = phoneNumber.cleanOptional(),
        )

    private fun String?.cleanOptional(): String? =
        this?.trim()?.take(240)?.ifBlank { null }

    private fun String?.cleanOptionalUpper(): String? =
        cleanOptional()?.uppercase()

    private fun String?.cleanCouponCode(): String? =
        cleanOptional()
            ?.uppercase()
            ?.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
            ?.take(40)
            ?.ifBlank { null }

    private fun String?.cleanClientRequestId(): String? =
        cleanOptional()
            ?.lowercase()
            ?.filter { it.isLetterOrDigit() || it == '-' || it == '_' || it == ':' || it == '.' }
            ?.take(96)
            ?.ifBlank { null }

    private fun String?.cleanOfferCapAmount(discountType: String): BigDecimal? =
        takeIf { discountType == "percentage" }
            ?.decimalOrZero()
            ?.takeIf { it > BigDecimal.ZERO }
            ?.scaled()

    private fun String?.cleanIsoDateOrNull(): String? =
        this
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { value -> runCatching { LocalDate.parse(value).toString() }.getOrNull() }

    private fun String?.cleanSearchTerm(): String? =
        this?.trim()?.take(80)?.ifBlank { null }

    private fun String?.cleanSearchTokens(): List<String> =
        this
            ?.trim()
            ?.take(80)
            ?.lowercase()
            ?.split(Regex("\\s+"))
            ?.filter { it.isNotBlank() }
            ?.take(8)
            .orEmpty()

    private fun String.ilikePattern(): String =
        "%${replace("%", "\\%").replace("_", "\\_")}%"

    private fun Int.sanitizedLimit(): Int =
        coerceIn(1, 200)

    private fun Int.sanitizedPage(): Int =
        coerceAtLeast(1)

    private fun DashboardQueryFilters.offset(): Int =
        (page.sanitizedPage() - 1) * limit.sanitizedLimit()

    private fun <T> List<T>.toPagedResult(filters: DashboardQueryFilters, totalItems: Int): PagedResult<T> {
        val pageSize = filters.limit.sanitizedLimit()
        val page = filters.page.sanitizedPage()
        val totalPages = if (totalItems <= 0) 0 else ((totalItems - 1) / pageSize) + 1
        return PagedResult(
            items = this,
            pagination = PaginationResponse(
                page = page,
                pageSize = pageSize,
                totalItems = totalItems.coerceAtLeast(0),
                totalPages = totalPages,
                hasPrevious = page > 1 && totalItems > 0,
                hasNext = page < totalPages,
            ),
        )
    }

    private fun String?.cleanOrderStatusFilter(): String? =
        this
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() && it != "all" }
            ?.filter { it.isLetterOrDigit() || it == '_' }
            ?.takeIf { it in AllowedOrderStatuses }

    private fun String?.cleanItemTypeFilter(): String? =
        this
            ?.trim()
            ?.lowercase()
            ?.replace("-", "_")
            ?.takeIf { it.isNotBlank() && it != "all" }
            ?.filter { it.isLetterOrDigit() || it == '_' }
            ?.takeIf { it in AllowedItemTypes }

    private fun String?.cleanOrderTypeFilter(): String? =
        this
            ?.trim()
            ?.lowercase()
            ?.replace("-", "_")
            ?.takeIf { it.isNotBlank() && it != "all" }
            ?.filter { it.isLetterOrDigit() || it == '_' }
            ?.takeIf { it in AllowedOrderTypes }

    private fun String?.cleanDatePreset(): String? {
        val normalized = this
            ?.trim()
            ?.lowercase()
            ?.replace("-", "_")
            ?.filter { it.isLetterOrDigit() || it == '_' }
            ?.takeIf { it.isNotBlank() }
        return when (normalized) {
            "all" -> "all"
            "today" -> "today"
            "yesterday" -> "yesterday"
            "week", "this_week" -> "week"
            "month", "this_month" -> "month"
            "upcoming", "future" -> "upcoming"
            else -> null
        }
    }

    private fun String.cleanItemType(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return when (normalized) {
            "service", "services" -> "service"
            "appointment", "booking", "booking_required" -> "appointment"
            else -> "product"
        }
    }

    private fun String.cleanCategoryItemType(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return when (normalized) {
            "all", "shared", "any" -> "all"
            "service", "services" -> "service"
            "appointment", "appointments", "booking", "booking_required" -> "appointment"
            else -> "product"
        }
    }

    private fun String.cleanCatalogStatus(): String {
        val normalized = normalizedCatalogStatusInput()
        return when (normalized) {
            "inactive", "disabled", "paused", "off" -> "inactive"
            "hidden", "hide", "catalog_hidden", "hidden_from_catalog", "off_catalog", "turn_off_catalog", "turnoffcatalog" -> "hidden"
            "unavailable", "not_available", "notavailable", "temporarily_unavailable", "service_unavailable", "appointment_unavailable" -> "unavailable"
            "out_of_stock", "outofstock", "out_stock", "no_stock", "stock_out", "sold_out", "soldout" -> "out_of_stock"
            "archived", "deleted" -> "archived"
            else -> "active"
        }
    }

    private fun String.isValidCatalogStatusInput(): Boolean =
        normalizedCatalogStatusInput().let { it.isBlank() || it in AllowedCatalogStatusInputs }

    private fun String.normalizedCatalogStatusInput(): String =
        trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }

    private fun String.cleanOrderType(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return when (normalized) {
            "service", "services" -> "service"
            "appointment", "booking" -> "appointment"
            else -> "sale"
        }
    }

    private fun String.cleanOrderSessionStatus(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return normalized.takeIf { it in AllowedOrderSessionStatuses } ?: "scheduled"
    }

    private fun String.dashboardLabel(): String =
        split("_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifBlank { "Unknown" }

    private fun String.dashboardTone(): String =
        when (trim().lowercase()) {
            "cancelled", "failed", "send_failed" -> "danger"
            "new", "draft", "queued", "part_paid" -> "warning"
            "paid", "completed", "sent", "ready", "synced" -> "success"
            else -> "info"
        }

    private fun String.cleanActivityType(): String =
        trim()
            .lowercase()
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(60)
            .ifBlank { "activity" }

    private fun String.cleanActivityTone(): String =
        trim().lowercase().takeIf { it in setOf("info", "success", "warning", "danger") } ?: "info"

    private fun String.sellableActivityLabel(): String =
        when (cleanItemType()) {
            "service" -> "Service"
            "appointment" -> "Appointment"
            else -> "Product"
        }

    private fun ProductResponse.catalogActivityBody(): String = buildList {
        add(name)
        add(itemType.sellableActivityLabel())
        add("$currency $sellingPrice")
        if (itemType.cleanItemType() == "product" && trackStock) {
            add("stock $stockQuantity $unit")
        }
        if (itemType.cleanItemType() == "appointment" && durationMinutes != null) {
            add("$durationMinutes min")
        }
    }.joinToString(" · ")

    private fun OrderResponse.activityCreatedTitle(): String =
        when (orderType.cleanOrderType()) {
            "appointment" -> "Appointment booked"
            "service" -> "Service order created"
            else -> "Sale created"
        }

    private fun OrderResponse.activityUpdatedTitle(): String =
        when (orderType.cleanOrderType()) {
            "appointment" -> "Appointment updated"
            "service" -> "Service order updated"
            else -> "Sale updated"
        }

    private fun OrderResponse.activityStatusTitle(): String =
        when (status.normalizedOrderStatus()) {
            "part_paid" -> "Part payment recorded"
            "paid" -> "Payment recorded"
            "completed" -> "${orderType.cleanOrderType().dashboardLabel()} completed"
            "cancelled" -> "${orderType.cleanOrderType().dashboardLabel()} cancelled"
            else -> "${orderType.cleanOrderType().dashboardLabel()} status updated"
        }

    private fun OrderResponse.activitySummaryBody(): String = buildList {
        add(orderNumber)
        add(customerName?.cleanOptional() ?: "Walk-in customer")
        add("$currency $total")
        add(status.dashboardLabel())
    }.joinToString(" · ")

    private fun OrderResponse.activityStatusBody(): String = buildList {
        add(orderNumber)
        add(customerName?.cleanOptional() ?: "Walk-in customer")
        add(status.dashboardLabel())
        add("paid $currency $paidTotal of $currency $total")
    }.joinToString(" · ")

    private fun String.cleanPrinterConnectionType(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedPrinterConnectionTypes) normalized else "mtp_usb"
    }

    private fun String.cleanPrinterHeaderAlignment(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("left", "center")) normalized else "center"
    }

    private fun String.cleanOfferScope(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("all", "category", "product")) normalized else "product"
    }

    private fun String.cleanDiscountType(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return when (normalized) {
            "fixed", "fixed_amount", "amount" -> "fixed"
            else -> "percentage"
        }
    }

    private fun String.cleanPaymentMethodType(): String =
        trim().lowercase().takeIf { it == "upi" } ?: "upi"

    private fun String.cleanFulfillmentType(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("standard", "take_away", "pickup", "delivery", "dine_in", "scheduled", "booking")) {
            normalized
        } else {
            "standard"
        }
    }

    private fun String.cleanPaymentMode(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("pay_on_spot", "upi", "cash", "card", "credit", "online", "bank_transfer")) {
            normalized
        } else {
            "pay_on_spot"
        }
    }

    private fun String.cleanSessionStatus(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("scheduled", "confirmed", "completed", "cancelled", "missed")) {
            normalized
        } else {
            "scheduled"
        }
    }

    private fun validateCreditPaymentStatus(paymentMode: String, status: String) {
        if (paymentMode == "credit" && status.impliesFullPayment()) {
            throw DashboardOrderValidationException(
                code = "credit_payment_status_invalid",
                publicMessage = "Credit sales must stay open until payment is collected.",
            )
        }
    }

    private fun String.cleanOrderSource(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("dashboard", "public_catalog", "api", "import", "invoice", "quotation")) normalized else "dashboard"
    }

    private fun String.isBillingDocumentOrderSource(): Boolean =
        this == "invoice" || this == "quotation"

    private fun String.cleanUpiId(): String? =
        trim()
            .lowercase()
            .take(120)
            .takeIf { it.contains("@") && it.substringBefore("@").isNotBlank() && it.substringAfter("@").isNotBlank() }

    private fun PublicCatalogPaymentMethodResponse.toUpiPaymentLink(
        amount: String,
        currency: String,
        note: String,
    ): String? {
        if (type.cleanPaymentMethodType() != "upi") return null
        val upi = upiId?.cleanUpiId() ?: return null
        val cleanCurrency = currency.cleanOptionalUpper() ?: "INR"
        val cleanAmount = amount.moneyOrZero().moneyString()
        val payee = payeeName?.cleanOptional() ?: label
        return buildString {
            append("upi://pay")
            append("?pa=").append(upi.urlEncoded())
            append("&pn=").append(payee.urlEncoded())
            append("&am=").append(cleanAmount.urlEncoded())
            append("&cu=").append(cleanCurrency.urlEncoded())
            append("&tn=").append(note.cleanOptional().orEmpty().urlEncoded())
        }
    }

    private fun OrderResponse.paymentBalanceDue(): BigDecimal =
        (total.moneyOrZero() - paidTotal.moneyOrZero()).coerceAtLeast(BigDecimal.ZERO)

    private fun OrderResponse.publicCatalogCanCollectBalancePayment(): Boolean =
        status.normalizedOrderStatus() in setOf("confirmed", "part_paid")

    private fun OrderResponse.publicCatalogStatusMessage(): String =
        when (status.normalizedOrderStatus()) {
            "confirmed" -> "The business confirmed your ${orderType.cleanOrderType().publicCatalogWorkLabel()}."
            "part_paid" -> "The business recorded a partial payment for this ${orderType.cleanOrderType().publicCatalogWorkLabel()}."
            "paid" -> "Payment is recorded for this ${orderType.cleanOrderType().publicCatalogWorkLabel()}."
            "completed" -> "This ${orderType.cleanOrderType().publicCatalogWorkLabel()} is completed."
            "cancelled" -> "The business rejected or cancelled this ${orderType.cleanOrderType().publicCatalogWorkLabel()}."
            else -> "Your ${orderType.cleanOrderType().publicCatalogWorkLabel()} is waiting for business confirmation."
        }

    private fun appendOrderChangeApprovalNotes(
        existing: String?,
        changeRequest: OrderChangeRequestResponse,
        businessNotes: String?,
    ): String =
        buildList {
            existing.cleanOptional()?.let(::add)
            changeRequest.items
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ") { item ->
                    "${item.name}: ${item.previousQuantity} -> ${item.requestedQuantity}"
                }
                ?.let { add("Approved customer change: $it.") }
            changeRequest.paymentReference.cleanOptional()?.let { add("Customer payment reference: $it.") }
            changeRequest.customerNotes.cleanOptional()?.let { add("Customer note: $it") }
            businessNotes.cleanOptional()?.let { add("Business note: $it") }
        }.joinToString("\n").take(2000)

    private fun String.publicCatalogWorkLabel(): String =
        when (cleanOrderType()) {
            "appointment" -> "appointment"
            "service" -> "service request"
            else -> "order"
        }

    private fun discountAmount(
        price: BigDecimal,
        discountType: String?,
        discountValue: BigDecimal,
        discountCapAmount: BigDecimal? = null,
    ): BigDecimal {
        val safeValue = discountValue.coerceAtLeast(BigDecimal.ZERO)
        return when (discountType?.cleanDiscountType()) {
            "fixed" -> safeValue.coerceAtMost(price).scaled()
            else -> {
                val rawDiscount = price
                    .multiply(safeValue.coerceAtMost(BigDecimal("100")))
                    .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                val cap = discountCapAmount?.takeIf { it > BigDecimal.ZERO }
                (if (cap == null) rawDiscount else rawDiscount.coerceAtMost(cap))
                    .coerceAtMost(price)
                    .scaled()
            }
        }
    }

    private fun String.urlEncoded(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")

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

    private fun effectivePaidTotalSql(orderAlias: String): String =
        """
        case
            when $orderAlias.status in ('paid', 'completed')
             and coalesce($orderAlias.paid_total, 0) <= 0
                then coalesce($orderAlias.total, 0)
            else coalesce($orderAlias.paid_total, 0)
        end
        """.trimIndent()

    private fun effectivePaidTotal(
        status: String,
        paidTotal: BigDecimal?,
        total: BigDecimal?,
    ): BigDecimal {
        val cleanPaidTotal = paidTotal ?: BigDecimal.ZERO
        val cleanTotal = total ?: BigDecimal.ZERO
        return if (status.impliesFullPayment() && cleanPaidTotal <= BigDecimal.ZERO) {
            cleanTotal
        } else {
            cleanPaidTotal
        }
    }

    private fun String.normalizedOrderStatus(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedOrderStatuses) normalized else "confirmed"
    }

    private fun String.allowsPublicCatalogChangeRequest(): Boolean =
        normalizedOrderStatus() in setOf("new", "confirmed", "part_paid")

    private fun String.allowsBusinessOrderChangeApproval(): Boolean =
        normalizedOrderStatus() !in setOf("completed", "cancelled")

    private fun String.appliesInventory(): Boolean =
        this in InventoryApplyingStatuses

    private fun String.impliesFullPayment(): Boolean =
        this in FullPaymentStatuses

    private fun List<PublicCatalogPreparedItem>.derivedOrderType(): String =
        when {
            any { it.itemType == "appointment" } -> "appointment"
            any { it.itemType == "service" } && none { it.itemType == "product" } -> "service"
            else -> "sale"
        }

    private class ProductCsvParseException(
        val row: Int,
        val publicMessage: String,
    ) : IllegalArgumentException(publicMessage)

    private fun String.toProductImportRequest(defaultCurrency: String): ProductImportRequest {
        val rows = parseCsvRows().filter { row -> row.any { it.isNotBlank() } }
        if (rows.isEmpty()) {
            return ProductImportRequest(emptyList())
        }
        val headers = rows.first().map { it.normalizedCsvHeader() }
        if ("name" !in headers) {
            throw ProductCsvParseException(
                row = 1,
                publicMessage = "CSV header must include the name column.",
            )
        }
        if (rows.size > 501) {
            throw ProductCsvParseException(
                row = 1,
                publicMessage = "Import up to 500 product rows at a time.",
            )
        }
        fun List<String>.value(vararg aliases: String): String {
            val index = aliases
                .map { it.normalizedCsvHeader() }
                .firstNotNullOfOrNull { alias -> headers.indexOf(alias).takeIf { it >= 0 } }
                ?: return ""
            return getOrNull(index).orEmpty().trim()
        }
        return ProductImportRequest(
            rows = rows.drop(1).mapNotNull { row ->
                if (row.all { it.isBlank() }) return@mapNotNull null
                ProductImportRowRequest(
                    name = row.value("name", "productName", "itemName", "item"),
                    itemType = row.value("itemType", "item type", "type", "sellableType").ifBlank { "product" },
                    categoryName = row.value("categoryName", "category name", "category"),
                    sku = row.value("sku"),
                    barcode = row.value("barcode", "barCode", "ean", "upc"),
                    description = row.value("description", "details"),
                    unit = row.value("unit").ifBlank { "pcs" },
                    sellingPrice = row.value("sellingPrice", "selling price", "price").ifBlank { "0" },
                    costPrice = row.value("costPrice", "cost price", "cost").ifBlank { "0" },
                    currency = row.value("currency").ifBlank { defaultCurrency.ifBlank { "INR" } },
                    taxRate = row.value("taxRate", "tax rate", "tax", "gst", "vat").ifBlank { "0" },
                    pricesIncludeTax = row.value("pricesIncludeTax", "prices include tax", "priceIncludesTax")
                        .csvBoolean(defaultValue = false),
                    stockQuantity = row.value("stockQuantity", "stock quantity", "stock", "openingStock")
                        .ifBlank { "0" },
                    reorderLevel = row.value("reorderLevel", "reorder level", "lowStockLevel", "minimumStock")
                        .ifBlank { "0" },
                    trackStock = row.value("trackStock", "track stock", "inventory")
                        .csvBoolean(defaultValue = true),
                    durationMinutes = row.value("durationMinutes", "duration minutes", "duration", "minutes")
                        .toIntOrNull(),
                    bookingRequired = row.value("bookingRequired", "booking required", "requiresBooking")
                        .csvBoolean(defaultValue = false),
                    expiryDate = row.value("expiryDate", "expiry date", "expiresAt", "expiry", "bestBefore"),
                    supplierName = row.value("supplierName", "supplier name", "supplier"),
                    status = row.value("status", "availability", "catalogStatus", "catalog status")
                        .ifBlank { "active" },
                )
            },
        )
    }

    private fun String.parseCsvRows(): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < length) {
            val char = this[index]
            when {
                inQuotes && char == '"' && getOrNull(index + 1) == '"' -> {
                    field.append('"')
                    index += 1
                }
                char == '"' -> inQuotes = !inQuotes
                !inQuotes && char == ',' -> {
                    row += field.toString()
                    field.clear()
                }
                !inQuotes && (char == '\n' || char == '\r') -> {
                    if (char == '\r' && getOrNull(index + 1) == '\n') index += 1
                    row += field.toString()
                    field.clear()
                    rows += row.toList()
                    row.clear()
                }
                else -> field.append(char)
            }
            index += 1
        }
        row += field.toString()
        if (row.any { it.isNotBlank() }) rows += row.toList()
        if (inQuotes) {
            throw ProductCsvParseException(
                row = rows.size.coerceAtLeast(1),
                publicMessage = "CSV has an unclosed quote. Check the file and try again.",
            )
        }
        return rows
    }

    private fun String.normalizedCsvHeader(): String =
        trim().lowercase().filter(Char::isLetterOrDigit)

    private fun String.csvBoolean(defaultValue: Boolean): Boolean =
        when (trim().lowercase()) {
            "true", "1", "yes", "y" -> true
            "false", "0", "no", "n" -> false
            else -> defaultValue
        }

    private fun ProductImportRowRequest.productImportValidationErrors(): List<String> = buildList {
        if (itemType.cleanItemType() !in AllowedItemTypes) add("Item type must be product, service, or appointment.")
        if (!sellingPrice.isValidNonNegativeDecimal()) add("Selling price must be a valid number.")
        if (!costPrice.isValidNonNegativeDecimal()) add("Cost price must be a valid number.")
        if (!taxRate.isValidNonNegativeDecimal()) add("Tax rate must be a valid number.")
        if (!stockQuantity.isValidNonNegativeDecimal()) add("Stock quantity must be zero or higher.")
        if (!reorderLevel.isValidNonNegativeDecimal()) add("Reorder level must be zero or higher.")
        if (expiryDate.cleanOptional() != null && expiryDate.cleanIsoDateOrNull() == null) {
            add("Expiry date must use YYYY-MM-DD.")
        }
        if (!status.isValidCatalogStatusInput()) {
            add("Status must be active, hidden, unavailable, inactive, or out_of_stock.")
        }
        if (itemType.cleanItemType() == "appointment" && (durationMinutes ?: 0) <= 0) {
            add("Appointment items need a duration in minutes.")
        }
    }

    private fun ProductRequest.validateCatalogItemForSave() {
        if (!status.isValidCatalogStatusInput()) {
            throw DashboardOrderValidationException(
                code = "product_status_invalid",
                publicMessage = "Choose active, hidden, unavailable, inactive, or out of stock.",
            )
        }
        if (itemType.cleanItemType() == "appointment" && (durationMinutes ?: 0) <= 0) {
            throw DashboardOrderValidationException(
                code = "appointment_duration_required",
                publicMessage = "Appointment items need a duration in minutes.",
            )
        }
    }

    private fun String.isValidNonNegativeDecimal(): Boolean {
        val value = trim().replace(",", "")
        if (value.isBlank()) return true
        return value.toBigDecimalOrNull()?.let { it >= BigDecimal.ZERO } == true
    }

    private fun SQLException.productImportRowMessage(): String =
        when (sqlState) {
            "23505" -> "SKU or barcode already exists in this workspace."
            "22P02", "22007", "22008", "22003" -> "Check the number or date format for this row."
            "42703", "42P01" -> "Catalog database is not ready for import. Run the latest backend migration."
            else -> "Could not import this row. Check the product values and try again."
        }

    private fun ProductImportRowRequest.toProductRequest(
        access: DashboardWorkspaceAccess,
        supplierId: String?,
        productName: String,
    ): ProductRequest =
        ProductRequest(
            name = productName,
            itemType = itemType,
            categoryName = categoryName,
            sku = sku,
            barcode = barcode,
            description = description,
            unit = unit.cleanOptional() ?: "pcs",
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            currency = currency.cleanOptionalUpper() ?: access.currency,
            taxRate = taxRate,
            pricesIncludeTax = pricesIncludeTax,
            stockQuantity = stockQuantity,
            reorderLevel = reorderLevel,
            trackStock = trackStock,
            durationMinutes = durationMinutes,
            bookingRequired = bookingRequired,
            expiryDate = expiryDate,
            supplierId = supplierId,
            status = status,
        )

    private fun List<ProductResponse>.toProductCsv(): String {
        val rows = map { product ->
            listOf(
                product.name,
                product.itemType,
                product.categoryName.orEmpty(),
                product.sku.orEmpty(),
                product.barcode.orEmpty(),
                product.description.orEmpty(),
                product.unit,
                product.sellingPrice,
                product.costPrice,
                product.currency,
                product.taxRate,
                product.pricesIncludeTax.toString(),
                product.stockQuantity,
                product.reorderLevel,
                product.trackStock.toString(),
                product.durationMinutes?.toString().orEmpty(),
                product.bookingRequired.toString(),
                product.expiryDate.orEmpty(),
                product.supplierName.orEmpty(),
                product.status,
            )
        }
        return (listOf(ProductCsvColumns) + rows)
            .joinToString("\n") { row -> row.joinToString(",") { it.csvEscaped() } }
    }

    private fun String.csvEscaped(): String =
        if (any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }

    private fun String?.toDashboardMediaUrl(): String? {
        val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (value.startsWith("https://") || value.startsWith("http://")) return value
        if (config.activeMediaStorageProvider != "cloudinary") return null
        val cloudName = config.cloudinaryCloudName?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return "https://res.cloudinary.com/$cloudName/image/upload/${value.cloudinaryPathEncoded()}"
    }

    private fun String.cloudinaryPathEncoded(): String =
        split("/")
            .filter { it.isNotBlank() }
            .joinToString("/") { segment ->
                java.net.URLEncoder.encode(segment, Charsets.UTF_8)
                    .replace("+", "%20")
            }

    private fun DashboardWorkspaceAccess.requirePermission(permission: String, actionLabel: String) {
        if (role == "business_owner") return
        val cleanPermissions = permissions.map { it.trim().lowercase().replace("-", "_") }.toSet()
        if (PermissionReadOnly in cleanPermissions || permission !in cleanPermissions) {
            throw DashboardOrderValidationException(
                code = "team_permission_denied",
                publicMessage = "This staff role cannot $actionLabel.",
            )
        }
    }

    private fun DashboardWorkspaceAccess.requireAnyPermission(permissions: List<String>, actionLabel: String) {
        if (role == "business_owner") return
        val cleanPermissions = this.permissions.map { it.trim().lowercase().replace("-", "_") }.toSet()
        if (PermissionReadOnly in cleanPermissions || permissions.none { it in cleanPermissions }) {
            throw DashboardOrderValidationException(
                code = "team_permission_denied",
                publicMessage = "This staff role cannot $actionLabel.",
            )
        }
    }

    private fun String.catalogCreatePermission(): String =
        when (trim().lowercase()) {
            "service" -> PermissionCreateService
            "appointment" -> PermissionCreateAppointment
            else -> PermissionCreateProduct
        }

    private data class PreparedOrderItem(
        val productId: String?,
        val variantId: String?,
        var variantName: String?,
        var variantTrackStock: Boolean = false,
        var includedQuantity: Int = 1,
        var components: List<PreparedPackageComponent> = emptyList(),
        val description: String,
        val quantity: BigDecimal,
        val unitPrice: BigDecimal,
        val taxRate: BigDecimal,
        val lineSubtotal: BigDecimal,
        val lineTax: BigDecimal,
        val lineTotal: BigDecimal,
    )

    private data class InsertedOrderItem(
        val item: PreparedOrderItem,
        val orderItemId: String,
    )

    private data class PreparedOrderSession(
        val orderItemId: String?,
        val sequenceNumber: Int,
        val title: String,
        val scheduledAt: String?,
        val status: String,
        val addonTotal: BigDecimal,
        val paidTotal: BigDecimal,
        val notes: String?,
    )

    private data class ResolvedPackageComponent(
        val productId: String,
        val variantId: String?,
        val quantity: BigDecimal,
        val status: String,
    )

    private data class ProductVariantValues(
        val name: String,
        val sku: String?,
        val barcode: String?,
        val sellingPrice: BigDecimal,
        val costPrice: BigDecimal,
        val stockQuantity: BigDecimal,
        val trackStock: Boolean,
        val durationMinutes: Int?,
        val includedQuantity: Int,
        val addonsJson: String,
        val sortOrder: Int,
        val status: String,
    )

    private data class PreparedPackageComponent(
        val productId: String,
        val productName: String,
        val itemType: String,
        val unit: String,
        val trackStock: Boolean,
        val variantTrackStock: Boolean,
        val stockQuantity: BigDecimal,
        val variantId: String?,
        val variantName: String?,
        val quantity: BigDecimal,
    )

    private data class InventoryRequirement(
        val productId: String,
        val variantId: String?,
        val variantScoped: Boolean,
        val quantity: BigDecimal,
    )

    private data class InventoryRequirementKey(
        val productId: String,
        val variantId: String?,
        val variantScoped: Boolean,
    )

    private data class ProductStockState(
        val trackStock: Boolean,
        val stockQuantity: BigDecimal,
    )

    private data class VariantStockState(
        val trackStock: Boolean,
        val stockQuantity: BigDecimal,
    )

    private data class CatalogVariant(
        val productId: String,
        val name: String,
        val trackStock: Boolean,
        val includedQuantity: Int,
        val components: List<PreparedPackageComponent>,
    )

    private data class CurrentOrderStatus(
        val status: String,
        val inventoryApplied: Boolean,
        val total: BigDecimal,
        val paymentMode: String,
    )

    private data class PublicCatalogPreparedItem(
        val orderItem: OrderItemRequest,
        val itemType: String,
        val discountTotal: BigDecimal = BigDecimal.ZERO,
    )

    private companion object {
        val AllowedOrderStatuses = setOf("new", "draft", "confirmed", "paid", "part_paid", "completed", "cancelled")
        val InventoryApplyingStatuses = setOf("confirmed", "paid", "part_paid", "completed")
        val FullPaymentStatuses = setOf("paid", "completed")
        val AllowedItemTypes = setOf("product", "service", "appointment")
        val AllowedOrderTypes = setOf("sale", "service", "appointment")
        val AllowedOrderSessionStatuses = setOf("pending_schedule", "scheduled", "in_progress", "completed", "cancelled", "no_show")
        val AllowedCatalogStatusInputs = setOf(
            "active",
            "inactive",
            "disabled",
            "paused",
            "off",
            "hidden",
            "hide",
            "catalog_hidden",
            "hidden_from_catalog",
            "off_catalog",
            "turn_off_catalog",
            "turnoffcatalog",
            "unavailable",
            "not_available",
            "notavailable",
            "temporarily_unavailable",
            "service_unavailable",
            "appointment_unavailable",
            "out_of_stock",
            "outofstock",
            "out_stock",
            "no_stock",
            "stock_out",
            "sold_out",
            "soldout",
        )
        const val PermissionReadOnly = "read_only"
        const val PermissionCreateSale = "create_sale"
        const val PermissionEditSale = "edit_sale"
        const val PermissionChangeBookingStatus = "change_booking_status"
        const val PermissionCreateProduct = "create_product"
        const val PermissionCreateService = "create_service"
        const val PermissionCreateAppointment = "create_appointment"
        const val PermissionCreateOffer = "create_offer"
        const val PermissionManageStock = "manage_stock"
        const val PermissionManageCustomers = "manage_customers"
        const val PermissionManageAccount = "manage_account"
        val AllowedPrinterConnectionTypes = setOf(
            "mtp_usb",
            "usb",
            "bluetooth",
            "network",
            "airprint",
            "system",
            "browser_print",
            "local_agent",
            "esc_pos",
        )
    }
}
