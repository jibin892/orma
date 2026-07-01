package com.orma.backend

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.db.DashboardOrderValidationException
import com.orma.backend.db.DashboardQueryFilters
import com.orma.backend.db.DashboardRepository
import com.orma.backend.db.DatabaseFactory
import com.orma.backend.db.PublicCatalogOrderSubmitResult
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.OrderItemRequest
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.PrinterProfileRequest
import com.orma.backend.models.ProductRequest
import com.orma.backend.models.ProductVariantRequest
import com.orma.backend.models.PublicCatalogOrderItemRequest
import com.orma.backend.models.PublicCatalogOrderRequest
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class DashboardRepositoryIntegrationTest {
    @Test
    fun readOnlyMemberCannotCreateCustomer() = withDashboardTestDatabase { repository, fixture ->
        val error = assertFailsWith<DashboardOrderValidationException> {
            runBlocking {
                repository.createCustomer(
                    firebaseUser = fixture.readOnlyUser,
                    request = CustomerRequest(name = "Blocked Customer"),
                )
            }
        }

        assertEquals("team_permission_denied", error.code)
    }

    @Test
    fun confirmedSaleReducesProductInventory() = withDashboardTestDatabase { repository, fixture ->
        val product = runBlocking {
            repository.createProduct(
                firebaseUser = fixture.ownerUser,
                request = ProductRequest(
                    name = fixture.uniqueName("Stocked Coffee"),
                    sellingPrice = "10",
                    costPrice = "4",
                    stockQuantity = "5",
                    reorderLevel = "1",
                    trackStock = true,
                    currency = "INR",
                ),
            )
        }
        assertNotNull(product)

        val order = runBlocking {
            repository.createOrder(
                firebaseUser = fixture.ownerUser,
                request = OrderRequest(
                    customerName = "Inventory Customer",
                    status = "confirmed",
                    paidTotal = "20",
                    items = listOf(
                        OrderItemRequest(
                            productId = product.id,
                            description = product.name,
                            quantity = "2",
                            unitPrice = "10",
                        ),
                    ),
                ),
            )
        }
        assertNotNull(order)

        val updatedProduct = runBlocking {
            repository.products(
                firebaseUser = fixture.ownerUser,
                filters = DashboardQueryFilters(query = product.name),
            )
        }?.items?.firstOrNull { it.id == product.id }

        assertNotNull(updatedProduct)
        assertEquals(3.0, updatedProduct.stockQuantity.toDoubleOrNull())
    }

    @Test
    fun publicCatalogRejectsProductQuantityAboveAvailableStock() = withDashboardTestDatabase { repository, fixture ->
        val product = runBlocking {
            repository.createProduct(
                firebaseUser = fixture.ownerUser,
                request = ProductRequest(
                    name = fixture.uniqueName("Catalog Limited Item"),
                    sellingPrice = "25",
                    costPrice = "10",
                    stockQuantity = "1",
                    reorderLevel = "0",
                    trackStock = true,
                    currency = "INR",
                ),
            )
        }
        assertNotNull(product)

        val catalog = runBlocking { repository.publicCatalog(fixture.workspaceId) }
        assertNotNull(catalog)
        assertEquals(true, catalog.products.any { it.id == product.id && it.inStock })

        val result = runBlocking {
            repository.createPublicCatalogOrder(
                workspaceId = fixture.workspaceId,
                request = PublicCatalogOrderRequest(
                    customerName = "Catalog Customer",
                    phoneNumber = "+919999999999",
                    clientRequestId = fixture.uniqueName("catalog-order"),
                    items = listOf(
                        PublicCatalogOrderItemRequest(
                            productId = product.id,
                            quantity = "2",
                        ),
                    ),
                ),
            )
        }

        assertIs<PublicCatalogOrderSubmitResult.ItemsUnavailable>(result)
    }

    @Test
    fun updateProductPersistsProductStockTrackingToggle() = withDashboardTestDatabase { repository, fixture ->
        val product = runBlocking {
            repository.createProduct(
                firebaseUser = fixture.ownerUser,
                request = ProductRequest(
                    name = fixture.uniqueName("Product Stock Toggle"),
                    sellingPrice = "80",
                    costPrice = "35",
                    stockQuantity = "12",
                    reorderLevel = "3",
                    trackStock = true,
                    currency = "INR",
                ),
            )
        }
        assertNotNull(product)
        assertEquals(true, product.trackStock)

        val updated = runBlocking {
            repository.updateProduct(
                firebaseUser = fixture.ownerUser,
                productId = product.id,
                request = ProductRequest(
                    name = product.name,
                    sellingPrice = product.sellingPrice,
                    costPrice = product.costPrice,
                    stockQuantity = product.stockQuantity,
                    reorderLevel = product.reorderLevel,
                    trackStock = false,
                    currency = product.currency,
                ),
            )
        }

        assertNotNull(updated)
        assertEquals(false, updated.trackStock)
        assertEquals(0.0, updated.stockQuantity.toDoubleOrNull())
        assertEquals(0.0, updated.reorderLevel.toDoubleOrNull())

        val reloaded = runBlocking {
            repository.products(
                firebaseUser = fixture.ownerUser,
                filters = DashboardQueryFilters(query = product.name),
            )
        }?.items?.firstOrNull { it.id == product.id }

        assertNotNull(reloaded)
        assertEquals(false, reloaded.trackStock)
        assertEquals(0.0, reloaded.stockQuantity.toDoubleOrNull())
        assertEquals(0.0, reloaded.reorderLevel.toDoubleOrNull())
    }

    @Test
    fun updateProductPersistsVariantStockTrackingToggle() = withDashboardTestDatabase { repository, fixture ->
        val product = runBlocking {
            repository.createProduct(
                firebaseUser = fixture.ownerUser,
                request = ProductRequest(
                    name = fixture.uniqueName("Variant Stock Toggle"),
                    sellingPrice = "70",
                    costPrice = "30",
                    stockQuantity = "10",
                    reorderLevel = "0",
                    trackStock = true,
                    currency = "INR",
                    variants = listOf(
                        ProductVariantRequest(
                            name = "Large (500ml)",
                            sellingPrice = "70",
                            stockQuantity = "4",
                            trackStock = true,
                        ),
                    ),
                ),
            )
        }
        assertNotNull(product)
        val variant = product.variants.first()
        assertEquals(true, variant.trackStock)

        val updated = runBlocking {
            repository.updateProduct(
                firebaseUser = fixture.ownerUser,
                productId = product.id,
                request = ProductRequest(
                    name = product.name,
                    sellingPrice = product.sellingPrice,
                    costPrice = product.costPrice,
                    stockQuantity = product.stockQuantity,
                    reorderLevel = product.reorderLevel,
                    trackStock = product.trackStock,
                    currency = product.currency,
                    variants = listOf(
                        ProductVariantRequest(
                            id = variant.id,
                            name = variant.name,
                            sellingPrice = variant.sellingPrice,
                            stockQuantity = variant.stockQuantity,
                            trackStock = false,
                        ),
                    ),
                ),
            )
        }
        assertNotNull(updated)
        assertEquals(false, updated.variants.firstOrNull { it.id == variant.id }?.trackStock)

        val reloaded = runBlocking {
            repository.products(
                firebaseUser = fixture.ownerUser,
                filters = DashboardQueryFilters(query = product.name),
            )
        }?.items?.firstOrNull { it.id == product.id }

        assertNotNull(reloaded)
        assertEquals(false, reloaded.variants.firstOrNull { it.id == variant.id }?.trackStock)
    }

    @Test
    fun printerReceiptLayoutOptionsPersistAcrossUpdateAndList() = withDashboardTestDatabase { repository, fixture ->
        val printer = runBlocking {
            repository.createPrinter(
                firebaseUser = fixture.ownerUser,
                request = PrinterProfileRequest(
                    name = fixture.uniqueName("Mobile Thermal"),
                    connectionType = "bluetooth",
                    address = "66:32:D4:4A:B2:B3",
                    paperWidthMm = 58,
                    dpi = 203,
                    isDefaultReceipt = true,
                    printLogo = true,
                    headerAlignment = "left",
                    showBusinessAddress = false,
                    showCatalogQr = true,
                    showTimedGreeting = false,
                ),
            )
        }
        assertNotNull(printer)
        assertEquals(true, printer.printLogo)
        assertEquals("left", printer.headerAlignment)
        assertEquals(false, printer.showBusinessAddress)
        assertEquals(true, printer.showCatalogQr)
        assertEquals(false, printer.showTimedGreeting)

        val updated = runBlocking {
            repository.updatePrinter(
                firebaseUser = fixture.ownerUser,
                printerId = printer.id,
                request = PrinterProfileRequest(
                    name = printer.name,
                    connectionType = printer.connectionType,
                    address = printer.address,
                    paperWidthMm = printer.paperWidthMm,
                    dpi = printer.dpi,
                    isDefaultReceipt = true,
                    printLogo = false,
                    headerAlignment = "center",
                    showBusinessAddress = true,
                    showCatalogQr = false,
                    showTimedGreeting = true,
                ),
            )
        }
        assertNotNull(updated)
        assertEquals(false, updated.printLogo)
        assertEquals("center", updated.headerAlignment)
        assertEquals(true, updated.showBusinessAddress)
        assertEquals(false, updated.showCatalogQr)
        assertEquals(true, updated.showTimedGreeting)

        val reloaded = runBlocking {
            repository.printers(
                firebaseUser = fixture.ownerUser,
                filters = DashboardQueryFilters(query = printer.name),
            )
        }?.items?.firstOrNull { it.id == printer.id }

        assertNotNull(reloaded)
        assertEquals(false, reloaded.printLogo)
        assertEquals("center", reloaded.headerAlignment)
        assertEquals(true, reloaded.showBusinessAddress)
        assertEquals(false, reloaded.showCatalogQr)
        assertEquals(true, reloaded.showTimedGreeting)
    }
}

