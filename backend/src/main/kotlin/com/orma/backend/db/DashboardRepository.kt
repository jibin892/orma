package com.orma.backend.db

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.CustomerResponse
import com.orma.backend.models.DashboardSummaryResponse
import com.orma.backend.models.OrderItemRequest
import com.orma.backend.models.OrderItemResponse
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.OrderResponse
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
import com.orma.backend.models.StockAdjustmentRequest
import com.orma.backend.models.StockMovementResponse
import com.orma.backend.models.SupplierRequest
import com.orma.backend.models.SupplierResponse
import com.orma.backend.models.WorkspacePaymentMethodRequest
import com.orma.backend.models.WorkspacePaymentMethodResponse
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

data class DashboardQueryFilters(
    val query: String? = null,
    val status: String? = null,
    val limit: Int = 80,
    val lowStockOnly: Boolean = false,
    val supplierId: String? = null,
    val barcode: String? = null,
    val scheduledOnly: Boolean = false,
)

sealed interface PublicCatalogOrderSubmitResult {
    data class Success(
        val response: PublicCatalogOrderResponse,
    ) : PublicCatalogOrderSubmitResult

    data object WorkspaceNotFound : PublicCatalogOrderSubmitResult

    data object ItemsUnavailable : PublicCatalogOrderSubmitResult
}

private val ProductCsvColumns = listOf(
    "name",
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
    "supplierName",
)

