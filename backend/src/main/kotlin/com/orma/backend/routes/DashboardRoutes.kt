package com.orma.backend.routes

import com.orma.backend.config.AppConfig
import com.orma.backend.db.DashboardQueryFilters
import com.orma.backend.db.DashboardRepository
import com.orma.backend.db.PublicCatalogOrderSubmitResult
import com.orma.backend.models.CustomerListResponse
import com.orma.backend.models.CustomerRequest
import com.orma.backend.models.ErrorResponse
import com.orma.backend.models.OrderListResponse
import com.orma.backend.models.OrderRequest
import com.orma.backend.models.OrderStatusRequest
import com.orma.backend.models.PrinterProfileListResponse
import com.orma.backend.models.PrinterProfileRequest
import com.orma.backend.models.ProductCategoryListResponse
import com.orma.backend.models.ProductCategoryRequest
import com.orma.backend.models.ProductOfferListResponse
import com.orma.backend.models.ProductOfferRequest
import com.orma.backend.models.PublicCatalogOrderRequest
import com.orma.backend.models.ProductImportCsvRequest
import com.orma.backend.models.ProductImportRequest
import com.orma.backend.models.ProductListResponse
import com.orma.backend.models.ProductRequest
import com.orma.backend.models.StockAdjustmentRequest
import com.orma.backend.models.SupplierListResponse
import com.orma.backend.models.SupplierRequest
import com.orma.backend.models.WorkspacePaymentMethodListResponse
import com.orma.backend.models.WorkspacePaymentMethodRequest
import com.orma.backend.notifications.OrderNotificationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.dashboardRoutes(
    config: AppConfig,
    dashboardRepository: DashboardRepository?,
    orderNotificationService: OrderNotificationService?,
) {
    get("/public/workspaces/{workspaceId}/catalog") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val workspaceId = call.parameters["workspaceId"].orEmpty()
        val catalog = repository.publicCatalog(workspaceId)
            ?: return@get call.publicCatalogNotFound()
        call.respond(catalog)
    }

    post("/public/workspaces/{workspaceId}/catalog") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val workspaceId = call.parameters["workspaceId"].orEmpty()
        val catalog = repository.publicCatalog(workspaceId)
            ?: return@post call.publicCatalogNotFound()
        call.respond(catalog)
    }

    post("/public/workspaces/{workspaceId}/orders") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val workspaceId = call.parameters["workspaceId"].orEmpty()
        val request = call.receive<PublicCatalogOrderRequest>()
        if (request.customerName.isBlank()) {
            call.respondValidation("public_customer_name_required", "Enter your name.")
            return@post
        }
        if (request.phoneNumber.isBlank()) {
            call.respondValidation("public_customer_phone_required", "Enter your phone number.")
            return@post
        }
        if (request.items.isEmpty()) {
            call.respondValidation("public_items_required", "Select at least one item.")
            return@post
        }
        when (val result = repository.createPublicCatalogOrder(workspaceId, request)) {
            is PublicCatalogOrderSubmitResult.Success -> {
                orderNotificationService?.notifyOrderCreated(result.response.order)
                call.respond(result.response)
            }
            PublicCatalogOrderSubmitResult.WorkspaceNotFound -> call.publicCatalogNotFound()
            PublicCatalogOrderSubmitResult.ItemsUnavailable -> call.respondValidation(
                code = "public_items_unavailable",
                message = "Selected items are unavailable. Refresh the catalog and try again.",
            )
        }
    }

    get("/dashboard/summary") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        call.respondWorkspaceResult(repository.summary(firebaseUser))
    }

    get("/customers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val customers = repository.customers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(CustomerListResponse(customers))
    }

    post("/customers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<CustomerRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("customer_name_required", "Enter the customer name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createCustomer(firebaseUser, request))
    }

    put("/customers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val customerId = call.parameters["id"].orEmpty()
        val request = call.receive<CustomerRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("customer_name_required", "Enter the customer name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateCustomer(firebaseUser, customerId, request))
    }

    get("/suppliers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val suppliers = repository.suppliers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(SupplierListResponse(suppliers))
    }

    post("/suppliers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<SupplierRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("supplier_name_required", "Enter the supplier name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createSupplier(firebaseUser, request))
    }

    put("/suppliers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val supplierId = call.parameters["id"].orEmpty()
        val request = call.receive<SupplierRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("supplier_name_required", "Enter the supplier name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateSupplier(firebaseUser, supplierId, request))
    }

    get("/products") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val products = repository.products(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(ProductListResponse(products))
    }

    get("/product-categories") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val categories = repository.productCategories(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(ProductCategoryListResponse(categories))
    }

    post("/product-categories") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductCategoryRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_category_name_required", "Enter the category name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createProductCategory(firebaseUser, request))
    }

    get("/offers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val offers = repository.productOffers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(ProductOfferListResponse(offers))
    }

    post("/offers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductOfferRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("offer_name_required", "Enter the offer name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createProductOffer(firebaseUser, request))
    }

    get("/products/export") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val export = repository.exportProducts(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(export)
    }

    get("/products/import-template") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val template = repository.productImportTemplate(firebaseUser) ?: return@get call.workspaceNotFound()
        call.respond(template)
    }

    post("/products/import-csv") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductImportCsvRequest>()
        if (request.csv.isBlank()) {
            call.respondValidation("product_import_empty", "Paste or upload a product CSV before importing.")
            return@post
        }
        call.respondWorkspaceResult(repository.importProductsCsv(firebaseUser, request.csv))
    }

    post("/products/import") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductImportRequest>()
        if (request.rows.isEmpty()) {
            call.respondValidation("product_import_empty", "Paste at least one product row before importing.")
            return@post
        }
        if (request.rows.size > 500) {
            call.respondValidation("product_import_limit", "Import up to 500 product rows at a time.")
            return@post
        }
        call.respondWorkspaceResult(repository.importProducts(firebaseUser, request))
    }

    post("/products") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_name_required", "Enter the product name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createProduct(firebaseUser, request))
    }

    put("/products/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val productId = call.parameters["id"].orEmpty()
        val request = call.receive<ProductRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_name_required", "Enter the product name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateProduct(firebaseUser, productId, request))
    }

    post("/products/{id}/stock-adjustments") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val productId = call.parameters["id"].orEmpty()
        val request = call.receive<StockAdjustmentRequest>()
        if (request.quantityDelta.isBlank()) {
            call.respondValidation("stock_quantity_required", "Enter the stock adjustment quantity.")
            return@post
        }
        call.respondWorkspaceResult(repository.adjustStock(firebaseUser, productId, request))
    }

    get("/orders") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val orders = repository.orders(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(OrderListResponse(orders))
    }

    post("/orders") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<OrderRequest>()
        if (request.items.isEmpty()) {
            call.respondValidation("order_items_required", "Add at least one item before creating the order.")
            return@post
        }
        val order = repository.createOrder(firebaseUser, request)
        if (order != null) {
            orderNotificationService?.notifyOrderCreated(order)
        }
        call.respondWorkspaceResult(order)
    }

    get("/orders/{id}") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val orderId = call.parameters["id"].orEmpty()
        call.respondWorkspaceResult(repository.order(firebaseUser, orderId))
    }

    put("/orders/{id}/status") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val orderId = call.parameters["id"].orEmpty()
        val request = call.receive<OrderStatusRequest>()
        if (request.status.isBlank()) {
            call.respondValidation("order_status_required", "Choose an order status.")
            return@put
        }
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status))
    }

    post("/orders/{id}/status") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val orderId = call.parameters["id"].orEmpty()
        val request = call.receive<OrderStatusRequest>()
        if (request.status.isBlank()) {
            call.respondValidation("order_status_required", "Choose an order status.")
            return@post
        }
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status))
    }

    get("/printers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val printers = repository.printers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(PrinterProfileListResponse(printers))
    }

    get("/payment-methods") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val methods = repository.paymentMethods(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(WorkspacePaymentMethodListResponse(methods))
    }

    post("/payment-methods") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<WorkspacePaymentMethodRequest>()
        if (request.label.isBlank()) {
            call.respondValidation("payment_label_required", "Enter a payment label.")
            return@post
        }
        if (request.upiId.isBlank()) {
            call.respondValidation("payment_upi_required", "Enter a UPI ID.")
            return@post
        }
        call.respondWorkspaceResult(repository.createPaymentMethod(firebaseUser, request))
    }

    post("/printers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<PrinterProfileRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("printer_name_required", "Enter the printer name.")
            return@post
        }
        call.respondWorkspaceResult(repository.createPrinter(firebaseUser, request))
    }

    put("/printers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val printerId = call.parameters["id"].orEmpty()
        val request = call.receive<PrinterProfileRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("printer_name_required", "Enter the printer name.")
            return@put
        }
        call.respondWorkspaceResult(repository.updatePrinter(firebaseUser, printerId, request))
    }
}