private inline fun withDashboardTestDatabase(
    block: (DashboardRepository, DashboardIntegrationFixture) -> Unit,
) {
    val databaseUrl = System.getenv("ORMA_TEST_DATABASE_URL")?.takeIf { it.isNotBlank() } ?: return
    val config = AppConfig.test().copy(
        databaseUrl = databaseUrl,
        databaseUser = System.getenv("ORMA_TEST_DATABASE_USER")?.takeIf { it.isNotBlank() },
        databasePassword = System.getenv("ORMA_TEST_DATABASE_PASSWORD")?.takeIf { it.isNotBlank() },
        runMigrations = true,
    )
    val dataSource = DatabaseFactory.dataSource(config) ?: return
    try {
        DatabaseFactory.migrate(dataSource)
        val fixture = dataSource.connection.use { connection ->
            connection.createDashboardIntegrationFixture()
        }
        try {
            block(DashboardRepository(dataSource, config), fixture)
        } finally {
            dataSource.connection.use { connection ->
                connection.cleanupDashboardIntegrationFixture(fixture)
            }
        }
    } finally {
        dataSource.close()
    }
}

private data class DashboardIntegrationFixture(
    val runId: String,
    val workspaceId: String,
    val ownerUser: VerifiedFirebaseUser,
    val readOnlyUser: VerifiedFirebaseUser,
) {
    fun uniqueName(prefix: String): String = "$prefix $runId"
}