private val RequiredProductCsvColumns = listOf("name")

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

    suspend fun createPublicCatalogOrder(
        workspaceId: String,
        request: PublicCatalogOrderRequest,
    ): PublicCatalogOrderSubmitResult = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val access = connection.resolvePublicWorkspaceAccess(workspaceId) ?: run {
                    connection.rollback()
                    return@withContext PublicCatalogOrderSubmitResult.WorkspaceNotFound
                }
                val publicItems = connection.publicOrderItems(access.workspaceId, request)
                if (publicItems.isEmpty()) {
                    connection.rollback()
                    return@withContext PublicCatalogOrderSubmitResult.ItemsUnavailable
                }
                val customer = connection.findCustomerByPhone(
                    workspaceId = access.workspaceId,
                    phoneNumber = request.phoneNumber,
                ) ?: connection.insertCustomer(
                    workspaceId = access.workspaceId,
                    request = CustomerRequest(
                        name = request.customerName,
                        phoneNumber = request.phoneNumber,
                        notes = "Created from public catalog.",
                    ),
                )
                val fulfillment = request.fulfillmentType.cleanFulfillmentType()
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
                        status = "draft",
                        scheduledAt = request.scheduledAt?.cleanOptional(),
                        paidTotal = "0",
                        currency = access.currency,
                        notes = publicNote,
                        fulfillmentType = fulfillment,
                        paymentMode = paymentMode,
                        source = "public_catalog",
                        items = publicItems,
                    ),
                )
                val paymentMethod = if (paymentMode == "upi") {
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
                            amount = order.total,
                            currency = order.currency,
                            note = "ORMA ${order.orderNumber}",
                        ),
                        paymentMethod = paymentMethod,
                    ),
                )
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

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
                recentOrders = connection.listOrders(
                    workspaceId = access.workspaceId,
                    filters = DashboardQueryFilters(limit = 5),
                    includeItems = false,
                ),
                lowStockItems = connection.listProducts(
                    workspaceId = access.workspaceId,
                    filters = DashboardQueryFilters(limit = 5, lowStockOnly = true),
                ),
            )
        }
    }

    suspend fun customers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<CustomerResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listCustomers(access.workspaceId, filters)
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

    suspend fun suppliers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<SupplierResponse>? = withContext(Dispatchers.IO) {
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

    suspend fun productCategories(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<ProductCategoryResponse>? = withContext(Dispatchers.IO) {
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
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.insertProductCategory(access.workspaceId, request)
        }
    }

    suspend fun productOffers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<ProductOfferResponse>? = withContext(Dispatchers.IO) {
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
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.insertProductOffer(access.workspaceId, request)
        }
    }

    suspend fun paymentMethods(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<WorkspacePaymentMethodResponse>? = withContext(Dispatchers.IO) {
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
                val method = connection.insertPaymentMethod(access.workspaceId, request)
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
    ): List<ProductResponse>? = withContext(Dispatchers.IO) {
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
            val products = connection.listProducts(access.workspaceId, filters = filters.copy(limit = 500))
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
                csv = ProductCsvColumns.joinToString(",") { it.csvEscaped() } + "\n",
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
                val response = connection.importProductRows(access, request)
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
                val response = connection.importProductRows(access, request)
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
                cleanSku != null && !seenSkus.add(cleanSku) -> {
                    skipped += 1
                    errors += ProductImportErrorResponse(row = rowNumber, message = "Duplicate SKU in this CSV.")
                    return@forEachIndexed
                }
                cleanBarcode != null && !seenBarcodes.add(cleanBarcode) -> {
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

    suspend fun orders(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<OrderResponse>? = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            val access = connection.resolveWorkspaceAccess(firebaseUser) ?: return@withContext null
            connection.listOrders(access.workspaceId, filters = filters, includeItems = false)
        }
    }

    suspend fun printers(
        firebaseUser: VerifiedFirebaseUser,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<PrinterProfileResponse>? = withContext(Dispatchers.IO) {
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
                val printer = connection.insertPrinter(access.workspaceId, request)
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
                val printer = connection.updatePrinter(access.workspaceId, printerId, request)
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

    private fun Connection.findPublicCatalogWorkspace(workspaceId: String): PublicCatalogWorkspaceResponse? {
        val cleanWorkspaceId = workspaceId.cleanUuidOrNull() ?: return null
        val sql = """
            select
                id::text,
                business_name,
                industry,
                city,
                currency,
                logo_file_name
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
                    PublicCatalogWorkspaceResponse(
                        id = result.getString("id"),
                        businessName = result.getString("business_name"),
                        industry = result.getString("industry"),
                        city = result.getString("city"),
                        currency = result.getString("currency") ?: "INR",
                        logoUrl = result.getString("logo_file_name"),
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
                currency
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
                        currency = result.getString("currency") ?: "INR",
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
                products.description,
                products.unit,
                products.selling_price,
                products.currency,
                products.tax_rate,
                products.prices_include_tax,
                products.track_stock,
                products.stock_quantity,
                offer.id::text as offer_id,
                offer.name as offer_name,
                offer.description as offer_description,
                offer.discount_type,
                offer.discount_value,
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
                  and (po.starts_at is null or po.starts_at <= now())
                  and (po.ends_at is null or po.ends_at >= now())
                  and (
                    po.applies_to = 'all'
                    or (po.applies_to = 'category' and po.category_id = products.category_id)
                    or (po.applies_to = 'product' and po.product_id = products.id)
                  )
                order by
                  case po.applies_to when 'product' then 0 when 'category' then 1 else 2 end,
                  po.discount_value desc,
                  po.created_at desc
                limit 1
            ) offer on true
            where products.workspace_id = ?::uuid
              and products.status = 'active'
              and (products.track_stock = false or products.stock_quantity > 0)
            order by coalesce(pc.sort_order, 999), coalesce(pc.name, ''), products.name asc
            limit 200
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        val trackStock = result.getBoolean("track_stock")
                        val stockQuantity = result.getBigDecimal("stock_quantity") ?: BigDecimal.ZERO
                        add(
                            result.toPublicCatalogProductResponse(trackStock, stockQuantity),
                        )
                    }
                }
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

    private fun Connection.publicOrderItems(
        workspaceId: String,
        request: PublicCatalogOrderRequest,
    ): List<OrderItemRequest> =
        request.items
            .take(50)
            .mapNotNull { requestedItem ->
                val productId = requestedItem.productId.cleanUuidOrNull() ?: return@mapNotNull null
                val quantity = requestedItem.quantity.decimalOrZero()
                    .takeIf { it > BigDecimal.ZERO }
                    ?: BigDecimal.ONE
                val product = publicOrderProduct(workspaceId, productId) ?: return@mapNotNull null
                if (product.trackStock && product.stockQuantity.decimalOrZero() <= BigDecimal.ZERO) {
                    return@mapNotNull null
                }
                OrderItemRequest(
                    productId = product.id,
                    description = product.name,
                    quantity = quantity.decimalString(),
                    unitPrice = product.offer?.finalPrice ?: product.sellingPrice,
                    taxRate = product.taxRate,
                )
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
                products.description,
                products.unit,
                products.selling_price,
                products.currency,
                products.tax_rate,
                products.prices_include_tax,
                products.track_stock,
                products.stock_quantity,
                offer.id::text as offer_id,
                offer.name as offer_name,
                offer.description as offer_description,
                offer.discount_type,
                offer.discount_value,
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
                  and (po.starts_at is null or po.starts_at <= now())
                  and (po.ends_at is null or po.ends_at >= now())
                  and (
                    po.applies_to = 'all'
                    or (po.applies_to = 'category' and po.category_id = products.category_id)
                    or (po.applies_to = 'product' and po.product_id = products.id)
                  )
                order by
                  case po.applies_to when 'product' then 0 when 'category' then 1 else 2 end,
                  po.discount_value desc,
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
                    result.toPublicCatalogProductResponse(trackStock, stockQuantity)
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

    private fun Connection.listCustomers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<CustomerResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select *
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
                        or coalesce(city, '') ilike ?
                        or coalesce(notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(5) { params.add(search.ilikePattern()) }
            }
            append(" order by created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
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

    private fun Connection.listSuppliers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<SupplierResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select *
                from suppliers
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
                        or coalesce(notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(5) { params.add(search.ilikePattern()) }
            }
            append(" order by created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
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

    private fun Connection.listProductCategories(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<ProductCategoryResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select id::text, name, sort_order, status, created_at::text, updated_at::text
                from product_categories
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and name ilike ?")
                params.add(search.ilikePattern())
            }
            append(" order by sort_order asc, name asc limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toProductCategoryResponse())
                }
            }
        }
    }

    private fun Connection.insertProductCategory(
        workspaceId: String,
        request: ProductCategoryRequest,
    ): ProductCategoryResponse {
        val sql = """
            insert into product_categories (workspace_id, name, sort_order, updated_at)
            values (?::uuid, ?, ?, now())
            on conflict (workspace_id, name) do update
            set sort_order = excluded.sort_order, status = 'active', updated_at = now()
            returning id::text, name, sort_order, status, created_at::text, updated_at::text
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setString(1, workspaceId)
            statement.setString(2, request.name.cleanName())
            statement.setInt(3, request.sortOrder.coerceIn(0, 999))
            statement.executeQuery().use { result ->
                result.next()
                result.toProductCategoryResponse()
            }
        }
    }

    private fun Connection.listProductOffers(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<ProductOfferResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
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
                    po.name,
                    po.description,
                    po.discount_type,
                    po.discount_value,
                    po.starts_at::text,
                    po.ends_at::text,
                    po.status,
                    po.created_at::text,
                    po.updated_at::text
                from product_offers po
                left join products p on p.id = po.product_id
                left join product_categories pc on pc.id = po.category_id
                where po.workspace_id = ?::uuid and po.status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and (po.name ilike ? or coalesce(p.name, '') ilike ? or coalesce(pc.name, '') ilike ?)")
                repeat(3) { params.add(search.ilikePattern()) }
            }
            append(" order by po.created_at desc limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toProductOfferResponse())
                }
            }
        }
    }

    private fun Connection.insertProductOffer(
        workspaceId: String,
        request: ProductOfferRequest,
    ): ProductOfferResponse {
        val appliesTo = request.appliesTo.cleanOfferScope()
        val sql = """
            insert into product_offers (
                workspace_id, product_id, category_id, applies_to, name, description,
                discount_type, discount_value, starts_at, ends_at, updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::timestamptz, ?::timestamptz, now())
            returning
                id::text,
                applies_to,
                product_id::text,
                (select name from products where id = product_offers.product_id) as product_name,
                category_id::text,
                (select name from product_categories where id = product_offers.category_id) as category_name,
                name,
                description,
                discount_type,
                discount_value,
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
            statement.setString(4, appliesTo)
            statement.setString(5, request.name.cleanName())
            statement.setNullableString(6, request.description?.cleanOptional())
            statement.setString(7, request.discountType.cleanDiscountType())
            statement.setBigDecimal(8, request.discountValue.decimalOrZero().coerceAtLeast(BigDecimal.ZERO))
            statement.setNullableString(9, request.startsAt?.cleanOptional())
            statement.setNullableString(10, request.endsAt?.cleanOptional())
            statement.executeQuery().use { result ->
                result.next()
                result.toProductOfferResponse()
            }
        }
    }

    private fun Connection.listPaymentMethods(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<WorkspacePaymentMethodResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select id::text, type, label, upi_id, payee_name, is_default, status, created_at::text, updated_at::text
                from workspace_payment_methods
                where workspace_id = ?::uuid and status = 'active'
                """.trimIndent(),
            )
            if (search != null) {
                append(" and (label ilike ? or coalesce(upi_id, '') ilike ? or coalesce(payee_name, '') ilike ?)")
                repeat(3) { params.add(search.ilikePattern()) }
            }
            append(" order by is_default desc, created_at desc limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toWorkspacePaymentMethodResponse())
                }
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

    private fun Connection.listProducts(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<ProductResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val supplierId = filters.supplierId.cleanUuidOrNull()
        val barcode = filters.barcode.cleanSearchTerm()
        val sql = buildString {
            append(
                """
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
                where p.workspace_id = ?::uuid and p.status = 'active'
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
            if (barcode != null) {
                append(" and coalesce(p.barcode, '') ilike ?")
                params.add(barcode.ilikePattern())
            }
            if (filters.lowStockOnly) {
                append(" and p.track_stock = true and p.stock_quantity <= p.reorder_level")
            }
            append(" order by p.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
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
                workspace_id, supplier_id, category_id, name, sku, barcode, description, unit,
                selling_price, cost_price, currency, tax_rate, prices_include_tax,
                stock_quantity, reorder_level, track_stock, updated_at
            )
            values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
            returning *, null::text as supplier_name, null::text as category_name, null::text as image_storage_path
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

    private fun Connection.updateProduct(
        access: DashboardWorkspaceAccess,
        productId: String,
        request: ProductRequest,
    ): ProductResponse? {
        val sql = """
            update products
            set supplier_id = ?::uuid, category_id = ?::uuid, name = ?, sku = ?, barcode = ?, description = ?,
                unit = ?, selling_price = ?, cost_price = ?, currency = ?, tax_rate = ?,
                prices_include_tax = ?, reorder_level = ?, track_stock = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid
            returning *, null::text as supplier_name, null::text as category_name, null::text as image_storage_path
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.setNullableUuid(1, request.supplierId)
            statement.setNullableUuid(2, request.categoryId)
            statement.setString(3, request.name.cleanName())
            statement.setNullableString(4, request.sku?.cleanOptionalUpper())
            statement.setNullableString(5, request.barcode?.cleanOptional())
            statement.setNullableString(6, request.description?.cleanOptional())
            statement.setString(7, request.unit.cleanOptional() ?: "pcs")
            statement.setBigDecimal(8, request.sellingPrice.moneyOrZero())
            statement.setBigDecimal(9, request.costPrice.moneyOrZero())
            statement.setString(10, request.currency?.cleanOptionalUpper() ?: access.currency)
            statement.setBigDecimal(11, request.taxRate.decimalOrZero())
            statement.setBoolean(12, request.pricesIncludeTax)
            statement.setBigDecimal(13, request.reorderLevel.decimalOrZero())
            statement.setBoolean(14, request.trackStock)
            statement.setString(15, productId)
            statement.setString(16, access.workspaceId)
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
        filters: DashboardQueryFilters = DashboardQueryFilters(),
        includeItems: Boolean,
    ): List<OrderResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val status = filters.status.cleanOrderStatusFilter()
        val sql = buildString {
            append(orderSelectSql())
            append(" where o.workspace_id = ?::uuid")
            if (status != null) {
                append(" and o.status = ?")
                params.add(status)
            } else {
                append(" and o.status <> 'cancelled'")
            }
            if (filters.scheduledOnly) {
                append(" and o.scheduled_at is not null")
            }
            if (search != null) {
                append(
                    """
                     and (
                        o.order_number ilike ?
                        or coalesce(c.name, '') ilike ?
                        or coalesce(o.notes, '') ilike ?
                    )
                    """.trimIndent(),
                )
                repeat(3) { params.add(search.ilikePattern()) }
            }
            append(" group by o.id, c.name")
            append(" order by o.created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
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

    private fun Connection.listPrinters(
        workspaceId: String,
        filters: DashboardQueryFilters = DashboardQueryFilters(),
    ): List<PrinterProfileResponse> {
        val params = mutableListOf(workspaceId)
        val search = filters.query.cleanSearchTerm()
        val sql = buildString {
            append(
                """
                select *
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
            append(" order by is_default_receipt desc, is_default_barcode desc, created_at desc")
            append(" limit ${filters.limit.sanitizedLimit()}")
        }
        return prepareStatement(sql).use { statement ->
            statement.bindStringParams(params)
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) add(result.toPrinterProfileResponse())
                }
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
                notes, updated_at
            )
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
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
        clearPrinterDefaultsIfNeeded(workspaceId, request, exceptPrinterId = printerId.cleanUuidOrNull())
        val sql = """
            update printer_profiles
            set name = ?, connection_type = ?, address = ?, paper_width_mm = ?, dpi = ?,
                supports_receipts = ?, supports_barcodes = ?, is_default_receipt = ?,
                is_default_barcode = ?, notes = ?, updated_at = now()
            where id = ?::uuid and workspace_id = ?::uuid and status = 'active'
            returning *
        """.trimIndent()
        return prepareStatement(sql).use { statement ->
            statement.bindPrinterRequest(request, startIndex = 1)
            statement.setString(11, printerId)
            statement.setString(12, workspaceId)
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
                notes, inventory_applied, created_by_user_id, source, fulfillment_type, payment_mode, updated_at
            )
            values (
                ?::uuid, ?::uuid, ?, ?, ?::timestamptz,
                ?, ?, 0, ?, ?, ?, ?, ?, ?::uuid, ?, ?, ?, now()
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
            statement.setString(14, request.source.cleanOrderSource())
            statement.setString(15, request.fulfillmentType.cleanFulfillmentType())
            statement.setString(16, request.paymentMode.cleanPaymentMode())
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
            o.fulfillment_type,
            o.payment_mode,
            o.source,
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
        setNullableUuid(startIndex + 1, request.categoryId)
        setString(startIndex + 2, request.name.cleanName())
        setNullableString(startIndex + 3, request.sku?.cleanOptionalUpper())
        setNullableString(startIndex + 4, request.barcode?.cleanOptional())
        setNullableString(startIndex + 5, request.description?.cleanOptional())
        setString(startIndex + 6, request.unit.cleanOptional() ?: "pcs")
        setBigDecimal(startIndex + 7, request.sellingPrice.moneyOrZero())
        setBigDecimal(startIndex + 8, request.costPrice.moneyOrZero())
        setString(startIndex + 9, request.currency?.cleanOptionalUpper() ?: access.currency)
        setBigDecimal(startIndex + 10, request.taxRate.decimalOrZero())
        setBoolean(startIndex + 11, request.pricesIncludeTax)
        setBigDecimal(startIndex + 12, request.stockQuantity.decimalOrZero())
        setBigDecimal(startIndex + 13, request.reorderLevel.decimalOrZero())
        setBoolean(startIndex + 14, request.trackStock)
    }

    private fun PreparedStatement.bindPrinterRequest(request: PrinterProfileRequest, startIndex: Int) {
        setString(startIndex, request.name.cleanName())
        setString(startIndex + 1, request.connectionType.cleanPrinterConnectionType())
        setNullableString(startIndex + 2, request.address?.cleanOptional())
        setInt(startIndex + 3, request.paperWidthMm.coerceIn(40, 120))
        setInt(startIndex + 4, request.dpi.coerceIn(120, 600))
        setBoolean(startIndex + 5, request.supportsReceipts)
        setBoolean(startIndex + 6, request.supportsBarcodes)
        setBoolean(startIndex + 7, request.isDefaultReceipt)
        setBoolean(startIndex + 8, request.isDefaultBarcode)
        setNullableString(startIndex + 9, request.notes?.cleanOptional())
    }

    private fun ResultSet.toProductCategoryResponse(): ProductCategoryResponse =
        ProductCategoryResponse(
            id = getString("id"),
            name = getString("name"),
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
            name = getString("name"),
            description = getString("description"),
            discountType = getString("discount_type"),
            discountValue = getBigDecimal("discount_value").decimalString(),
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
        val discount = if (getString("offer_id") == null) {
            BigDecimal.ZERO
        } else {
            discountAmount(price, discountType, discountValue)
        }
        val finalPrice = price.subtract(discount).coerceAtLeast(BigDecimal.ZERO).scaled()
        return PublicCatalogProductResponse(
            id = getString("id"),
            categoryId = getString("category_id"),
            categoryName = getString("category_name"),
            name = getString("name"),
            description = getString("description"),
            unit = getString("unit"),
            sellingPrice = price.moneyString(),
            currency = getString("currency") ?: "INR",
            taxRate = getBigDecimal("tax_rate").decimalString(),
            pricesIncludeTax = getBoolean("prices_include_tax"),
            trackStock = trackStock,
            stockQuantity = stockQuantity.decimalString(),
            inStock = !trackStock || stockQuantity > BigDecimal.ZERO,
            imageUrl = getString("image_storage_path").toDashboardMediaUrl(),
            offer = getString("offer_id")?.let {
                PublicCatalogOfferResponse(
                    id = it,
                    name = getString("offer_name"),
                    description = getString("offer_description"),
                    discountType = discountType ?: "percentage",
                    discountValue = discountValue.decimalString(),
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
            categoryId = getString("category_id"),
            categoryName = getString("category_name"),
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
            imageUrl = getString("image_storage_path").toDashboardMediaUrl(),
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
            notes = getString("notes"),
            status = getString("status"),
            createdAt = getString("created_at"),
            updatedAt = getString("updated_at"),
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
            fulfillmentType = getString("fulfillment_type") ?: "standard",
            paymentMode = getString("payment_mode") ?: "pay_on_spot",
            source = getString("source") ?: "dashboard",
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

    private fun PreparedStatement.bindStringParams(params: List<String>) {
        params.forEachIndexed { index, value ->
            setString(index + 1, value)
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

    private fun String?.cleanSearchTerm(): String? =
        this?.trim()?.take(80)?.ifBlank { null }

    private fun String.ilikePattern(): String =
        "%${replace("%", "\\%").replace("_", "\\_")}%"

    private fun Int.sanitizedLimit(): Int =
        coerceIn(1, 200)

    private fun String?.cleanOrderStatusFilter(): String? =
        this
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() && it != "all" }
            ?.filter { it.isLetterOrDigit() || it == '_' }
            ?.takeIf { it in AllowedOrderStatuses }

    private fun String.cleanPrinterConnectionType(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedPrinterConnectionTypes) normalized else "mtp_usb"
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
        return if (normalized in setOf("pay_on_spot", "upi", "cash", "card", "online", "bank_transfer")) {
            normalized
        } else {
            "pay_on_spot"
        }
    }

    private fun String.cleanOrderSource(): String {
        val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in setOf("dashboard", "public_catalog", "api", "import")) normalized else "dashboard"
    }

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

    private fun discountAmount(
        price: BigDecimal,
        discountType: String?,
        discountValue: BigDecimal,
    ): BigDecimal {
        val safeValue = discountValue.coerceAtLeast(BigDecimal.ZERO)
        return when (discountType?.cleanDiscountType()) {
            "fixed" -> safeValue.coerceAtMost(price).scaled()
            else -> price
                .multiply(safeValue.coerceAtMost(BigDecimal("100")))
                .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                .coerceAtMost(price)
                .scaled()
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

    private fun String.normalizedOrderStatus(): String {
        val normalized = trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        return if (normalized in AllowedOrderStatuses) normalized else "confirmed"
    }

    private fun String.appliesInventory(): Boolean =
        this in InventoryApplyingStatuses

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
                    supplierName = row.value("supplierName", "supplier name", "supplier"),
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
        if (!sellingPrice.isValidNonNegativeDecimal()) add("Selling price must be a valid number.")
        if (!costPrice.isValidNonNegativeDecimal()) add("Cost price must be a valid number.")
        if (!taxRate.isValidNonNegativeDecimal()) add("Tax rate must be a valid number.")
        if (!stockQuantity.isValidNonNegativeDecimal()) add("Stock quantity must be zero or higher.")
        if (!reorderLevel.isValidNonNegativeDecimal()) add("Reorder level must be zero or higher.")
    }

    private fun String.isValidNonNegativeDecimal(): Boolean {
        val value = trim().replace(",", "")
        if (value.isBlank()) return true
        return value.toBigDecimalOrNull()?.let { it >= BigDecimal.ZERO } == true
    }

    private fun ProductImportRowRequest.toProductRequest(
        access: DashboardWorkspaceAccess,
        supplierId: String?,
        productName: String,
    ): ProductRequest =
        ProductRequest(
            name = productName,
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
            supplierId = supplierId,
        )

    private fun List<ProductResponse>.toProductCsv(): String {
        val rows = map { product ->
            listOf(
                product.name,
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
                product.supplierName.orEmpty(),
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
        val AllowedPrinterConnectionTypes = setOf(
            "mtp_usb",
            "usb",
            "bluetooth",
            "network",
            "airprint",
            "system",
            "esc_pos",
        )
    }
}
