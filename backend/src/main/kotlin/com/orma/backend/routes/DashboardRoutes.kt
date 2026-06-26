package com.orma.backend.routes

import com.orma.backend.auth.VerifiedFirebaseUser
import com.orma.backend.config.AppConfig
import com.orma.backend.db.DashboardQueryFilters
import com.orma.backend.db.DashboardOrderValidationException
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

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

    post("/public/workspaces/{workspaceId}/orders/{orderId}") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val workspaceId = call.parameters["workspaceId"].orEmpty()
        val orderId = call.parameters["orderId"].orEmpty()
        val order = repository.publicCatalogOrder(workspaceId, orderId)
            ?: return@post call.publicCatalogNotFound()
        call.respond(order)
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
            PublicCatalogOrderSubmitResult.AppointmentTimeRequired -> call.respondValidation(
                code = "public_appointment_time_required",
                message = "Choose a preferred date and time before booking.",
            )
        }
    }

    get("/dashboard/summary") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        call.respondWorkspaceResult(repository.summary(firebaseUser, call.dashboardFilters()))
    }

    get("/customers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val customers = repository.customers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(CustomerListResponse(customers.items, customers.pagination))
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

    get("/customers/{id}/orders") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val customerId = call.parameters["id"].orEmpty()
        val orders = repository.customerOrders(firebaseUser, customerId, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(OrderListResponse(orders.items, orders.pagination))
    }

    get("/suppliers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val suppliers = repository.suppliers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(SupplierListResponse(suppliers.items, suppliers.pagination))
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
        call.respond(ProductListResponse(products.items, products.pagination))
    }

    get("/product-categories") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val categories = repository.productCategories(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(ProductCategoryListResponse(categories.items, categories.pagination))
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
        call.respond(ProductOfferListResponse(offers.items, offers.pagination))
    }

    post("/offers") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductOfferRequest>()
        val scope = request.appliesTo.normalizedOfferScope()
        if (request.name.isBlank()) {
            call.respondValidation("offer_name_required", "Enter the offer name.")
            return@post
        }
        if ((request.discountValue.toDoubleOrNull() ?: 0.0) <= 0.0) {
            call.respondValidation("offer_discount_required", "Enter a discount greater than zero.")
            return@post
        }
        if (request.discountType.equals("percentage", ignoreCase = true) && (request.discountValue.toDoubleOrNull() ?: 0.0) > 100.0) {
            call.respondValidation("offer_discount_too_high", "Percentage discount cannot be above 100%.")
            return@post
        }
        if (!request.discountCapAmount.isNullOrBlank() && (request.discountCapAmount.toDoubleOrNull() ?: -1.0) <= 0.0) {
            call.respondValidation("offer_cap_invalid", "Enter a cap amount greater than zero, or leave it empty.")
            return@post
        }
        if (!request.customerId.isNullOrBlank() && request.couponCode.isNullOrBlank()) {
            call.respondValidation("offer_coupon_required", "Enter a coupon code for a customer-specific offer.")
            return@post
        }
        if (scope == "category" && request.categoryId.isNullOrBlank()) {
            call.respondValidation("offer_category_required", "Choose the category for this offer.")
            return@post
        }
        if (scope == "product" && request.productId.isNullOrBlank()) {
            call.respondValidation("offer_product_required", "Choose the product or service for this offer.")
            return@post
        }
        val offer = try {
            repository.createProductOffer(firebaseUser, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the offer details.")
            return@post
        }
        call.respondWorkspaceResult(offer)
    }

    put("/offers/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val offerId = call.parameters["id"].orEmpty()
        val request = call.receive<ProductOfferRequest>()
        val scope = request.appliesTo.normalizedOfferScope()
        if (request.name.isBlank()) {
            call.respondValidation("offer_name_required", "Enter the offer name.")
            return@put
        }
        if ((request.discountValue.toDoubleOrNull() ?: 0.0) <= 0.0) {
            call.respondValidation("offer_discount_required", "Enter a discount greater than zero.")
            return@put
        }
        if (request.discountType.equals("percentage", ignoreCase = true) && (request.discountValue.toDoubleOrNull() ?: 0.0) > 100.0) {
            call.respondValidation("offer_discount_too_high", "Percentage discount cannot be above 100%.")
            return@put
        }
        if (!request.discountCapAmount.isNullOrBlank() && (request.discountCapAmount.toDoubleOrNull() ?: -1.0) <= 0.0) {
            call.respondValidation("offer_cap_invalid", "Enter a cap amount greater than zero, or leave it empty.")
            return@put
        }
        if (!request.customerId.isNullOrBlank() && request.couponCode.isNullOrBlank()) {
            call.respondValidation("offer_coupon_required", "Enter a coupon code for a customer-specific offer.")
            return@put
        }
        if (scope == "category" && request.categoryId.isNullOrBlank()) {
            call.respondValidation("offer_category_required", "Choose the category for this offer.")
            return@put
        }
        if (scope == "product" && request.productId.isNullOrBlank()) {
            call.respondValidation("offer_product_required", "Choose the product or service for this offer.")
            return@put
        }
        val offer = try {
            repository.updateProductOffer(firebaseUser, offerId, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the offer details.")
            return@put
        }
        call.respondWorkspaceResult(offer)
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
        val result = try {
            repository.importProductsCsv(firebaseUser, request.csv)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the import permissions.")
            return@post
        }
        call.respondWorkspaceResult(result)
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
        val result = try {
            repository.importProducts(firebaseUser, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the import permissions.")
            return@post
        }
        call.respondWorkspaceResult(result)
    }

    post("/products") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<ProductRequest>()
        if (request.name.isBlank()) {
            call.respondValidation("product_name_required", "Enter the product name.")
            return@post
        }
        if (!request.expiryDate.isNullOrBlank() && !request.expiryDate.isValidIsoDateOnly()) {
            call.respondValidation("product_expiry_invalid", "Choose a valid expiry date.")
            return@post
        }
        val product = try {
            repository.createProduct(firebaseUser, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the item details.")
            return@post
        }
        call.respondWorkspaceResult(product)
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
        if (!request.expiryDate.isNullOrBlank() && !request.expiryDate.isValidIsoDateOnly()) {
            call.respondValidation("product_expiry_invalid", "Choose a valid expiry date.")
            return@put
        }
        val product = try {
            repository.updateProduct(firebaseUser, productId, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the item details.")
            return@put
        }
        call.respondWorkspaceResult(product)
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
        if (!request.expiryDate.isNullOrBlank() && !request.expiryDate.isValidIsoDateOnly()) {
            call.respondValidation("product_expiry_invalid", "Choose a valid expiry date.")
            return@post
        }
        val product = try {
            repository.adjustStock(firebaseUser, productId, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the stock update.")
            return@post
        }
        call.respondWorkspaceResult(product)
    }

    get("/orders") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val orders = repository.orders(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(OrderListResponse(orders.items, orders.pagination))
    }

    post("/orders") {
        val repository = dashboardRepository ?: return@post call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@post
        val request = call.receive<OrderRequest>()
        if (request.items.isEmpty()) {
            call.respondValidation("order_items_required", "Add at least one item before creating the order.")
            return@post
        }
        if (request.orderType.normalizedDashboardOrderType() == "appointment" && request.scheduledAt.isNullOrBlank()) {
            call.respondValidation("appointment_time_required", "Choose a preferred date and time for this appointment.")
            return@post
        }
        val order = try {
            repository.createOrder(firebaseUser, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the order details.")
            return@post
        }
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

    put("/orders/{id}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val orderId = call.parameters["id"].orEmpty()
        val request = call.receive<OrderRequest>()
        if (request.items.isEmpty()) {
            call.respondValidation("order_items_required", "Add at least one item before saving the booking.")
            return@put
        }
        if (request.orderType.normalizedDashboardOrderType() == "appointment" && request.scheduledAt.isNullOrBlank()) {
            call.respondValidation("appointment_time_required", "Choose a preferred date and time for this appointment.")
            return@put
        }
        val order = try {
            repository.updateOrder(firebaseUser, orderId, request)
        } catch (error: DashboardOrderValidationException) {
            call.respondValidation(error.code, error.message ?: "Check the order details.")
            return@put
        }
        call.respondWorkspaceResult(order)
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
        if (call.respondOrderStatusPaymentValidation(repository, firebaseUser, orderId, request)) return@put
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status, request.paidTotal))
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
        if (call.respondOrderStatusPaymentValidation(repository, firebaseUser, orderId, request)) return@post
        call.respondWorkspaceResult(repository.updateOrderStatus(firebaseUser, orderId, request.status, request.paidTotal))
    }

    get("/printers") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val printers = repository.printers(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(PrinterProfileListResponse(printers.items, printers.pagination))
    }

    get("/payment-methods") {
        val repository = dashboardRepository ?: return@get call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@get
        val methods = repository.paymentMethods(firebaseUser, call.dashboardFilters()) ?: return@get call.workspaceNotFound()
        call.respond(WorkspacePaymentMethodListResponse(methods.items, methods.pagination))
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
        if (!request.upiId.contains("@")) {
            call.respondValidation("payment_upi_invalid", "Enter a valid UPI ID.")
            return@post
        }
        call.respondWorkspaceResult(repository.createPaymentMethod(firebaseUser, request))
    }

    put("/payment-methods/{paymentMethodId}") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val paymentMethodId = call.parameters["paymentMethodId"].orEmpty()
        val request = call.receive<WorkspacePaymentMethodRequest>()
        if (request.label.isBlank()) {
            call.respondValidation("payment_label_required", "Enter a payment label.")
            return@put
        }
        if (request.upiId.isBlank()) {
            call.respondValidation("payment_upi_required", "Enter a UPI ID.")
            return@put
        }
        if (!request.upiId.contains("@")) {
            call.respondValidation("payment_upi_invalid", "Enter a valid UPI ID.")
            return@put
        }
        call.respondWorkspaceResult(repository.updatePaymentMethod(firebaseUser, paymentMethodId, request))
    }

    put("/payment-methods/{paymentMethodId}/default") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val paymentMethodId = call.parameters["paymentMethodId"].orEmpty()
        call.respondWorkspaceResult(repository.setDefaultPaymentMethod(firebaseUser, paymentMethodId))
    }

    put("/payment-methods/{paymentMethodId}/delete") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val paymentMethodId = call.parameters["paymentMethodId"].orEmpty()
        call.respondWorkspaceResult(repository.deletePaymentMethod(firebaseUser, paymentMethodId))
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

    put("/printers/{id}/delete") {
        val repository = dashboardRepository ?: return@put call.dashboardDatabaseNotConfigured()
        val firebaseUser = call.verifiedFirebaseUser(config) ?: return@put
        val printerId = call.parameters["id"].orEmpty()
        call.respondWorkspaceResult(repository.deletePrinter(firebaseUser, printerId))
    }
}

private fun ApplicationCall.dashboardFilters(): DashboardQueryFilters {
    val query = request.queryParameters
    return DashboardQueryFilters(
        query = query["q"],
        status = query["status"],
        itemType = query["itemType"],
        orderType = query["orderType"],
        datePreset = query["datePreset"],
        dateFrom = query["dateFrom"],
        dateTo = query["dateTo"],
        page = query["page"]?.toIntOrNull() ?: 1,
        limit = query["limit"]?.toIntOrNull() ?: 50,
        lowStockOnly = query["lowStock"].toBooleanQuery(),
        supplierId = query["supplierId"],
        barcode = query["barcode"],
        scheduledOnly = query["scheduledOnly"].toBooleanQuery(),
        excludeCancelled = query["excludeCancelled"].toBooleanQuery(),
    )
}

private fun String?.toBooleanQuery(): Boolean =
    this?.trim()?.lowercase() in setOf("true", "1", "yes")

private fun String.normalizedDashboardOrderType(): String {
    val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
    return when (normalized) {
        "service", "services" -> "service"
        "appointment", "booking" -> "appointment"
        else -> "sale"
    }
}

private suspend fun ApplicationCall.respondOrderStatusPaymentValidation(
    repository: DashboardRepository,
    firebaseUser: VerifiedFirebaseUser,
    orderId: String,
    request: OrderStatusRequest,
): Boolean {
    if (request.status.normalizedDashboardOrderStatus() != "part_paid") return false
    val order = repository.order(firebaseUser, orderId) ?: run {
        workspaceNotFound()
        return true
    }
    val validation = partPaidAmountValidation(request.paidTotal, order.total) ?: return false
    respondValidation(validation.first, validation.second)
    return true
}

private fun partPaidAmountValidation(amount: String?, total: String): Pair<String, String>? {
    val cleanAmount = amount?.trim().orEmpty()
    if (cleanAmount.isBlank()) {
        return "part_paid_amount_required" to "Enter the amount collected."
    }
    val paidAmount = cleanAmount.dashboardMoneyOrNull()
        ?: return "part_paid_amount_invalid" to "Enter a valid amount."
    val totalAmount = total.dashboardMoneyOrNull() ?: BigDecimal.ZERO
    return when {
        totalAmount <= BigDecimal.ZERO -> "part_paid_total_invalid" to "Order total must be above zero before recording payment."
        paidAmount <= BigDecimal.ZERO -> "part_paid_amount_invalid" to "Enter an amount above zero."
        paidAmount >= totalAmount -> "part_paid_amount_too_high" to "For full collection, choose Paid instead of Part paid."
        else -> null
    }
}

private fun String.normalizedDashboardOrderStatus(): String =
    trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }

private fun String.dashboardMoneyOrNull(): BigDecimal? =
    trim()
        .replace(",", "")
        .toBigDecimalOrNull()
        ?.setScale(2, RoundingMode.HALF_UP)

private fun String.normalizedOfferScope(): String {
    val normalized = trim().lowercase().replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }
    return if (normalized in setOf("all", "category", "product")) normalized else "product"
}

private fun String.isValidIsoDateOnly(): Boolean =
    runCatching { LocalDate.parse(trim()) }.isSuccess

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