private fun Connection.createDashboardIntegrationFixture(): DashboardIntegrationFixture {
    val runId = UUID.randomUUID().toString().replace("-", "").take(10)
    val ownerUid = "test-owner-$runId"
    val readOnlyUid = "test-readonly-$runId"
    val ownerUserId = insertAppUser(ownerUid, "owner-$runId@example.com")
    val readOnlyUserId = insertAppUser(readOnlyUid, "readonly-$runId@example.com")
    val workspaceId = insertWorkspace(ownerUserId, runId)
    insertWorkspaceMember(
        workspaceId = workspaceId,
        userId = ownerUserId,
        role = "business_owner",
        permissions = listOf(
            "create_sale",
            "edit_sale",
            "change_booking_status",
            "create_product",
            "create_service",
            "create_appointment",
            "create_offer",
            "manage_stock",
            "manage_customers",
            "manage_account",
            "download_invoice",
        ),
    )
    insertWorkspaceMember(
        workspaceId = workspaceId,
        userId = readOnlyUserId,
        role = "read_only",
        permissions = listOf("read_only"),
    )
    return DashboardIntegrationFixture(
        runId = runId,
        workspaceId = workspaceId,
        ownerUser = VerifiedFirebaseUser(
            uid = ownerUid,
            email = "owner-$runId@example.com",
            phoneNumber = null,
            displayName = "Integration Owner",
            provider = "password",
        ),
        readOnlyUser = VerifiedFirebaseUser(
            uid = readOnlyUid,
            email = "readonly-$runId@example.com",
            phoneNumber = null,
            displayName = "Read Only",
            provider = "password",
        ),
    )
}