private fun ApplicationCall.dashboardFilters(): DashboardQueryFilters {
    val query = request.queryParameters
    return DashboardQueryFilters(
        query = query["q"],
        status = query["status"],
        limit = query["limit"]?.toIntOrNull() ?: 80,
        lowStockOnly = query["lowStock"].toBooleanQuery(),
        supplierId = query["supplierId"],
        barcode = query["barcode"],
        scheduledOnly = query["scheduledOnly"].toBooleanQuery(),
    )
}

private fun String?.toBooleanQuery(): Boolean =
    this?.trim()?.lowercase() in setOf("true", "1", "yes")

private suspend fun ApplicationCall.respondWorkspaceResult(value: Any?) {
    if (value == null) {
        workspaceNotFound()
    } else {
        respond(value)
    }
}

private suspend fun ApplicationCall.dashboardDatabaseNotConfigured() {
    respond(
        HttpStatusCode.ServiceUnavailable,
        ErrorResponse(
            code = "database_not_configured",
            message = "DATABASE_URL is required before dashboard APIs can run.",
        ),
    )
}

private suspend fun ApplicationCall.workspaceNotFound() {
    respond(
        HttpStatusCode.NotFound,
        ErrorResponse(
            code = "workspace_not_found",
            message = "Complete business setup before using this workspace module.",
        ),
    )
}

private suspend fun ApplicationCall.publicCatalogNotFound() {
    respond(
        HttpStatusCode.NotFound,
        ErrorResponse(
            code = "public_catalog_not_found",
            message = "This ORMA ordering link is not active.",
        ),
    )
}

private suspend fun ApplicationCall.respondValidation(code: String, message: String) {
    respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(code = code, message = message),
    )
}