private fun Connection.insertAppUser(firebaseUid: String, email: String): String =
    prepareStatement(
        """
        insert into app_users (
            firebase_uid, email, role, onboarding_status, notifications_enabled, updated_at
        )
        values (?, ?, 'business_owner', 'completed', false, now())
        returning id::text
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, firebaseUid)
        statement.setString(2, email)
        statement.executeQuery().use { result ->
            result.next()
            result.getString("id")
        }
    }

private fun Connection.insertWorkspace(ownerUserId: String, runId: String): String =
    prepareStatement(
        """
        insert into business_workspaces (
            owner_user_id, business_name, legal_name, industry, address_line,
            city, country, invoice_prefix, next_invoice_number, payment_terms,
            invoice_footer, currency, tax_mode, business_mode, onboarding_completed_at, updated_at
        )
        values (
            ?::uuid, ?, ?, 'Cafe', 'Test Street', 'Test City', 'India',
            'TST', '1', 'Due on receipt', 'Thank you', 'INR', 'exclusive',
            'product_selling', now(), now()
        )
        returning id::text
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, ownerUserId)
        statement.setString(2, "Integration Workspace $runId")
        statement.setString(3, "Integration Workspace $runId")
        statement.executeQuery().use { result ->
            result.next()
            result.getString("id")
        }
    }

private fun Connection.insertWorkspaceMember(
    workspaceId: String,
    userId: String,
    role: String,
    permissions: List<String>,
) {
    prepareStatement(
        """
        insert into workspace_members (
            workspace_id, user_id, role, status, permissions, updated_at
        )
        values (?::uuid, ?::uuid, ?, 'active', ?, now())
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, workspaceId)
        statement.setString(2, userId)
        statement.setString(3, role)
        statement.setArray(4, createArrayOf("text", permissions.toTypedArray()))
        statement.executeUpdate()
    }
}

private fun Connection.cleanupDashboardIntegrationFixture(fixture: DashboardIntegrationFixture) {
    prepareStatement("delete from business_workspaces where id = ?::uuid").use { statement ->
        statement.setString(1, fixture.workspaceId)
        statement.executeUpdate()
    }
    prepareStatement("delete from app_users where firebase_uid in (?, ?)").use { statement ->
        statement.setString(1, fixture.ownerUser.uid)
        statement.setString(2, fixture.readOnlyUser.uid)
        statement.executeUpdate()
    }
}
